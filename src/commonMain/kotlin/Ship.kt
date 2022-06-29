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
            thrustForward(ship, forwardThrust)
        }
        if (input.keys.pressing(Key.DOWN) || input.keys.pressing(Key.S)) {
            thrustInput = true
            thrustBackward(ship, fineThrust)
        }
        if (input.keys.pressing(Key.E) || input.keys.pressing(Key.PAGE_DOWN)) {
            thrustInput = true
            thrustRight(ship, fineThrust)
        }
        if (input.keys.pressing(Key.Q) || input.keys.pressing(Key.DELETE)) {
            thrustInput = true
            thrustLeft(ship, fineThrust)
        }
        if (input.keys.pressing(Key.RIGHT) || input.keys.pressing(Key.D)) {
            torqueInput = true
            torqueRight(ship, angularThrust)
        }
        if (input.keys.pressing(Key.LEFT) || input.keys.pressing(Key.A)) {
            torqueInput = true
            torqueLeft(ship, angularThrust)
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
        flightAssist(ship, thrustInput, torqueInput, toggled = flightAssist)
    }

    private fun flightAssist(
        body: Body,
        thrustInput: Boolean,
        torqueInput: Boolean,
        toggled: Boolean
    ) {
        if (toggled) {
            if (!thrustInput && body.linearVelocity.lengthSquared() > 0) {
                val forward = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
                val backward = forward.negate()
                val right = Vec2(-body.angle.sine.toFloat(), body.angle.cosine.toFloat())
                val left = right.negate()

                val shipSpeed = body.linearVelocity.length()
                val shipVelocityDirection = body.linearVelocity / shipSpeed

                if (Vec2.dot(forward, shipVelocityDirection) < 0) thrustForward(body, Vec2.dot(forward, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(backward, shipVelocityDirection) < 0) thrustBackward(body, Vec2.dot(backward, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(right, shipVelocityDirection) < 0) thrustRight(body, Vec2.dot(right, shipVelocityDirection) * -fineThrust)
                if (Vec2.dot(left, shipVelocityDirection) < 0) thrustLeft(body, Vec2.dot(left, shipVelocityDirection) * -fineThrust)
            }

            if (!torqueInput && body.angularVelocity.absoluteValue > 0) {
                if (body.angularVelocity > 0) torqueLeft(body, angularThrust)
                if (body.angularVelocity < 0) torqueRight(body, angularThrust)
            }
        }
    }

    private fun thrustForward(body: Body, thrustAmount: Float) {
        val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
    }


    private fun thrustBackward(body: Body, thrustAmount: Float) {
        val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
        val thrustVector = thrustDirection * -thrustAmount

        body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
    }

    private fun thrustRight(body: Body, thrustAmount: Float) {
        val thrustDirection = Vec2(-body.angle.sine.toFloat(), body.angle.cosine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
    }

    private fun thrustLeft(body: Body, thrustAmount: Float) {
        val thrustDirection = Vec2(body.angle.sine.toFloat(), -body.angle.cosine.toFloat())
        val thrustVector = thrustDirection * thrustAmount

        body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
    }

    private fun torqueRight(body: Body, angularThrust: Float) {
        body.applyTorque(calculateLimitedAngularThrust(body, angularThrust))
    }

    private fun torqueLeft(body: Body, angularThrust: Float) {
        body.applyTorque(calculateLimitedAngularThrust(body, -angularThrust))
    }

    private fun calculateLimitedAngularThrust(body: Body, angularThrust: Float): Float {
        if (body.angularVelocity.absoluteValue > maxAngularVelocity
            && ((body.angularVelocity > 0 && angularThrust > 0) || (body.angularVelocity < 0 && angularThrust < 0))
        ) return 0f

        return angularThrust
    }

    private fun calculateLimitedThrustVector(body: Body, thrustVector: Vec2): Vec2 {
        val thrustAmount = thrustVector.length()

        val thrustDirection = thrustVector / thrustAmount
        val fullThrustVector = thrustDirection * thrustAmount
        val shipSpeed = body.linearVelocity.length()
        val shipVelocityDirection = body.linearVelocity / shipSpeed

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