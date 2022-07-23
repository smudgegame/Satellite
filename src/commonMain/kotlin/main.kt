import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.nearestBox2dWorld
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.onCollisionExit
import com.soywiz.korgw.GameWindow
import com.soywiz.korma.geom.Angle
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.callbacks.DestructionListener
import org.jbox2d.collision.Manifold
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.contacts.Contact


//------- DEBUG VALUES --------------
val debugMode = true

const val WINDOW_WIDTH = 800
const val WINDOW_HEIGHT = 450

const val angularThrust = 25f
const val forwardThrust = 60f
const val fineThrust = 0.75f * forwardThrust

const val maxAngularVelocity = 4f
const val maxLinearVel = 18f

var unlimitedFuel = true
var invincible = true

var flightAssist = true

const val uiScale = 0.5
//------------------------------------


//---------- Game Values -------------
//val debugMode = false
//
//const val WINDOW_WIDTH = 1600
//const val WINDOW_HEIGHT = 900
//
//const val angularThrust = 10f
//const val forwardThrust = 30f
//const val fineThrust = 0.15f * forwardThrust
//
//const val maxAngularVelocity = 1.5f
//const val maxLinearVel = 5f
//
//var unlimitedFuel = false
//var invincible = false
//
//var flightAssist = false
//const val uiScale = 1
//------------------------------------

val landingSites = mutableListOf<Fixture>()

suspend fun main() = Korge(
    width = WINDOW_WIDTH, height = WINDOW_HEIGHT,
    quality = GameWindow.Quality.PERFORMANCE, title = "Satellite"
) {
    val ship = Ship(this)

    if (!debugMode){
        generateAsteroids(this, (15..20).random())
    }
    else{
        val orb = Orb(this)
        generateAsteroids(this, 5)
    }

    Station(this, 350, 200, Angle.fromDegrees(0))
    Station(this, 350, 200, Angle.fromDegrees(0))

    createUI(this)

    nearestBox2dWorld.setContactListener(object : ContactListener {
        override fun beginContact(contact: Contact) {
            val fixA = contact.m_fixtureA!!
            val fixB = contact.m_fixtureB!!
            val bodyA = fixA.m_body!!
            val bodyB = fixB.m_body!!
            ship.onCollide(bodyA, bodyB, fixA, fixB)
        }

        override fun endContact(contact: Contact) {
            val fixA = contact.m_fixtureA!!
            val fixB = contact.m_fixtureB!!
            val bodyA = fixA.m_body!!
            val bodyB = fixB.m_body!!
            ship.onCollideExit(bodyA, bodyB, fixA, fixB)
        }
        override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
        override fun preSolve(contact: Contact, oldManifold: Manifold) {}
    })
}



fun generateAsteroids(mainStage: Stage, amount: Int) {
    var size = 0
    for (i in 0..amount) {

        if (!debugMode){
            size = (20..50).random()
        }else{
            size = (10..25).random()
        }

        Asteroid(mainStage, size)
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
