import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors

fun Container.createUI(mainStage: Stage) {
    val flightAssistIndicator = solidRect(100, 25, Colors.WHITE).position(700, 0)

    val flightAssistText = text("'X' FA: OFF", 50 * 0.25, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }

    var lastAssist = flightAssist
    mainStage.addUpdater {
        if (lastAssist != flightAssist) {
            lastAssist = flightAssist
            if (flightAssist) {
                flightAssistText.text = "'X' FA: ON"
                flightAssistText.color = Colors.DARKGREEN
            } else {
                flightAssistText.text = "'X' FA: OFF"
                flightAssistText.color = Colors.RED
            }
        }
    }
}