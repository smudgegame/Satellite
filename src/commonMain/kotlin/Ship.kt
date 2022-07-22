import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.cosine
import com.soywiz.korma.geom.sine
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import kotlin.math.absoluteValue
import kotlin.math.sign


class Ship(mainStage: Stage) : Container() {
    private val shipBody = mainStage.solidRect(40, 15, Colors.LIGHTBLUE)
        .position(300, 100)
        .registerBodyWithFixture(type = BodyType.DYNAMIC, friction = 2f, gravityScale = 0f).body!!

    init {
        Controls(shipBody, mainStage)
    }


    init {
        mainStage.addUpdater {
            shipBody.wrapInView()
            shipBody.applyDrag()
        }
    }


}