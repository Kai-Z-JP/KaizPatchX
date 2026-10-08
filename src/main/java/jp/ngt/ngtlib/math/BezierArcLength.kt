package jp.ngt.ngtlib.math

import kotlin.math.ceil
import kotlin.math.max

class BezierArcLength(curve: CubicBezier2D, samplesPerUnit: Int) {
    private val distances: DoubleArray
    val length: Double

    init {
        val segments = max(16, ceil(curve.controlPolygonLength * samplesPerUnit).toInt())
        distances = DoubleArray(segments + 1)
        var previous = curve.offsetAt(0.0)
        for (index in 1..segments) {
            val parameter = index.toDouble() / segments
            val point = curve.offsetAt(parameter)
            distances[index] = distances[index - 1] + (point - previous).length
            previous = point
        }
        length = distances.last()
    }

    fun parameterAt(ratio: Double): Double {
        if (ratio <= 0.0) return 0.0
        if (ratio >= 1.0) return 1.0
        if (length == 0.0) return ratio
        val distance = ratio * length
        if (distance >= length) return 1.0
        var lower = 0
        var upper = distances.lastIndex
        while (upper - lower > 1) {
            val middle = (lower + upper) ushr 1
            if (distances[middle] <= distance) lower = middle else upper = middle
        }
        val weight = (distance - distances[lower]) / (distances[upper] - distances[lower])
        return (lower + weight) / distances.lastIndex
    }
}
