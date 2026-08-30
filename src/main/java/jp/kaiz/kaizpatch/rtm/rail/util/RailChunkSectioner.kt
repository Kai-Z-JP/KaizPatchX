package jp.kaiz.kaizpatch.rtm.rail.util

import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.util.MathHelper
import java.util.LinkedHashSet
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class RailSection(
    val startRatio: Double,
    val endRatio: Double,
    val startRP: RailPosition,
    val endRP: RailPosition,
)

/** 通常レールの中心線を、通過するチャンクごとの連続区間へ分ける。 */
object RailChunkSectioner {
    private const val RATIO_EPSILON = 1.0E-7
    private const val SAMPLE_PER_METER = 4.0

    @JvmStatic
    fun split(source: RailMapBasic): List<RailSection> {
        if (source.length <= 0.0) {
            return listOf(RailSection(0.0, 1.0, copy(source.startRP), copy(source.endRP)))
        }

        val boundaries = findBoundaries(source)
        if (boundaries.isEmpty()) {
            return listOf(RailSection(0.0, 1.0, copy(source.startRP), copy(source.endRP)))
        }

        val starts = mutableListOf(SectionStart(0.0, copy(source.startRP)))
        val occupiedCoreBlocks = LinkedHashSet<BlockPos>()
        occupiedCoreBlocks += BlockPos(source.startRP.blockX, source.startRP.blockY, source.startRP.blockZ)

        for (index in boundaries.indices) {
            val ratio = boundaries[index]
            val nextRatio = if (index + 1 < boundaries.size) boundaries[index + 1] else 1.0
            val core = findCoreBlock(source, ratio, nextRatio, occupiedCoreBlocks) ?: continue
            val rp = createBoundaryRP(source, ratio, core)
            starts += SectionStart(ratio, rp)
            occupiedCoreBlocks += core
        }

        if (starts.size == 1) {
            return listOf(RailSection(0.0, 1.0, starts[0].rp, copy(source.endRP)))
        }

        val result = ArrayList<RailSection>(starts.size)
        for (index in starts.indices) {
            val start = starts[index]
            val endRatio = if (index + 1 < starts.size) starts[index + 1].ratio else 1.0
            val endRP = if (index + 1 < starts.size) asSectionEnd(starts[index + 1].rp) else copy(source.endRP)
            if (endRatio - start.ratio > RATIO_EPSILON) {
                result += RailSection(start.ratio, endRatio, copy(start.rp), endRP)
            }
        }
        return result
    }

    private fun findBoundaries(source: RailMapBasic): List<Double> {
        val samples = max(1, ceil(source.length * SAMPLE_PER_METER).toInt())
        val result = ArrayList<Double>()
        var previousRatio = 0.0
        var previousChunk = chunkAt(source, previousRatio)

        for (index in 1..samples) {
            val currentRatio = index.toDouble() / samples.toDouble()
            var currentChunk = chunkAt(source, currentRatio)
            var searchStart = previousRatio
            var searchChunk = previousChunk
            var guard = 0

            while (searchChunk != currentChunk && guard++ < 4) {
                val boundary = findFirstChunkChange(source, searchStart, currentRatio, searchChunk)
                if (boundary > RATIO_EPSILON && boundary < 1.0 - RATIO_EPSILON &&
                    (result.isEmpty() || boundary - result.last() > RATIO_EPSILON)
                ) {
                    result += boundary
                }

                val after = min(currentRatio, boundary + max(RATIO_EPSILON * 4.0, (currentRatio - previousRatio) * 1.0E-5))
                if (after <= searchStart + RATIO_EPSILON) break
                searchStart = after
                searchChunk = chunkAt(source, searchStart)
                currentChunk = chunkAt(source, currentRatio)
            }

            previousRatio = currentRatio
            previousChunk = currentChunk
        }
        return result.filter { boundary ->
            val delta = max(RATIO_EPSILON * 8.0, 1.0E-6)
            chunkAt(source, max(0.0, boundary - delta)) != chunkAt(source, min(1.0, boundary + delta))
        }
    }

