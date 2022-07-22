import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType

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
    val ship = Ship(this)

    //platform
    solidRect(200, 20, Colors.WHITE).position(200, 700).registerBodyWithFixture(type = BodyType.STATIC)

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
