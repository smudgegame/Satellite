import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.math.min

fun Container.createUI(mainStage: Stage) {
    val flightAssistIndicator = solidRect(100, 25, Colors.WHITE).position(mainStage.width-100, 0.0)

    val flightAssistText = text("'X' FA: OFF", 50 * 0.25, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }


    val fuelGauge = solidRect(200,50, Colors["#677c7d"]).position(mainStage.width-this.width, mainStage.height-this.height)
    val fuelBarWidth = 190
    val fuelBar = solidRect(190,40, Colors.GREEN){
        centerXOn(fuelGauge.bview)
        centerYOn(fuelGauge.bview)
    }

    var lastAssist = !flightAssist
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

        fuelBar.width = fuelBarWidth * (fuel / fuelCapacity).toDouble()
    }
}