import org.jbox2d.common.Vec2

operator fun Vec2.plus(other: Vec2) = this.add(other)
operator fun Vec2.minus(other: Vec2) = this.sub(other)
operator fun Vec2.times(scalar: Float) = this.mul(scalar)
operator fun Vec2.div(scalar: Float) = this.mul(1/scalar)

fun Vec2.dot(other: Vec2) = Vec2.dot(this, other)