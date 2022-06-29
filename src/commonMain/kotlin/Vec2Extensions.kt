import org.jbox2d.common.Vec2

operator fun Vec2.plus(other: Vec2) = this.add(other)
operator fun Vec2.minus(other: Vec2) = this.sub(other)
operator fun Vec2.times(scaler: Float) = this.mul(scaler)
operator fun Vec2.div(scaler: Float) = this.mul(1/scaler)

fun Vec2.dot(other: Vec2) = Vec2.dot(this, other)