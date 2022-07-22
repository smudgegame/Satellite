import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.nearestBox2dWorld
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.shape.buildVectorPath
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.pooling.arrays.Vec2ArrayPool
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Asteroid(mainStage: Stage, radius: Int) : Container() {
    private val _radius = radius
    private val divisions = 8

    private val offsetScale  = 0.05 * radius

    private val asteroidBody = mainStage.container {
        shapeView(buildVectorPath {
            createAsteroidVertices().forEach { (x, y) -> lineTo( x.toDouble(),  y.toDouble()) }
        }, Colors.WHITE, Colors.TRANSPARENT_BLACK, 2.0)
    }.position(100, 100)
        .registerBodyWithFixture(shape = createBoundingPolygon(), type = BodyType.STATIC, friction = 2f, gravityScale = 0f).body!!

    init {
        mainStage.addUpdater {
            asteroidBody.wrapInView()
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
            val variance = (-15..15).random()

            Pair((_radius + variance)* cos(angle),(_radius + variance) * sin(angle)).also{if ( i == 0) firstVert = it }
        }
    }

    private fun createBoundingPolygon(): PolygonShape {
        val vertPool = Vec2ArrayPool()
        val boundingShape = PolygonShape()
        val scale = nearestBox2dWorld.customScale.toFloat()
        val vectors = createAsteroidVertices().map { (x, y) -> Vec2(x.toFloat(), y.toFloat()) / scale }.toTypedArray()
        boundingShape.set(vectors, divisions, vertPool, null)

        return boundingShape
    }
}