    private fun findFirstChunkChange(source: RailMapBasic, from: Double, to: Double, fromChunk: ChunkPos): Double {
        var low = from
        var high = to
        while (high - low > RATIO_EPSILON) {
            val middle = (low + high) * 0.5
            if (chunkAt(source, middle) == fromChunk) {
                low = middle
            } else {
                high = middle
            }
        }
        return high
    }

    private fun findCoreBlock(
        source: RailMapBasic,
        startRatio: Double,
        endRatio: Double,
        occupied: Set<BlockPos>,
    ): BlockPos? {
        val length = endRatio - startRatio
        if (length <= RATIO_EPSILON) return null

        val targetRatio = min(endRatio, startRatio + max(RATIO_EPSILON * 8.0, length * 1.0E-4))
        val targetChunk = chunkAt(source, targetRatio)
        for (index in 0..32) {
            val local = if (index == 0) 1.0E-4 else index.toDouble() / 32.0
            val ratio = min(endRatio, startRatio + length * local)
            val point = source.getRailPos(ratio)
            val candidate = BlockPos(
                floor(point[1]).toInt(),
                floor(source.getRailHeight(ratio)).toInt(),
                floor(point[0]).toInt(),
            )
            if (candidate.chunk == targetChunk && candidate !in occupied) {
                return candidate
            }
        }
        return null
    }

    private fun createBoundaryRP(source: RailMapBasic, ratio: Double, core: BlockPos): RailPosition {
        val yaw = source.getRailYaw(ratio)
        val direction = (MathHelper.floor_double((normalizeAngle(yaw) / 45.0F + 0.5F).toDouble()) and 7).toByte()
        val point = source.getRailPos(ratio)
        val rp = RailPosition(core.x, core.y, core.z, direction)
        rp.anchorYaw = MathHelper.wrapAngleTo180_float(yaw)
        rp.anchorPitch = MathHelper.wrapAngleTo180_float(source.getRailPitch(ratio))
        rp.anchorLengthHorizontal = 0.0F
        rp.anchorLengthVertical = 0.0F
        rp.cantEdge = source.getRailRoll(ratio)
        copyLimits(source.startRP, rp)
        rp.setPosition(point[1], source.getRailHeight(ratio), point[0])
        return rp
    }

    private fun normalizeAngle(angle: Float): Float {
        var result = angle % 360.0F
        if (result < 0.0F) result += 360.0F
        return result
    }

    private fun chunkAt(source: RailMapBasic, ratio: Double): ChunkPos {
        val point = source.getRailPos(ratio)
        return ChunkPos(floor(point[1]).toInt() shr 4, floor(point[0]).toInt() shr 4)
    }

    private fun copy(source: RailPosition): RailPosition = RailPosition.readFromNBT(source.writeToNBT())

    private fun asSectionEnd(sectionStart: RailPosition): RailPosition {
        val result = copy(sectionStart)
        val x = result.posX
        val y = result.posY
        val z = result.posZ
        result.direction = ((result.direction.toInt() + 4) and 7).toByte()
        result.anchorYaw = MathHelper.wrapAngleTo180_float(result.anchorYaw + 180.0F)
        result.setPosition(x, y, z)
        return result
    }

    private fun copyLimits(source: RailPosition, target: RailPosition) {
        target.constLimitHP = source.constLimitHP
        target.constLimitHN = source.constLimitHN
        target.constLimitWP = source.constLimitWP
        target.constLimitWN = source.constLimitWN
        target.cantCenter = source.cantCenter
        target.cantRandom = source.cantRandom
    }

    private data class SectionStart(val ratio: Double, val rp: RailPosition)
    private data class ChunkPos(val x: Int, val z: Int)
    private data class BlockPos(val x: Int, val y: Int, val z: Int) {
        val chunk: ChunkPos get() = ChunkPos(x shr 4, z shr 4)
    }
}
