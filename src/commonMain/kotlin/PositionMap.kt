import com.soywiz.kds.*
import kotlin.random.*

class Position(val x: Int, val y: Int)

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {

    //TODO: Question for Austin, I need some help walking through these functions

    private fun getOrNull(x: Int, y: Int) = if(array.get(x,y) != -1) Position(x,y) else null

    private fun getNumber(x: Int, y: Int) = array.tryGet(x,y)?.let {blocks[it]?.number?.ordinal ?: -1}

    operator fun get(x: Int, y: Int ) = array[x,y]

    operator fun set(x: Int, y: Int, value: Int){
        array[x,y] = value
    }

    //TODO: Question for Austin, I follow the function takes a function and executes it on each element, but what's the "Unit" here?
    fun forEach(action: (Int) -> Unit) {array.forEach(action)}

    override  fun equals(other: Any?): Boolean {
        return (other is PositionMap) && this.array.data.contentEquals(other.array.data)
    }

    override fun hashCode() = array.hashCode()

    fun getRandomFreePosition(): Position? {
        //TODO: Question for Austin, this array.count has a lambda, so, is this a 'count where'?
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if(value == -1) {
                current++
                if(current == chosen){
                    return Position( x, y)
                }
            }
        }
        return null
    }
}

