import com.soywiz.klock.*
import com.soywiz.korge.box2d.*
import com.soywiz.korge.time.timers
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Rectangle
import org.jbox2d.common.Timer
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.FixtureDef
import kotlin.math.roundToInt
import kotlin.time.measureTime


class Station(mainStage: Stage, x: Int, y: Int, rotation: Angle = Angle.ZERO) : Container() {
    private var pointsLeft = 1
    private val landingPadRect  =  SolidRect(50, 10, Colors.GREEN).position(-25, 0)
    private var landingPad: Fixture

    private val station = mainStage.container {
        solidRect(200, 20, Colors.WHITE).position(-100, 10)
        addChild(landingPadRect.centerXOn(this))
    }
    .position(x, y)
    .registerBodyWithFixture(shape = BoxShape(Rectangle(-100, 10, 200, 20)/nearestBox2dWorld.customScale), type = BodyType.STATIC)
    .rotation(rotation)
    .body!!


    init {
        landingPad = station.createFixture(
            FixtureDef().apply {
                shape = BoxShape(Rectangle(-25, 0, 50, 10)/nearestBox2dWorld.customScale)
                isSensor = true
            }
        )!!

        landingSites.add(landingPad)


        mainStage.addUpdater {
            movePointsToScore()
        }
    }

    private fun movePointsToScore(){
        while (pointsLeft > 0 && docked){
            pointsLeft -= 1
            score += 1
        }
    }
}