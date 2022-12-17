import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.shape.buildVectorPath
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.joints.WeldJointDef
import org.jbox2d.pooling.arrays.Vec2ArrayPool
import kotlin.math.abs

var score = 0.0

var isDestroyed = false

const val fuelCapacity = 100f
var fuel = 100f

const val healthCapacity = 100f
var health = 100f

var docked = false

class Ship(mainStage: Stage) : Container() {
    private var landedStation: Body? = null
    private var parentJoint: Joint? = null
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

    private val shipShapeView = ShapeView(buildVectorPath {
        vertices.forEach { (x, y) -> lineTo( 1.15 * x.toDouble(),  1.15 * y.toDouble()) }
    }, Colors.WHITE, Colors.TRANSPARENT_BLACK, 4.0).position(-1.5,4.0)

    private val shipBoundingPolygon = createBoundingPolygon()

    private val shipBody = mainStage.container {
        addChild(shipShapeView)
        solidRect(27, 10, Colors.GREEN).position(0, -4)
    }.position(300, 100)
        .rotation(Angle.Companion.fromDegrees(90))
        .registerBodyWithFixture(shape = shipBoundingPolygon, type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!


    init {
        Controls(this, shipBody, mainStage)
        mainStage.addUpdater {
            shipBody.wrapInView(mainStage, nearestBox2dWorld.customScale.toFloat())
            shipBody.applyDrag()
            consumeFuel()
            attemptLanding()
            if (!docked){
                landedStation = null
                parentJoint = null
            }
        }

        landingGear = shipBody.createFixture(
            FixtureDef().apply
            {
                shape = BoxShape(Rectangle(0, -10, 27, 10) / nearestBox2dWorld.customScale)
                isSensor = true
            })!!
    }

    private fun consumeFuel() {
        if (thrustInput && fuel != 0f && !unlimitedFuel) {
            fuel -= 0.05f
            if (fuel <= 0f) {
                fuel = 0f
            }
        }
    }

    private fun applyDamage(){
        if(health > 0 && !invincible){
            health -= 10
            println("Damage")
        }
        else if(health <= 0 && !invincible){
           isDestroyed = true
        }
    }

    private fun checkDestroyed(){
        if (isDestroyed){
            nearestBox2dWorld.destroyBody(shipBody)
        }
    }

    fun onCollide(bodyA: Body, bodyB: Body, fixA: Fixture, fixB: Fixture) {
        when {
            bodyA == shipBody && fixA != landingGear && !landingSites.contains(fixB) -> applyDamage()
            fixA == landingGear && landingSites.contains(fixB) -> landedStation = bodyB
        }
    }

    fun onCollideExit(bodyA: Body, bodyB: Body, fixA: Fixture, fixB: Fixture) {
        if (!docked && (((landingSites.contains(fixA) && fixB == landingGear) || (fixA == landingGear && landingSites.contains(fixB))))) {
            docked = false
            landedStation = null
            parentJoint = null
        }
    }

    fun undock() {
        if (docked) {
            shipBody.world.destroyJoint(parentJoint)
            docked = false
        }
    }

    private fun attemptLanding() {
        if(landedStation != null && parentJoint == null) {
            val diff = abs(landedStation!!.angleDegrees - shipBody.angleDegrees)
            if (diff < 30) {
                val weldJointDef = WeldJointDef()
                weldJointDef.initialize(shipBody, landedStation!!, shipBody.position)
                weldJointDef.referenceAngleDegrees = 0f
                parentJoint = shipBody.world.createJoint(weldJointDef)!!
                docked = true
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