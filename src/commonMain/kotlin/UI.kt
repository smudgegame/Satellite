import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.math.min


fun Container.createUI(mainStage: Stage) {

    val uiContainerWidth = 200 * uiScale
    val uiContainerHeight = 50 * uiScale

    val uiBars = 2
    val uiContainerFullHeight = uiContainerHeight * uiBars

    val uiBarWidth = uiContainerWidth - 10
    val uiBarHeight = uiContainerHeight - 5

    val uiAlpha = 0.5

    val flightAssistIndicator = solidRect(100 * uiScale, 25 * uiScale, Colors.WHITE).position(mainStage.width-100 * uiScale, 0.0)

    val flightAssistText = text("'X' FA: OFF", 12.5 * uiScale, Colors.RED) {
        centerXOn(flightAssistIndicator)
        alignTopToTopOf(flightAssistIndicator, 5.0)
    }

    //UI Container
    val uiContainer = solidRect(uiContainerWidth,uiContainerFullHeight, Colors["#677c7d"]){
        x = 0.0
        y = 0.0
        alpha = uiAlpha
    }

    //Health Gauge
    val healthGauge = solidRect(uiContainerWidth,uiContainerHeight, Colors["#677c7d"]){
        alpha = 0.0
        alignTopToTopOf(uiContainer)
        centerXOn(uiContainer)
    }
    val healthBar = solidRect(uiBarWidth,uiBarHeight, Colors["#ab0008"]){
        alpha = uiAlpha
        centerXOn(healthGauge.bview)
        centerYOn(healthGauge.bview)
    }
    //Fuel Gauge
    val fuelGauge = solidRect(uiContainerWidth,uiContainerHeight, Colors["#677c7d"]){
        alpha = 0.0
        alignTopToBottomOf(healthGauge.bview)
        centerXOn(uiContainer)
    }
    val fuelBar = solidRect(uiBarWidth,uiBarHeight, Colors.GREEN){
        alpha = uiAlpha
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

        healthBar.width = uiBarWidth * (health / healthCapacity).toDouble()
        fuelBar.width = uiBarWidth * (fuel / fuelCapacity).toDouble()
    }
}