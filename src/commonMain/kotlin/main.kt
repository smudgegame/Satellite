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

suspend fun main() = Korge(
	width = 800, height = 800,
	quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!"
) {

	val angularThrust = 4f
	val forwardThrust = 15f
	val reverseThrust = -0.25f * forwardThrust

	val rec = solidRect(40, 15, Colors.GREEN).position(300, 100).registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f)
	val ship = rec.body!!

	addUpdater {
		wrapBodyInView(ship)

		//Linear Drag
		if(ship.linearVelocity.lengthSquared() > 0f){
			ship.applyForceToCenter(Vec2(ship.linearVelocityX / -4, ship.linearVelocityY / -4))
		}

		//Angular Drag
		if(ship.angularVelocity.absoluteValue > 0f){
			ship.applyTorque(sign(ship.angularVelocity) / -1.5f)
		}

		//Controls
		if (input.keys.pressing(Key.UP)){
			ship.applyForceToCenter(calculateThrustVector(ship, forwardThrust))
		}
		if (input.keys.pressing(Key.DOWN)){
			ship.applyForceToCenter(calculateThrustVector(ship, reverseThrust))
		}
		if(input.keys.pressing(Key.RIGHT)){
			ship.applyTorque(calculateAngularThrust(ship, angularThrust))
		}
		if(input.keys.pressing(Key.LEFT)){
			ship.applyTorque(calculateAngularThrust(ship, -angularThrust))
		}
	}

	solidRect(200,20, Colors.WHITE).position (200,700).registerBodyWithFixture(type = BodyType.STATIC)
}

fun calculateAngularThrust(body: Body, angularThrust: Float): Float{
	val maxAngularVelocity = 1.5f

	if(body.angularVelocity.absoluteValue > maxAngularVelocity){
		if ((body.angularVelocity > 0 && angularThrust > 0) || (body.angularVelocity < 0 && angularThrust < 0)){
			return 0f
		}
	}

	return angularThrust
}

fun calculateThrustVector(body: Body, thrustAmount: Float): Vec2 {
	val maxLinearVel = 5f

	var thrustDirection = Vec2(body.angle.cosine.toFloat(), body.angle.sine.toFloat())
	var shipSpeed = body.linearVelocity.length()
	var shipVelocityDirection = body.linearVelocity.mul(1 / shipSpeed)

	//calculate limited thrust
	var amountOfThrustInDirectionOfVelocity = thrustAmount * Vec2.dot(thrustDirection, shipVelocityDirection)
	var thrustInDirectionOfMaxVelocity = shipVelocityDirection.mul(amountOfThrustInDirectionOfVelocity)
	var limitedThrustVector = thrustDirection.sub(thrustInDirectionOfMaxVelocity)
	var isThrustInDirectionOfMaxVelocity = sign(thrustAmount) * sign(Vec2.dot(shipVelocityDirection, thrustDirection)) > 0

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
