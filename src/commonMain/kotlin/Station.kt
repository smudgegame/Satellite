import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Rectangle
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef

class Station(container: Container, x: Int, y: Int, rotation: Angle = Angle.ZERO) : Container() {

    init {
        val body = container.container {
            solidRect(200, 20, Colors.WHITE).position(-100, 10)
            solidRect(50, 10, Colors.GREEN).position(-25, 0)
        }
            .position(x, y)
            .registerBodyWithFixture(shape = BoxShape(Rectangle(-100, 10, 200, 20)/nearestBox2dWorld.customScale), type = BodyType.STATIC)
            .rotation(rotation)
            .body!!

        landingSites.add(body.createFixture(
            FixtureDef().apply {
                shape = BoxShape(Rectangle(25, 0, 50, 10)/nearestBox2dWorld.customScale)
                isSensor = true
            }
        )!!)

    }

}