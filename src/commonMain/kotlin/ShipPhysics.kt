import com.soywiz.korma.geom.cosine
import com.soywiz.korma.geom.sine
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import kotlin.math.absoluteValue
import kotlin.math.sign

fun Body.flightAssist(
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

            if (Vec2.dot(backward, shipVelocityDirection) < 0) thrustForward(Vec2.dot(backward, shipVelocityDirection) * fineThrust)
            if (Vec2.dot(forward, shipVelocityDirection) < 0) thrustBackward(Vec2.dot(forward, shipVelocityDirection) * fineThrust)
            if (Vec2.dot(left, shipVelocityDirection) < 0) thrustRight(Vec2.dot(left, shipVelocityDirection) * fineThrust)
            if (Vec2.dot(right, shipVelocityDirection) < 0) thrustLeft(Vec2.dot(right, shipVelocityDirection) * fineThrust)
        }

        if (!torqueInput && angularVelocity.absoluteValue > 0) {
            if (angularVelocity > 0) torqueLeft(angularThrust)
            if (angularVelocity < 0) torqueRight(angularThrust)
        }
    }
}

fun Body.thrustForward(thrustAmount: Float) {
    val thrustVector = Vec2(angle.cosine.toFloat(), angle.sine.toFloat()) * thrustAmount

    applyForceToCenter(calculateLimitedThrustVector(thrustVector))
}

fun Body.thrustBackward(thrustAmount: Float) {
    val thrustVector = Vec2(-angle.cosine.toFloat(), -angle.sine.toFloat()) * thrustAmount

    applyForceToCenter(calculateLimitedThrustVector(thrustVector))
}

fun Body.thrustRight(thrustAmount: Float) {
    val thrustVector = Vec2(-angle.sine.toFloat(), angle.cosine.toFloat()) * thrustAmount

    applyForceToCenter(calculateLimitedThrustVector(thrustVector))
}

fun Body.thrustLeft(thrustAmount: Float) {
    val thrustVector = Vec2(angle.sine.toFloat(), -angle.cosine.toFloat()) * thrustAmount

    applyForceToCenter(calculateLimitedThrustVector(thrustVector))
}

fun Body.torqueRight(angularThrust: Float) {
    applyTorque(calculateLimitedAngularThrust(angularThrust))
}

fun Body.torqueLeft(angularThrust: Float) {
    applyTorque(calculateLimitedAngularThrust(-angularThrust))
}

fun Body.calculateLimitedAngularThrust(angularThrust: Float): Float {
    if (angularVelocity.absoluteValue > maxAngularVelocity
        && ((angularVelocity > 0 && angularThrust > 0) || (angularVelocity < 0 && angularThrust < 0))
    ) return 0f

    return angularThrust
}

fun Body.calculateLimitedThrustVector(thrustVector: Vec2): Vec2 {
    val thrustAmount = thrustVector.length()
    val thrustDirection = thrustVector / thrustAmount
    val fullThrustVector = thrustDirection * thrustAmount
    val shipSpeed = linearVelocity.length()
    val shipVelocityDirection = linearVelocity / shipSpeed

    //calculate limited thrust
    val amountOfThrustInDirectionOfVelocity = thrustAmount * (thrustDirection * shipVelocityDirection)
    val thrustInDirectionOfMaxVelocity = shipVelocityDirection * amountOfThrustInDirectionOfVelocity
    val limitedThrustVector = fullThrustVector - thrustInDirectionOfMaxVelocity
    val isThrustInDirectionOfMaxVelocity = sign(shipVelocityDirection * thrustDirection) > 0

    return if (shipSpeed > maxLinearVel && isThrustInDirectionOfMaxVelocity) limitedThrustVector else thrustDirection.mul(thrustAmount)
}

fun Body.applyDrag() {
    //Linear Drag
    if (linearVelocity.lengthSquared() > 0f) {
        applyForceToCenter(linearVelocity.mul(-0.005f * linearVelocity.lengthSquared()))
    }

    //Angular Drag
    if (angularVelocity.absoluteValue > 0f) {
        applyTorque(sign(angularVelocity) * -0.005f)
    }
}