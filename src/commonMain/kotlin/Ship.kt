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

var isDestroyed = false

const val fuelCapacity = 100f
var fuel = 100f
var unlimitedFuel = true

const val healthCapacity = 100f
var health = 10f
var invincible = false

class Ship(mainStage: Stage) : Container() {
    private var landedStation: Body? = null
    private var parentJoint: Joint? = null
    private var landingGear: Fixture
    private var undocking = false
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
        Controls(this, shipBody, mainStage)
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
        if (thrustInput && fuel != 0f && !unlimitedFuel) {
            fuel -= 0.1f
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
            bodyA == bodyB -> println("same body")
            bodyA != shipBody && bodyB != shipBody -> {}
            bodyA == shipBody && fixA != landingGear && !landingSites.contains(fixB) -> applyDamage()
            !landingSites.contains(fixA) && !landingSites.contains(fixB) -> println("Contact")
            bodyA == shipBody -> landedStation = bodyB
            else -> landedStation = bodyA
        }
    }

    fun onCollideExit(bodyA: Body, bodyB: Body, fixA: Fixture, fixB: Fixture) {
        if (undocking && landedStation != null && (bodyA == landedStation || bodyB == landedStation)) {
            println("Undocking")
            undocking = false
            landedStation = null
        }
    }

    fun undock() {
        if (parentJoint != null) {
            shipBody.world.destroyJoint(parentJoint)
            parentJoint = null
            undocking = true
        }
    }

    private fun attemptLanding() {
        if (!undocking && parentJoint == null && landedStation != null) {
            landedStation!!.angleDegrees
            shipBody.angleDegrees
            val diff = abs(landedStation!!.angleDegrees - shipBody.angleDegrees)
            if (diff < 30) {
                parentJoint = shipBody.world.createJoint(WeldJointDef().apply {
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