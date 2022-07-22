import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Anchor
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType

class Orb(mainStage: Stage) : Container() {
    private val shape = mainStage.circle(10.0, Colors.GREEN) {
        anchor(Anchor.CENTER)
    }

    private val orbBody = shape.registerBodyWithFixture(type = BodyType.DYNAMIC, gravityScale = 0f).body!!

    init {
        mainStage.addUpdater {
            val mouseVec = Vec2(input.mouse.x.toFloat(), input.mouse.y.toFloat())
            val goal = mouseVec - Vec2(shape.pos.x.toFloat(), shape.pos.y.toFloat())
            orbBody.linearVelocity = goal
        }
    }
}