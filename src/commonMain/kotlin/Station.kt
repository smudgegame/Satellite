import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import org.jbox2d.dynamics.BodyType

class Station(container: Container, x: Int, y: Int, rotation: Angle = Angle.ZERO) : Container() {

    init {
        container.container {
            solidRect(200, 20, Colors.WHITE).position(-100, -10)
            solidRect(50, 20, Colors.GREEN).position(-25, -30)
        }
            .position(x, y)
            .registerBodyWithFixture( type = BodyType.STATIC)
            .rotation(rotation)
    }

}