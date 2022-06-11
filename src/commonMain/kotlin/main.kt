import com.soywiz.klock.seconds
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.vector.roundRect
import com.soywiz.korma.interpolation.Easing

suspend fun main() = Korge(width = 480, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {

    val font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
    val restartImg = resourcesVfs["restart.png"].readBitmap()
    val undoImg = resourcesVfs["undo.png"].readBitmap()

    //Setting game component sizes based on view width
    val cellSize = views.virtualWidth / 5.0
    val fieldSize = 50 + 4 * cellSize
    val leftIndent = (views.virtualWidth - fieldSize) / 2
    val topIndent = 150.0

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
}