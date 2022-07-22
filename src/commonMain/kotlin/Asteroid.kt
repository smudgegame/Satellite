import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.nearestBox2dWorld
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.shape.buildVectorPath
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.pooling.arrays.Vec2ArrayPool
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Asteroid(mainStage: Stage, radius: Int) : Container() {
    private val _radius = radius
    private val divisions = 8
    private val vertices = createAsteroidVertices()

    //Random initialization
    private var initialized = false
    private val velocityMagnitude = 100
    private val randomInitialVelocity = Vec2((-velocityMagnitude..velocityMagnitude).random().toFloat(),(-velocityMagnitude..velocityMagnitude).random().toFloat())
    private val initialAngularImpulse = (-100..100).random() * Random.nextFloat()
    private val randomXPosition = (0..WINDOW_WIDTH).random()
    private val randomYPosition = (0..WINDOW_HEIGHT).random()

    private val asteroidBody = mainStage.container {
        shapeView(buildVectorPath {
            vertices.forEach { (x, y) -> lineTo( 1.1 * x.toDouble(),  1.1 * y.toDouble()) }
        }, Colors["#9b9b9b"], Colors.TRANSPARENT_BLACK, 2.0)
    }.position(randomXPosition, randomYPosition)
        .registerBodyWithFixture(shape = createBoundingPolygon(), type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!

    init {
        mainStage.addUpdater {
            asteroidBody.wrapInView(mainStage, nearestBox2dWorld.customScale.toFloat())
        }
    }

    private fun createAsteroidVertices(): List<Pair<Number, Number>>  {
        val angleStep = 2 * PI / divisions
        var firstVert = Pair(0.0,0.0)

        return (0..divisions).map { i ->
            val angle = i * angleStep

            if (i == divisions){
                return@map firstVert
            }
            val variance = (-20..20).random()

            Pair((_radius + variance)* cos(angle),(_radius + variance) * sin(angle)).also{if ( i == 0) firstVert = it }
        }
    }

    private fun createBoundingPolygon(): PolygonShape {
        val vertPool = Vec2ArrayPool()
        val boundingShape = PolygonShape()
        val scale = nearestBox2dWorld.customScale.toFloat()
        val vectors = vertices.map { (x, y) -> Vec2(x.toFloat(), y.toFloat()) / scale }.toTypedArray()
        boundingShape.set(vectors, divisions, vertPool, null)

        return boundingShape
    }

}