import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.shape.buildVectorPath
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.joints.JointDef
import org.jbox2d.dynamics.joints.JointType
import org.jbox2d.dynamics.joints.WeldJointDef
import org.jbox2d.pooling.arrays.Vec2ArrayPool
import kotlin.math.abs

//--------- DEBUG VALUES ---------
const val fuelCapacity = 1000f
var fuel = 1000f

const val healthCapacity = 100f
var health = 100f
//--------------------------------

//--------- GAME VALUES ---------
//const val fuelCapacity = 100f
//var fuel = 100f
//
//const val healthCapacity = 100f
//var health = 100f
//-------------------------------

class Ship(mainStage: Stage) : Container() {
    private var landedStation: Body? = null
    private var parentJoin: Joint? = null
    private var landingGear: Fixture
    private val vertices = listOf<Pair<Number, Number>>(
        Pair(0, 0),
        Pair(0, -50),
        Pair(12.5, -62.5),
        Pair(14.5, -62.5),
        Pair(27.0, -50.0),
        Pair(27.0, 0.0),
        Pair(0.0, 0.0),
    )

    private val shipBody = mainStage.container {
        shapeView(buildVectorPath {
            vertices.forEach { (x, y) -> lineTo(x.toDouble(), y.toDouble()) }
        }, Colors.WHITE, Colors.TRANSPARENT_BLACK, 4.0)
        solidRect(27, 10, Colors.GREEN).position(0, -10)
    }.position(300, 100)
        .rotation(Angle.Companion.fromDegrees(90))
        .registerBodyWithFixture(shape = createBoundingPolygon(), type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!

    init {
        Controls(shipBody, mainStage)
        mainStage.addUpdater {
            shipBody.wrapInView(mainStage, nearestBox2dWorld.customScale.toFloat())
            shipBody.applyDrag()
            consumeFuel()
            attemptLanding()
        }

        landingGear = shipBody.createFixture(
            FixtureDef().apply
            {
                shape = BoxShape(Rectangle(0, -10, 27, 10) / nearestBox2dWorld.customScale)
                isSensor = true
            })!!
    }

    private fun consumeFuel() {
        if (thrustInput && fuel != 0f) {
            fuel -= 0.1f
            if (fuel <= 0f) {
                fuel = 0f
            }
        }
    }

    fun onCollide(bodyA: Body, bodyB: Body, fixA: Fixture, fixB: Fixture) {
        when {
            bodyA == bodyB -> println("same body")
            bodyA != shipBody && bodyB != shipBody -> {}
            bodyA == shipBody && fixA != landingGear && !landingSites.contains(fixB) -> damage()
            !landingSites.contains(fixA) && !landingSites.contains(fixB) -> println("Contact")
            bodyA == shipBody -> landedStation = bodyB
            else -> landedStation = bodyA
        }
    }

    private fun damage(){
        health -= 10
        println("Damage")
    }

    private fun attemptLanding() {
        if (parentJoin == null && landedStation != null) {
            landedStation!!.angleDegrees
            shipBody.angleDegrees
            val diff = abs(landedStation!!.angleDegrees - shipBody.angleDegrees)
            if (diff < 30) {
                parentJoin = shipBody.world.createJoint(WeldJointDef().apply {
                    bodyA = shipBody
                    bodyB = landedStation
                    referenceAngleDegrees = 0f
                    println("docked")
                })!!
            }
        }
    }

    private fun createBoundingPolygon(): PolygonShape {
        val vertPool = Vec2ArrayPool()
        val boundingShape = PolygonShape()
        val scale = nearestBox2dWorld.customScale.toFloat()
        val vectors = vertices.map { (x, y) -> Vec2(x.toFloat(), y.toFloat()) / scale }.toTypedArray()
        boundingShape.set(vectors, vertices.size, vertPool, null)

        return boundingShape
    }
}