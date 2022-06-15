import com.soywiz.korim.color.*
import Number.*
import com.soywiz.korge.view.*

fun Container.block(number: Number) : Block {
    return Block(number).addTo(this)
}

class Block(val number: Number): Container() {

    init {
        roundRect(cellSize, cellSize, 5.0, fill = number.color)
        val textColor = when (number) {
            ZERO, ONE -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.value.toString(), textSizeFor(number), textColor, font) {
            centerBetween(0.0, 0.0, cellSize, cellSize)
        }
    }
}

//TODO: Question for Austin, return type of function textSizeFor? Or because  the curly braces at the end implies lambda function?
private fun textSizeFor(number: Number) = when (number) {
    ZERO, ONE, TWO, THREE, FOUR, FIVE -> cellSize / 2
    SIX, SEVEN, EIGHT -> cellSize * 4 / 9
    NINE, TEN, ELEVEN, TWELVE -> cellSize * 2 / 5
    THIRTEEN, FOURTEEN, FIFTEEN -> cellSize * 7 / 20
    SIXTEEN -> cellSize * 3 / 10
}
