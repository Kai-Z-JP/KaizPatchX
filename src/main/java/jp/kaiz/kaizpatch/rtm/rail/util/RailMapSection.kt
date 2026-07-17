package jp.kaiz.kaizpatch.rtm.rail.util

import jp.ngt.rtm.rail.util.RailMap
import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition
import kotlin.math.max
import kotlin.math.min

/**
 * 元のRailMapBasicの一部分だけを公開するRailMap。
 * 曲線を再構築せず元RailMapへ委譲することで、勾配・カントを含めた連続性を維持する。
 */
class RailMapSection(
    val source: RailMapBasic,
    private val sectionStartRP: RailPosition,
    private val sectionEndRP: RailPosition,
    val startRatio: Double,
    val endRatio: Double,
) : RailMap() {
    private val ratioLength = max(0.0, endRatio - startRatio)

    override fun getStartRP(): RailPosition = sectionStartRP

    override fun getEndRP(): RailPosition = sectionEndRP

    override fun getLength(): Double = source.length * ratioLength

    private fun sourceRatio(split: Int, index: Int): Double {
        val localRatio = if (split <= 0) 0.0 else index.toDouble() / split.toDouble()
        return sourceRatio(localRatio)
    }

    fun sourceRatio(localRatio: Double): Double {
        return startRatio + ratioLength * max(0.0, min(1.0, localRatio))
    }

    override fun getNearlestPoint(split: Int, x: Double, z: Double): Int {
        if (split <= 0) return 0
        var nearest = 0
        var distance = Double.MAX_VALUE
        for (index in 0..split) {
            val point = getRailPos(split, index)
            val dx = x - point[1]
            val dz = z - point[0]
            val current = dx * dx + dz * dz
            if (current < distance) {
                distance = current
                nearest = index
            }
        }
        return nearest
    }

    override fun getRailPos(split: Int, index: Int): DoubleArray = source.getRailPos(sourceRatio(split, index))

    override fun getRailHeight(split: Int, index: Int): Double = source.getRailHeight(sourceRatio(split, index))

    override fun getRailYaw(split: Int, index: Int): Float = source.getRailYaw(sourceRatio(split, index))

    override fun getRailPitch(split: Int, index: Int): Float = source.getRailPitch(sourceRatio(split, index))

    override fun getRailRoll(split: Int, index: Int): Float = source.getRailRoll(sourceRatio(split, index))
}
