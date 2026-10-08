package jp.ngt.ngtlib.math

import kotlin.math.hypot
import kotlin.math.max

class CubicBezier2D(
    private val startX: Double, private val startY: Double,
    firstX: Double, firstY: Double, secondX: Double, secondY: Double,
    private val endX: Double, private val endY: Double,
) {
    private val first = BezierVector(firstX - startX, firstY - startY)
    private val middle = BezierVector(secondX - firstX, secondY - firstY)
    private val last = BezierVector(endX - secondX, endY - secondY)
    val controlPolygonLength: Double = first.length + middle.length + last.length
    val tangentScale: Double = 3.0 * max(first.length, max(middle.length, last.length))
    private val thirdDerivative = (last - middle * 2.0 + first) * 6.0

    fun pointAt(parameter: Double): DoubleArray {
        val t = parameter.coerceIn(0.0, 1.0)
        if (t == 0.0) return doubleArrayOf(startX, startY)
        if (t == 1.0) return doubleArrayOf(endX, endY)
        val offset = offsetAt(t)
        return doubleArrayOf(startX + offset.x, startY + offset.y)
    }

    internal fun offsetAt(t: Double): BezierVector =
        (first * 3.0 + ((middle - first) * 3.0 + thirdDerivative * (t / 6.0)) * t) * t

    internal fun tangentAt(t: Double): BezierVector {
        val u = 1.0 - t
        return (first * (u * u) + middle * (2.0 * t * u) + last * (t * t)) * 3.0
    }

    internal fun meanTangent(from: Double, to: Double): BezierVector {
        val width = to - from
        return tangentAt((from + to) * 0.5) + thirdDerivative * (width * width / 24.0)
    }
}

internal data class BezierVector(val x: Double, val y: Double) {
    val length: Double get() = hypot(x, y)
    operator fun plus(other: BezierVector) = BezierVector(x + other.x, y + other.y)
    operator fun minus(other: BezierVector) = BezierVector(x - other.x, y - other.y)
    operator fun times(scale: Double) = BezierVector(x * scale, y * scale)
}
