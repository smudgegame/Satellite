import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.position
import com.soywiz.korge.view.solidRect
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.cosine
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.sine
import org.jbox2d.common.MathUtils.Companion.clamp
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

val thrustAmount = .5f
val maxVelocity = 1f
val turnAmount = .1f
val maxTurn = 1f

suspend fun main() = Korge(
	width = 800, height = 800,
	quality = GameWindow.Quality.PERFORMANCE, title = "Satellite"
) {

	val ship = solidRect(100, 50, Colors.GREEN).position(300, 100).registerBodyWithFixture(type = BodyType.DYNAMIC, gravityScale = 0f)

	keys {
		down(Key.UP) {
			val x = clamp((ship.rotation.cosine * thrustAmount).toFloat(), -maxVelocity, maxVelocity)
			val y = clamp((ship.rotation.sine * thrustAmount).toFloat(), -maxVelocity, maxVelocity)
			ship.body!!.linearVelocityX = ship.body!!.linearVelocityX + x
			ship.body!!.linearVelocityY = ship.body!!.linearVelocityY + y
		}
		down(Key.LEFT) { ship.body!!.angularVelocity=  clamp(ship.body!!.angularVelocity - turnAmount, -maxTurn, maxTurn) }
		down(Key.RIGHT) { ship.body!!.angularVelocity = clamp(ship.body!!.angularVelocity + turnAmount, -maxTurn, maxTurn)}
	}

	addUpdater {
		val oldVelocity = ship.body!!.linearVelocity
		val oldAngularVel = ship.body!!.angularVelocity
		val updated = when {
			ship.x > 800 -> { ship.x = 1.0; true}
			ship.x < 0 -> {ship.x = 790.0; true}
			ship.y > 800 -> {ship.y = 1.0; true}
			ship.y < 0 -> {ship.y = 790.0; true}
			else -> false
		}
		if (updated){
			ship.body!!.linearVelocity = oldVelocity
			ship.body!!.angularVelocity = oldAngularVel
		}
	}


}

fun clamp(amount: Float, min: Float, max: Float): Float {
	return min(max, max(min, amount))
}

fun randomAngle(): Angle = Random.nextInt(0, 90).degrees