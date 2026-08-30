package jp.kaiz.kaizpatch.rtm.rail.util

import jp.ngt.rtm.rail.TileEntityLargeRailBase
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.util.RailMap
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import kotlin.math.*

/** 現在のレール端点から、進行方向側に接続されたレールを解決する。 */
object RailTransitionResolver {
    private const val SPLITS_PER_METER = 360.0
    private const val ENDPOINT_MARGIN_METERS = 0.5
    private val verticalOffsets = intArrayOf(0, 1, -1, 2, -2, 3, -3)

    @JvmStatic
    fun findConnectedCore(
        world: World,
        currentCore: TileEntityLargeRailCore?,
        currentMap: RailMap?,
        split: Int,
        previousIndex: Int,
        currentX: Double,
        currentZ: Double,
        targetX: Double,
        targetZ: Double,
        movingYaw: Float,
    ): TileEntityLargeRailCore? {
        if (currentCore == null || currentMap == null || split <= 0 || previousIndex < 0) return null

        val movement = hypot(targetX - currentX, targetZ - currentZ)
        val travelYaw = if (movement > 1.0E-7) {
            Math.toDegrees(atan2(targetX - currentX, targetZ - currentZ)).toFloat()
        } else {
            movingYaw
        }
        val exits = selectExitDirections(currentMap, split, previousIndex, movement, travelYaw)
        for (towardEnd in exits) {
            val endpoint = if (towardEnd) currentMap.endRP else currentMap.startRP
            findCoreAcrossEndpoint(world, currentCore, currentMap, endpoint)?.let { return it }
        }
        return null
    }

    internal fun selectExitDirections(
        currentMap: RailMap,
        split: Int,
        previousIndex: Int,
        movement: Double,
        travelYaw: Float,
    ): List<Boolean> {
        val indexMargin = ceil((movement + ENDPOINT_MARGIN_METERS) * SPLITS_PER_METER).toInt()
        return buildList {
            if (previousIndex <= indexMargin) {
                add(false)
            }
            if (split - previousIndex <= indexMargin) {
                add(true)
            }
        }
            .sortedBy { abs(MathHelper.wrapAngleTo180_float(travelYaw - outwardYaw(currentMap, split, it))) }
            .filter { abs(MathHelper.wrapAngleTo180_float(travelYaw - outwardYaw(currentMap, split, it))) <= 90.0F }
    }

    private fun outwardYaw(map: RailMap, split: Int, towardEnd: Boolean): Float {
        val index = if (towardEnd) max(0, split - 1) else min(1, split)
        val yaw = map.getRailYaw(split, index) + if (towardEnd) 0.0F else 180.0F
        return MathHelper.wrapAngleTo180_float(yaw)
    }

    private fun findCoreAcrossEndpoint(
        world: World,
        currentCore: TileEntityLargeRailCore,
        currentMap: RailMap,
        endpoint: RailPosition,
    ): TileEntityLargeRailCore? {
        val neighbor = endpoint.neighborPos
        val expectedY = floor(endpoint.posY).toInt()
        val yCandidates = LinkedHashSet<Int>().apply {
            add(neighbor[1])
            verticalOffsets.forEach { add(expectedY + it) }
        }
        world.chunkProvider.loadChunk(neighbor[0] shr 4, neighbor[2] shr 4)
        for (y in yCandidates) {
            val rail = world.getTileEntity(neighbor[0], y, neighbor[2]) as? TileEntityLargeRailBase ?: continue
            val candidate = rail.railCore ?: continue
            if (candidate === currentCore) continue
            if (currentCore.isSameLogicalRail(candidate) || connects(currentMap, candidate)) return candidate
        }
        return null
    }

    private fun connects(currentMap: RailMap, candidate: TileEntityLargeRailCore): Boolean =
        candidate.allRailMaps?.any { currentMap.canConnect(it) } == true

}
