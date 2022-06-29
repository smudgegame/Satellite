import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.*
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import kotlin.math.absoluteValue
import kotlin.math.sign

const val angularThrust = 1f
const val forwardThrust = 12f
const val fineThrust = 0.15f * forwardThrust

const val maxAngularVelocity = 1.5f
const val maxLinearVel = 5f

var flightAssist = false

suspend fun main() = Korge(
    width = 800, height = 800,
    quality = GameWindow.Quality.PERFORMANCE, title = "Satellite"
) {

    val ship = solidRect(40, 15, Colors.LIGHTBLUE)
        .position(300, 100)
        .registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!
//		.registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f, linearDamping = -0.005f, angularDamping = -0.005f).body!!

    //platform
    solidRect(200, 20, Colors.WHITE).position(200, 700).registerBodyWithFixture(type = BodyType.STATIC)

    //UI
    val flightAssistIndicator = solidRect(100, 25, Colors.WHITE).position(700, 0)
    addChild(flightAssistIndicator)

    val flightAssistText = text("'X' FA: OFF", 50 * 0.25, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }

    addUpdater {

        wrapBodyInView(ship)
        applyDragToBody(ship)

        applyControls(ship, flightAssistText)
    }
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

fun flightAssist(
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

fun thrustForward(body: Body, thrustAmount: Float) {
    val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
    val thrustVector = thrustDirection * thrustAmount

    body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}


fun thrustBackward(body: Body, thrustAmount: Float) {
    val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
    val thrustVector = thrustDirection * -thrustAmount

    body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun thrustRight(body: Body, thrustAmount: Float) {
    val thrustDirection = Vec2(-body.angle.sine.toFloat(), body.angle.cosine.toFloat())
    val thrustVector = thrustDirection * thrustAmount

    body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun thrustLeft(body: Body, thrustAmount: Float) {
    val thrustDirection = Vec2(body.angle.sine.toFloat(), -body.angle.cosine.toFloat())
    val thrustVector = thrustDirection * thrustAmount

    body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun torqueRight(body: Body, angularThrust: Float) {
    body.applyTorque(calculateLimitedAngularThrust(body, angularThrust))
}

fun torqueLeft(body: Body, angularThrust: Float) {
    body.applyTorque(calculateLimitedAngularThrust(body, -angularThrust))
}

fun calculateLimitedAngularThrust(body: Body, angularThrust: Float): Float {
    if (body.angularVelocity.absoluteValue > maxAngularVelocity
        && ((body.angularVelocity > 0 && angularThrust > 0) || (body.angularVelocity < 0 && angularThrust < 0))
    ) return 0f

    return angularThrust
}

fun calculateLimitedThrustVector(body: Body, thrustVector: Vec2): Vec2 {
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

fun applyDragToBody(body: Body) {
    //Linear Drag
    if (body.linearVelocity.lengthSquared() > 0f) {
        body.applyForceToCenter(body.linearVelocity.mul(-0.005f * body.linearVelocity.lengthSquared()))
    }

    //Angular Drag
    if (body.angularVelocity.absoluteValue > 0f) {
        body.applyTorque(sign(body.angularVelocity) * -0.005f)
    }
}

fun wrapBodyInView(body: Body) {
    val leftBound = 0f
    val rightBound = 40f
    val upBound = 0f
    val downBound = 40f

    when {
        body.position.x > rightBound -> body.setTransform(Vec2(0f, body.position.y), body.angle)
        body.position.y > downBound -> body.setTransform(Vec2(body.position.x, 0f), body.angle)
        body.position.x < leftBound -> body.setTransform(Vec2(40f, body.position.y), body.angle)
        body.position.y < upBound -> body.setTransform(Vec2(body.position.x, 40f), body.angle)
    }
}
