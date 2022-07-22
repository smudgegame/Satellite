import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Rectangle
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

const val WINDOW_WIDTH = 1600
const val WINDOW_HEIGHT = 900


const val angularThrust = 4f
const val forwardThrust = 25f
const val fineThrust = 0.5f * forwardThrust

const val maxAngularVelocity = 5f
const val maxLinearVel = 20f

//const val angularThrust = 1f
//const val forwardThrust = 12f
//const val fineThrust = 0.15f * forwardThrust
//
//const val maxAngularVelocity = 1.5f
//const val maxLinearVel = 5f

var flightAssist = false

suspend fun main() = Korge(
    width = WINDOW_WIDTH, height = WINDOW_HEIGHT,
    quality = GameWindow.Quality.PERFORMANCE, title = "Satellite"
) {
    val ship = Ship(this)
    generateAsteroids(this,10)
    val orb = Orb(this)


    //platform
    solidRect(20, 200, Colors.WHITE).position(500, 100).registerBodyWithFixture(shape = BoxShape(Rectangle(2,2,16,196) / nearestBox2dWorld.customScale), type = BodyType.STATIC)

    createUI(this)
}

fun generateAsteroids(mainStage: Stage,amount: Int) {
    for (i in 0..amount){
        val size = (20..50).random()
        Asteroid(mainStage,size)
    }
}

fun Body.wrapInView(mainStage: Stage, worldScale: Float) {
    val leftBound = -2f
    val rightBound = (mainStage.width + 2).toFloat() / worldScale
    val upBound = -2f
    val downBound = (mainStage.height + 2f).toFloat() / worldScale

    when {
        position.x > rightBound -> setTransform(Vec2(leftBound, position.y), angle)
        position.y > downBound -> setTransform(Vec2(position.x, upBound), angle)
        position.x < leftBound -> setTransform(Vec2(rightBound, position.y), angle)
        position.y < upBound -> setTransform(Vec2(position.x, downBound), angle)
    }
}
