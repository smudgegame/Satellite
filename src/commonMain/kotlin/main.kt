import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.PointArrayList
import com.soywiz.korma.geom.shape.Shape2d
import com.soywiz.korma.geom.vector.VectorPath
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

    val triangle = this.container()
    val trianglePath = VectorPath()
    trianglePath.lineTo(0.0,0.0)
    trianglePath.lineTo(2.0,0.0)
    trianglePath.lineTo(0.0,2.0)
    triangle.shapeView(trianglePath,fill = Colors.WHITE)

    //KTree Test
    //val myTree = resourcesVfs["ship.ktree"].readKTree(views)

    //platform
    solidRect(200, 20, Colors.WHITE).position(200, 700).registerBodyWithFixture(type = BodyType.STATIC)
    val chunk = solidRect(100,50, Colors.WHITE).position(100,100).registerBodyWithFixture(type = BodyType.DYNAMIC, gravityScale = 0f)

    //UI
    val flightAssistIndicator = solidRect(100, 25, Colors.WHITE).position(700, 0)
    addChild(flightAssistIndicator)

    val flightAssistText = text("'X' FA: OFF", 50 * 0.25, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }

    addUpdater {
        ship.update(this, flightAssistText)
        chunk.body!!.wrapInView()
    }
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
