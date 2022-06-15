import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.vector.roundRect
import kotlin.properties.*
import kotlin.random.*


var cellSize: Double = 0.0
var fieldSize: Double = 0.0
var leftIndent: Double = 0.0
var topIndent: Double = 0.0
var font: BitmapFont by Delegates.notNull()

//Block management
var map = PositionMap()
val blocks: MutableMap<Int, Block> = mutableMapOf<Int,Block>()
var freeId = 0

fun columnX(number: Int) = leftIndent + 10 + (cellSize + 10) * number
fun rowY(number: Int) = topIndent + 10 + (cellSize + 10) * number

suspend fun main() = Korge(width = 480, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {

    font = resourcesVfs["clear_sans.fnt"].readBitmapFont()

    val restartImg = resourcesVfs["restart.png"].readBitmap()
    val undoImg = resourcesVfs["undo.png"].readBitmap()

    //Setting game component sizes based on view width
    cellSize = views.virtualWidth / 5.0
    fieldSize = 50 + 4 * cellSize
    leftIndent = (views.virtualWidth - fieldSize) / 2
    topIndent = 150.0

    //Creating game board frame
    /* First Attempts
    val bgField = RoundRect(fieldSize,fieldSize, 5.0, fill = Colors["#b9aea0"])
    bgField.x = leftIndent
    bgField.y = topIndent

    val bgField = roundRect(fieldSize,fieldSize, 5.0, fill = Colors["#b9aea0"]) {
        x = leftIndent
        y = topIndent)
    }*/

    val bgField = roundRect(fieldSize,fieldSize, 5.0, fill = Colors["#b9aea0"]) {
        position(leftIndent,topIndent)
    }
    //or bgField.addTo(this)
    addChild(bgField)

    graphics {
        position(leftIndent,topIndent)
        fill(Colors["#cec0b2"]){
            for(i in 0..3){
                for(j in 0..3){
                    roundRect( 10.0 + (10 + cellSize) * i, 10.0 + (10 + cellSize) * j, cellSize, cellSize,5.0)
                }
            }
        }
    }

    val bgLogo = roundRect( cellSize, cellSize,5.0, fill = Colors["#edc403"]){
        position(leftIndent,30.0)
    }
    addChild(bgLogo)

    val bgBest = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]){
        alignRightToRightOf(bgField)
        alignTopToTopOf(bgLogo)
    }
    addChild(bgBest)

    var bgScore = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]){
        alignRightToLeftOf(bgBest,24)
        alignTopToTopOf(bgLogo)
    }
    addChild(bgScore)

    text("2048", cellSize * 0.5, Colors.WHITE, font).centerOn(bgLogo)

    text("SCORE",cellSize * 0.25, RGBA(239, 226, 210), font){
        centerXOn(bgScore)
        alignTopToTopOf(bgScore, 5.0)
    }

    text("0",cellSize * 0.5, Colors.WHITE, font){
        centerXOn(bgScore)
        alignBottomToBottomOf(bgScore, 5.0)
    }

    text("BEST",cellSize * 0.25, RGBA(239, 226, 210), font){
        centerXOn(bgBest)
        alignTopToTopOf(bgBest, 5.0)
    }

    text("0",cellSize * 0.5, Colors.WHITE, font){
        centerXOn(bgBest)
        alignBottomToBottomOf(bgBest, 5.0)
    }

    val btnSize = cellSize * 0.3
    val restartBlock = container{
        val background = roundRect(btnSize, btnSize, 5.0, fill = RGBA(185, 174, 160))
        image(restartImg){
            size(btnSize * 0.8, btnSize * 0.8)
            centerOn(background)
        }
        alignTopToBottomOf(bgBest, 5.0)
        alignRightToRightOf(bgField)
    }

    val undoBlock = container{
        val background = roundRect(btnSize,btnSize, 5.0, fill = RGBA(185, 174, 160))
        image(undoImg){
            size(btnSize * 0.6, btnSize * 0.6)
            centerOn(background)
        }
        alignTopToBottomOf(bgBest, 5.0)
        alignRightToLeftOf(restartBlock,5.0)
    }

    InitOrderDemo("Test")

    generateBlock()

    root.keys.down {
        when (it.key) {
            Key.LEFT -> moveBlocksTo(Direction.LEFT)
            Key.RIGHT -> moveBlocksTo(Direction.RIGHT)
            Key.UP -> moveBlocksTo(Direction.TOP)
            Key.DOWN -> moveBlocksTo(Direction.BOTTOM)
            else -> Unit
        }
    }

    onSwipe(20.0) {
        when (it.direction) {
            SwipeDirection.LEFT -> moveBlocksTo(Direction.LEFT)
            SwipeDirection.RIGHT -> moveBlocksTo(Direction.RIGHT)
            SwipeDirection.TOP -> moveBlocksTo(Direction.TOP)
            SwipeDirection.BOTTOM -> moveBlocksTo(Direction.BOTTOM)
        }
    }
}

fun Stage.moveBlocksTo(direction: Direction) {
    println(direction)
}

fun Container.generateBlock() {
    val position = map.getRandomFreePosition() ?: return
    val number = if (Random.nextDouble() < 0.9) Number.ZERO else Number.ONE
    val newId = createNewBlock(number, position)
    map[position.x, position.y] = newId

}

fun Container.createNewBlock(number: Number, position: Position): Int {
    val id = freeId++
    createNewBlockWithId(id, number, position)
    return id
}

fun Container.createNewBlockWithId(id: Int, number: Number, position: Position) {
    blocks[id] = block(number).position(columnX(position.x), rowY(position.y))
}
