@file:JvmName("RailRenderSampling")

package jp.ngt.rtm.render

import jp.kaiz.kaizpatch.rtm.rail.util.RailMapSection
import jp.ngt.rtm.rail.util.RailMap
import kotlin.math.ceil
import kotlin.math.max

data class RailRenderSamplePlan(
    val source: RailMap,
    val split: Int,
    val startIndex: Int,
    val endIndexExclusive: Int,
    val isSectioned: Boolean,
) {
    val logicalSampleCount: Int
        get() = split + 1

    val sampleCount: Int
        get() = max(0, endIndexExclusive - startIndex)

    val indices: IntRange
        get() = startIndex until endIndexExclusive

    fun getRailPos(index: Int): DoubleArray = source.getRailPos(split, index)

    fun getRailHeight(index: Int): Double = source.getRailHeight(split, index)

    fun getRailYaw(index: Int): Float = source.getRailYaw(split, index)

    fun getRailPitch(index: Int): Float = source.getRailPitch(split, index)

    fun getRailRoll(index: Int): Float = source.getRailRoll(split, index)
}

fun createRailRenderSamplePlan(railMap: RailMap, minimumSplit: Int = 0): RailRenderSamplePlan {
    val section = railMap as? RailMapSection
    val source = section?.source ?: railMap
    val split = max(railRenderSplit(source.length), minimumSplit)
    if (section == null) {
        return RailRenderSamplePlan(source, split, 0, split + 1, false)
    }

    val startIndex = firstIndexAtOrAfter(section.startRatio, split)
    val endIndexExclusive = if (section.endRatio >= 1.0) {
        split + 1
    } else {
        firstIndexAtOrAfter(section.endRatio, split)
    }
    return RailRenderSamplePlan(source, split, startIndex, endIndexExclusive, true)
}

fun railRenderSplit(length: Double): Int = max(0, (length.toFloat() * SAMPLES_PER_METER).toInt())

private fun firstIndexAtOrAfter(ratio: Double, split: Int): Int {
    val boundedRatio = ratio.coerceIn(0.0, 1.0)
    return ceil(boundedRatio * split).toInt().coerceIn(0, split)
}

private const val SAMPLES_PER_METER = 2.0F
