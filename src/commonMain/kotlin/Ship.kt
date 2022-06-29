import com.soywiz.korev.Key
import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.cosine
import com.soywiz.korma.geom.sine
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import kotlin.math.absoluteValue
import kotlin.math.sign

class Ship(parent: Container): Container() {
    private val body = parent.solidRect(40, 15, Colors.LIGHTBLUE)
        .position(300, 100)
        .registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!


    fun update(stage: Stage, flightAssistText: Text){
        body.wrapInView()
        body.applyDrag()
        stage.applyControls(body, flightAssistText)
    }

    private fun Stage.applyControls(
        ship: Body,
        flightAssistText: Text
    ) {
        var thrustInput = false
        var torqueInput = false
        if (input.keys.pressing(Key.UP) || input.keys.pressing(Key.W)) {
            thrustInput = true
            ship.thrustForward(forwardThrust)
        }
        if (input.keys.pressing(Key.DOWN) || input.keys.pressing(Key.S)) {
            thrustInput = true
            ship.thrustBackward(fineThrust)
        }
        if (input.keys.pressing(Key.E) || input.keys.pressing(Key.PAGE_DOWN)) {
            thrustInput = true
            ship.thrustRight(fineThrust)
        }
        if (input.keys.pressing(Key.Q) || input.keys.pressing(Key.DELETE)) {
            thrustInput = true
            ship.thrustLeft(fineThrust)
        }
        if (input.keys.pressing(Key.RIGHT) || input.keys.pressing(Key.D)) {
            torqueInput = true
            ship.torqueRight(angularThrust)
        }
        if (input.keys.pressing(Key.LEFT) || input.keys.pressing(Key.A)) {
            torqueInput = true
            ship.torqueLeft(angularThrust)
        }
        if (input.keys.justPressed(Key.X)) {
            if (flightAssist) {
                flightAssist = false
                flightAssistText.text = "'X' FA: OFF"
                flightAssistText.color = Colors.RED
            } else {
                flightAssist = true
                flightAssistText.text = "'X' FA: ON"
                flightAssistText.color = Colors.DARKGREEN
            }
        }
        ship.flightAssist(thrustInput, torqueInput, toggled = flightAssist)
    }

    private fun Body.flightAssist(
        thrustInput: Boolean,
        torqueInput: Boolean,
        toggled: Boolean
    ) {
        if (toggled) {
            if (!thrustInput && linearVelocity.lengthSquared() > 0) {
                val forward = Vec2(angle.cosine.toFloat(), angle.sine.toFloat())
                val backward = forward.negate()
                val right = Vec2(-angle.sine.toFloat(), angle.cosine.toFloat())
                val left = right.negate()

                val shipSpeed = linearVelocity.length()
                val shipVelocityDirection = linearVelocity / shipSpeed

                if (Vec2.dot(forward, shipVelocityDirection) < 0) thrustForward(Vec2.dot(forward, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(backward, shipVelocityDirection) < 0) thrustBackward(Vec2.dot(backward, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(right, shipVelocityDirection) < 0) thrustRight(Vec2.dot(right, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(left, shipVelocityDirection) < 0) thrustLeft(Vec2.dot(left, shipVelocityDirection) * -fineThrust)
            }

            if (!torqueInput && angularVelocity.absoluteValue > 0) {
                if (angularVelocity > 0) torqueLeft(angularThrust)
                if (angularVelocity < 0) torqueRight(angularThrust)
            }
        }
    }

    private fun Body.thrustForward(thrustAmount: Float) {
        val thrustDirection = Vec2(angle.cosine.toFloat(), angle.sine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        applyForceToCenter(calculateLimitedThrustVector(thrustVector))
    }


    private fun Body.thrustBackward(thrustAmount: Float) {
        val thrustDirection = Vec2(angle.cosine.toFloat(), angle.sine.toFloat())
        val thrustVector = thrustDirection * -thrustAmount

        applyForceToCenter(calculateLimitedThrustVector(thrustVector))
    }

    private fun Body.thrustRight(thrustAmount: Float) {
        val thrustDirection = Vec2(-angle.sine.toFloat(), angle.cosine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        applyForceToCenter(calculateLimitedThrustVector(thrustVector))
    }

    private fun Body.thrustLeft(thrustAmount: Float) {
        val thrustDirection = Vec2(angle.sine.toFloat(), -angle.cosine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        applyForceToCenter(calculateLimitedThrustVector(thrustVector))
    }

    private fun Body.torqueRight(angularThrust: Float) {
        applyTorque(calculateLimitedAngularThrust(angularThrust))
    }

    private fun Body.torqueLeft(angularThrust: Float) {
        applyTorque(calculateLimitedAngularThrust(-angularThrust))
    }

    private fun Body.calculateLimitedAngularThrust(angularThrust: Float): Float {
        if (angularVelocity.absoluteValue > maxAngularVelocity
            && ((angularVelocity > 0 && angularThrust > 0) || (angularVelocity < 0 && angularThrust < 0))
        ) return 0f

        return angularThrust
    }

    private fun Body.calculateLimitedThrustVector(thrustVector: Vec2): Vec2 {
        val thrustAmount = thrustVector.length()

        val thrustDirection = thrustVector / thrustAmount
        val fullThrustVector = thrustDirection * thrustAmount
        val shipSpeed = linearVelocity.length()
        val shipVelocityDirection = linearVelocity / shipSpeed

        //calculate limited thrust
        val amountOfThrustInDirectionOfVelocity = thrustAmount * Vec2.dot(thrustDirection, shipVelocityDirection)
        val thrustInDirectionOfMaxVelocity = shipVelocityDirection * amountOfThrustInDirectionOfVelocity
        val limitedThrustVector = fullThrustVector - thrustInDirectionOfMaxVelocity
        val isThrustInDirectionOfMaxVelocity = sign(Vec2.dot(shipVelocityDirection, thrustDirection)) > 0

        return if (shipSpeed > maxLinearVel && isThrustInDirectionOfMaxVelocity) limitedThrustVector else thrustDirection.mul(thrustAmount)
    }

    private fun Body.applyDrag() {
        //Linear Drag
        if (linearVelocity.lengthSquared() > 0f) {
            applyForceToCenter(linearVelocity.mul(-0.005f * linearVelocity.lengthSquared()))
        }

        //Angular Drag
        if (angularVelocity.absoluteValue > 0f) {
            applyTorque(sign(angularVelocity) * -0.005f)
        }
    }
}