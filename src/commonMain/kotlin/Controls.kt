import com.soywiz.korev.GameButton
import com.soywiz.korev.GameStick
import com.soywiz.korev.Key
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addUpdater
import com.soywiz.korma.geom.Point
import org.jbox2d.dynamics.Body
import kotlin.math.absoluteValue

private const val deadzone = 0.1

class Controls(private val ship: Body, mainStage: Stage) {
    private val input = mainStage.input

    init {
        mainStage.addUpdater {
            applyControls(ship)
        }
    }

    private fun applyControls(ship: Body) {
        input.connectedGamepads
        val rawGamepad0 = input.gamepads[0]
        val leftStick: Point = rawGamepad0[GameStick.LEFT]
        val rightStick: Point = rawGamepad0[GameStick.RIGHT]
        val rightTrigger: Double = rawGamepad0[GameButton.RIGHT_TRIGGER]
        val leftTrigger: Double = rawGamepad0[GameButton.LEFT_TRIGGER]

        var thrustInput = false
        var torqueInput = false
        if (input.keys.pressing(Key.UP) || input.keys.pressing(Key.W) || rightTrigger > deadzone || rightStick.y.toFloat() > deadzone) {
            thrustInput = true

            when {
                rightTrigger > deadzone -> ship.thrustUp(rightTrigger.toFloat() * forwardThrust)
                rightStick.y > deadzone -> ship.thrustUp(rightStick.y.toFloat() * fineThrust)
                else -> ship.thrustUp(forwardThrust)
            }
        }
        if (input.keys.pressing(Key.DOWN) || input.keys.pressing(Key.S) || leftTrigger.toFloat() > deadzone || rightStick.y.toFloat() < -deadzone) {
            thrustInput = true

            when {
                leftTrigger.toFloat() > deadzone -> ship.thrustDown(leftTrigger.toFloat() * fineThrust)
                rightStick.y.toFloat() < -deadzone -> ship.thrustDown(rightStick.y.toFloat().absoluteValue * fineThrust)
                else -> ship.thrustDown(fineThrust)
            }
        }
        if (input.keys.pressing(Key.E) || input.keys.pressing(Key.PAGE_DOWN) || rightStick.x.toFloat() > deadzone) {
            thrustInput = true

            if (rightStick.x.toFloat() > deadzone)
                ship.thrustRight(rightStick.x.toFloat() * fineThrust)
            else
                ship.thrustRight(fineThrust)
        }
        if (input.keys.pressing(Key.Q) || input.keys.pressing(Key.DELETE) || rightStick.x.toFloat() < -deadzone) {
            thrustInput = true

            if (rightStick.x.toFloat() < -deadzone)
                ship.thrustLeft(rightStick.x.toFloat().absoluteValue * fineThrust)
            else
                ship.thrustLeft(fineThrust)
        }
        if (input.keys.pressing(Key.RIGHT) || input.keys.pressing(Key.D) || leftStick.x.toFloat() > deadzone) {
            torqueInput = true

            if (leftStick.x > deadzone)
                ship.torqueRight(leftStick.x.toFloat() * angularThrust)
            else
                ship.torqueRight(angularThrust)
        }
        if (input.keys.pressing(Key.LEFT) || input.keys.pressing(Key.A) || leftStick.x.toFloat() < -deadzone) {
            torqueInput = true

            if (leftStick.x.toFloat() < -deadzone)
                ship.torqueLeft(leftStick.x.toFloat().absoluteValue * angularThrust)
            else
                ship.torqueLeft(angularThrust)
        }
        if (input.keys.justPressed(Key.X)) {
            flightAssist = !flightAssist
        }
        ship.flightAssist(thrustInput, torqueInput, toggled = flightAssist)
    }
}