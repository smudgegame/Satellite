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

const val fuelCapacity = 100f
var fuel = 20f

class Ship(mainStage: Stage) : Container() {

    private val verticies = listOf<Pair<Number, Number>>(
        Pair(0, 0),
        Pair(50, 0),
        Pair(62.5, 12.5),
        Pair(62.5, 14.5),
        Pair(50.0, 27.0),
        Pair(0.0, 27.0),
        Pair(0.0, 0.0),
    )

    private val shipBody = mainStage.container {
        shapeView(buildVectorPath {
            verticies.forEach { (x, y) -> lineTo(x.toDouble(), y.toDouble()) }
        }, Colors.WHITE, Colors.TRANSPARENT_BLACK, 4.0)
    }.position(300, 100)
        .registerBodyWithFixture(shape = createBoundingPolygon(), type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!

    init {
        Controls(shipBody, mainStage)
        mainStage.addUpdater {
            shipBody.wrapInView(mainStage, nearestBox2dWorld.customScale.toFloat())
            shipBody.applyDrag()
            consumeFuel()
        }
    }

    private fun consumeFuel(){
        if (thrustInput && fuel != 0f){
            fuel -= 0.1f
            if(fuel <= 0f){
                fuel = 0f
            }
        }
    }

    private fun createBoundingPolygon(): PolygonShape {
        val vertPool = Vec2ArrayPool()
        val boundingShape = PolygonShape()
        val scale = nearestBox2dWorld.customScale.toFloat()
        val vectors = verticies.map { (x, y) -> Vec2(x.toFloat(), y.toFloat()) / scale }.toTypedArray()
        boundingShape.set(vectors, verticies.size, vertPool, null)

        return boundingShape
    }
}