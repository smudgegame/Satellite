import com.soywiz.korge.box2d.body
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.PointArrayList
import com.soywiz.korma.geom.shape.Shape2d
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.pooling.arrays.Vec2ArrayPool

class Asteroid(parent: Container): Container() {

    private val vertices = arrayOf(Vec2(0f,0f), Vec2(2f,0f), Vec2(0f,2f))

    private fun createBoundingPolygon(): PolygonShape{
        //Collision Polygon
        val vertPool = Vec2ArrayPool()
        val boundingShape = PolygonShape()
        boundingShape.set(vertices,3,vertPool,null)

        return boundingShape
    }

    private fun createViewPolygon(): Shape2d.Polygon {
        //View Polygon
        val pointArrayList = PointArrayList()
        pointArrayList.add(Point(0, 0))
        pointArrayList.add(Point(2, 0))
        pointArrayList.add(Point(0, 2))

        return Shape2d.Polygon(pointArrayList)
    }

}