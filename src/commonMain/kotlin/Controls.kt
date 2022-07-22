import com.soywiz.korev.GameButton
import com.soywiz.korev.GameStick
import com.soywiz.korev.Key
import com.soywiz.korge.box2d.body
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addUpdater
import com.soywiz.korma.geom.Point
import org.jbox2d.dynamics.Body
import kotlin.math.absoluteValue

private const val deadzone = 0.1


var thrustInput = false
var torqueInput = false

class Controls(private val ship: Ship, private val shipBody: Body, mainStage: Stage) {
    private val input = mainStage.input

    init {
        mainStage.addUpdater {
            applyControls(ship, shipBody)
        }
    }

    private fun applyControls(ship: Ship, shipBody: Body) {
        input.connectedGamepads

        thrustInput = false
        torqueInput = false

        val rawGamepad0 = input.gamepads[0]
        val leftStick: Point = rawGamepad0[GameStick.LEFT]
        val rightStick: Point = rawGamepad0[GameStick.RIGHT]
        val rightTrigger: Double = rawGamepad0[GameButton.RIGHT_TRIGGER]
        val leftTrigger: Double = rawGamepad0[GameButton.LEFT_TRIGGER]

        if (fuel > 0) {
            if (input.keys.pressing(Key.UP) || input.keys.pressing(Key.W) || rightTrigger > deadzone || rightStick.y.toFloat() > deadzone) {
                thrustInput = true

                when {
                    rightTrigger > deadzone -> shipBody.thrustUp(rightTrigger.toFloat() * forwardThrust)
                    rightStick.y > deadzone -> shipBody.thrustUp(rightStick.y.toFloat() * fineThrust)
                    else -> {
                        ship.undock()
                        shipBody.thrustUp(forwardThrust)
                    }
                }
            }
            if (input.keys.pressing(Key.DOWN) || input.keys.pressing(Key.S) || leftTrigger.toFloat() > deadzone || rightStick.y.toFloat() < -deadzone && fuel > 0) {
                thrustInput = true

                when {
                    leftTrigger.toFloat() > deadzone -> shipBody.thrustDown(leftTrigger.toFloat() * fineThrust)
                    rightStick.y.toFloat() < -deadzone -> shipBody.thrustDown(rightStick.y.toFloat().absoluteValue * fineThrust)
                    else -> shipBody.thrustDown(fineThrust)
                }
            }
            if (input.keys.pressing(Key.E) || input.keys.pressing(Key.PAGE_DOWN) || rightStick.x.toFloat() > deadzone && fuel > 0 && fuel > 0) {
                thrustInput = true

                if (rightStick.x.toFloat() > deadzone)
                    shipBody.thrustRight(rightStick.x.toFloat() * fineThrust)
                else
                    shipBody.thrustRight(fineThrust)
            }
            if (input.keys.pressing(Key.Q) || input.keys.pressing(Key.DELETE) || rightStick.x.toFloat() < -deadzone && fuel > 0) {
                thrustInput = true

                if (rightStick.x.toFloat() < -deadzone)
                    shipBody.thrustLeft(rightStick.x.toFloat().absoluteValue * fineThrust)
                else
                    shipBody.thrustLeft(fineThrust)
            }
        }

        if (input.keys.pressing(Key.RIGHT) || input.keys.pressing(Key.D) || leftStick.x.toFloat() > deadzone && fuel > 0) {
            torqueInput = true

            if (leftStick.x > deadzone)
                shipBody.torqueRight(leftStick.x.toFloat() * angularThrust)
            else
                shipBody.torqueRight(angularThrust)
        }
        if (input.keys.pressing(Key.LEFT) || input.keys.pressing(Key.A) || leftStick.x.toFloat() < -deadzone) {
            torqueInput = true

            if (leftStick.x.toFloat() < -deadzone)
                shipBody.torqueLeft(leftStick.x.toFloat().absoluteValue * angularThrust)
            else
                shipBody.torqueLeft(angularThrust)
        }
        if (input.keys.justPressed(Key.X)) {
            flightAssist = !flightAssist
        }
        shipBody.flightAssist(thrustInput, torqueInput, toggled = flightAssist)
    }
}