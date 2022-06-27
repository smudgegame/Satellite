import com.soywiz.klogger.Console
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

/**
 * Interactive sample for the integrated [Box-2D](http://www.jbox2d.org) physic lib.
 *
 * Click on anywhere to spawn a new box!
 */

const val angularThrust = 4f
const val forwardThrust = 12f
const val fineThrust = 0.25f * forwardThrust

suspend fun main() = Korge(
	width = 800, height = 800,
	quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!"
) {

	val rec = solidRect(40, 15, Colors.GREEN).position(300, 100).registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f)
	val ship = rec.body!!

	addUpdater {
		wrapBodyInView(ship)

		//Linear Drag
		if(ship.linearVelocity.lengthSquared() > 0f){
			ship.applyForceToCenter(ship.linearVelocity.mul(-0.2f))
		}

		//Angular Drag
		if(ship.angularVelocity.absoluteValue > 0f){
			ship.applyTorque(sign(ship.angularVelocity) / -1.5f)
		}

		//Ship Thrust Controls
		if (input.keys.pressing(Key.UP ) || input.keys.pressing(Key.W ) ){
			thrustForward(ship)
		}
		if (input.keys.pressing(Key.DOWN ) || input.keys.pressing(Key.S)){
			thrustBackward(ship)
		}
		if(input.keys.pressing(Key.E)){
			thrustRight(ship)
		}
		if(input.keys.pressing(Key.Q)){
			thrustLeft(ship)
		}
		if(input.keys.pressing(Key.RIGHT) ||  input.keys.pressing(Key.D)){
			torqueRight(ship)
		}
		if(input.keys.pressing(Key.LEFT) || input.keys.pressing(Key.A)){
			torqueLeft(ship)
		}

		Console.info(ship.linearVelocity.length())
	}


	solidRect(200,20, Colors.WHITE).position (200,700).registerBodyWithFixture(type = BodyType.STATIC)
}

fun thrustForward(body: Body){
	val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
	val thrustVector = thrustDirection.mul(forwardThrust)

	body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun thrustBackward(body: Body){
	val thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
	val thrustVector = thrustDirection.mul(-fineThrust)

	body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun thrustRight(body: Body){
	val thrustDirection = Vec2(-body.angle.sine.toFloat(), body.angle.cosine.toFloat())
	val thrustVector = thrustDirection.mul(fineThrust)

	body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun thrustLeft(body: Body){
	val thrustDirection = Vec2(body.angle.sine.toFloat(), -body.angle.cosine.toFloat())
	val thrustVector = thrustDirection.mul(fineThrust)

	body.applyForceToCenter(calculateLimitedThrustVector(body, thrustVector))
}

fun torqueRight(body: Body){
	body.applyTorque(calculateLimitedAngularThrust(body, angularThrust))
}

fun torqueLeft(body: Body){
	body.applyTorque(calculateLimitedAngularThrust(body, -angularThrust))
}

fun calculateLimitedAngularThrust(body: Body, angularThrust: Float): Float{
	val maxAngularVelocity = 1.5f

	if(body.angularVelocity.absoluteValue > maxAngularVelocity){
		if ((body.angularVelocity > 0 && angularThrust > 0) || (body.angularVelocity < 0 && angularThrust < 0)){
			return 0f
		}
	}

	return angularThrust
}

fun calculateLimitedThrustVector(body: Body, thrustVector: Vec2): Vec2 {
	val maxLinearVel = 5f

	val thrustAmount = thrustVector.length()

	val thrustDirection = thrustVector.mul(1/thrustAmount)
	val fullThrustVector = thrustDirection.mul(thrustAmount)
	val shipSpeed = body.linearVelocity.length()
	val shipVelocityDirection = body.linearVelocity.mul(1 / shipSpeed)

	//calculate limited thrust
	val amountOfThrustInDirectionOfVelocity = thrustAmount * Vec2.dot(thrustDirection, shipVelocityDirection)
	val thrustInDirectionOfMaxVelocity = shipVelocityDirection.mul(amountOfThrustInDirectionOfVelocity)
	val limitedThrustVector = fullThrustVector.sub(thrustInDirectionOfMaxVelocity)
	val isThrustInDirectionOfMaxVelocity = sign(thrustAmount) * sign(Vec2.dot(shipVelocityDirection, thrustDirection)) > 0

	return if (shipSpeed > maxLinearVel &&  isThrustInDirectionOfMaxVelocity) {
		limitedThrustVector
	} else {
		thrustDirection.mul(thrustAmount)
	}
}

fun wrapBodyInView(body: Body){
	if(body.position.x > 41f){
		body.setTransform(Vec2(0f, body.position.y), body.angle)
	}

	if(body.position.y > 41f){
		body.setTransform(Vec2(body.position.x,0f), body.angle)
	}

	if(body.position.x < -1f){
		body.setTransform(Vec2(40f, body.position.y), body.angle)
	}

	if(body.position.y < -1f){
		body.setTransform(Vec2(body.position.x,40f), body.angle)
	}
}

//fun applyDragToBody(body: Body){
//	val A = 1.0f
//	val Cd = 0.05f
//	val v = Vec2(body.linearVelocityX, body.linearVelocityY)
//	val speed = v.length()
//	val dragForce = 0.5f * 1 * speed * speed * Cd * A
//	val dragDirection = v.mulLocal(dragForce / speed)
//
//	body.applyForceToCenter(dragDirection.negateLocal())
//}
