@file:JvmName("BezierCurveDirection")

package jp.ngt.ngtlib.math

import kotlin.math.atan2

private const val TANGENT_TOLERANCE = 1e-12
private const val NEIGHBORHOOD = 1e-4

fun slopeAt(curve: CubicBezier2D, parameter: Double): Double {
    val t = parameter.coerceIn(0.0, 1.0)
    val tolerance = curve.tangentScale * TANGENT_TOLERANCE
    if (curve.tangentScale == 0.0) return 0.0
    val tangent = curve.tangentAt(t)
    if (tangent.length > tolerance) return atan2(tangent.y, tangent.x)

    val before = (t - NEIGHBORHOOD).coerceAtLeast(0.0)
    val after = (t + NEIGHBORHOOD).coerceAtMost(1.0)
    val centered = curve.meanTangent(before, after)
    if (centered.length > tolerance) return atan2(centered.y, centered.x)

    val oneSided = if (t < 1.0) curve.meanTangent(t, after) else curve.meanTangent(before, t)
    return atan2(oneSided.y, oneSided.x)
}
