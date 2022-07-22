import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Rectangle
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Fixture

const val angularThrust = 5f
const val forwardThrust = 20f
const val fineThrust = 0.5f * forwardThrust

const val maxAngularVelocity = 3f
const val maxLinearVel = 10f

//const val angularThrust = 1f
//const val forwardThrust = 12f
//const val fineThrust = 0.15f * forwardThrust
//
//const val maxAngularVelocity = 1.5f
//const val maxLinearVel = 5f

var flightAssist = true

val landingSites = mutableListOf<Fixture>()

suspend fun main() = Korge(
    width = 800, height = 800,
    quality = GameWindow.Quality.PERFORMANCE, title = "Satellite"
) {
    val ship = Ship(this)
//    val orb = Orb(this)

    Station(this, 350, 200, Angle.fromDegrees(0))
//    Station(this, 200, 100, Angle.fromDegrees(90))
//    Station(this, 500, 100, Angle.fromDegrees(-90))

    createUI(this)
}

fun Body.wrapInView() {
    val leftBound = -2f
    val rightBound = 42f
    val upBound = -2f
    val downBound = 42f

    when {
        position.x > rightBound -> setTransform(Vec2(leftBound, position.y), angle)
        position.y > downBound -> setTransform(Vec2(position.x, upBound), angle)
        position.x < leftBound -> setTransform(Vec2(rightBound, position.y), angle)
        position.y < upBound -> setTransform(Vec2(position.x, downBound), angle)
    }
}
