import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.shape.buildVectorPath
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.forEachFixture
import org.jbox2d.pooling.arrays.Vec2ArrayPool

class Ship(mainStage: Stage) : Container() {
    private val vertices = listOf<Pair<Number, Number>>(
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
            vertices.forEach { (x, y) -> lineTo(x.toDouble(), y.toDouble()) }
        }, Colors.WHITE, Colors.TRANSPARENT_BLACK, 4.0)
        solidRect(10, 27, Colors.GREEN).position(-10, 0)
    }.position(300, 100)
        .registerBodyWithFixture(shape = createBoundingPolygon(), type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!

    init {
        Controls(shipBody, mainStage)
        mainStage.addUpdater {
            shipBody.wrapInView()
            shipBody.applyDrag()
        }

        val sensorFixture = shipBody.createFixture(
            FixtureDef().apply {
                shape = BoxShape(Rectangle(-10, 0, 10, 27) / nearestBox2dWorld.customScale)
                isSensor = true
            })


        shipBody.world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                when {
                    contact.m_fixtureA == sensorFixture -> println("Landing")
                    contact.m_fixtureA?.m_body == shipBody -> println("Contact")
                    else  -> println("Ignoring")
                }
            }

            override fun endContact(contact: Contact) {}
            override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
            override fun preSolve(contact: Contact, oldManifold: Manifold) {
            }
        })
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