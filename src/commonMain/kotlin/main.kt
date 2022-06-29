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

    //UI
    val flightAssistIndicator = solidRect(100, 25, Colors.WHITE).position(700, 0)
    addChild(flightAssistIndicator)

    val flightAssistText = text("'X' FA: OFF", 50 * 0.25, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }

    addUpdater {
        ship.update(this, flightAssistText)
    }
}

fun Body.wrapInView() {
    val leftBound = 0f
    val rightBound = 40f
    val upBound = 0f
    val downBound = 40f

    when {
        position.x > rightBound -> setTransform(Vec2(0f, position.y), angle)
        position.y > downBound -> setTransform(Vec2(position.x, 0f), angle)
        position.x < leftBound -> setTransform(Vec2(40f, position.y), angle)
        position.y < upBound -> setTransform(Vec2(position.x, 40f), angle)
    }
}
