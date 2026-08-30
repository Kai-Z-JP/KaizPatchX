package jp.kaiz.kaizpatch.rtm.rail.util

import jp.kaiz.kaizpatch.rtm.rail.TileEntityLargeRailSectionCore
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

    @JvmStatic
    fun findCrossedSectionCore(
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
        val sectionCore = currentCore as? TileEntityLargeRailSectionCore ?: return null
        if (currentMap == null || split <= 0 || previousIndex < 0) return null

        val exits = selectCrossedExitDirections(
            currentMap,
            split,
            previousIndex,
            currentX,
            currentZ,
            targetX,
            targetZ,
            movingYaw,
        )
        for (towardEnd in exits) {
            findAdjacentSectionCore(world, sectionCore, towardEnd)?.let { return it }
        }
        return null
    }

    @JvmStatic
    fun keepCurrentSectionCore(
        currentCore: TileEntityLargeRailCore?,
        locatedCore: TileEntityLargeRailCore,
    ): TileEntityLargeRailCore {
        val keepCurrent = shouldKeepCurrentSectionCore(
            hasCurrentCore = currentCore != null,
            isSameCore = currentCore === locatedCore,
            isSameLogicalRail = currentCore?.isSameLogicalRail(locatedCore) == true,
        )
        if (keepCurrent && currentCore != null) {
            return currentCore
        }
        return locatedCore
    }

    internal fun shouldKeepCurrentSectionCore(
        hasCurrentCore: Boolean,
        isSameCore: Boolean,
        isSameLogicalRail: Boolean,
    ): Boolean = hasCurrentCore && !isSameCore && isSameLogicalRail

    internal fun selectCrossedExitDirections(
        currentMap: RailMap,
        split: Int,
        previousIndex: Int,
        currentX: Double,
        currentZ: Double,
        targetX: Double,
        targetZ: Double,
        movingYaw: Float,
    ): List<Boolean> {
        val movement = hypot(targetX - currentX, targetZ - currentZ)
        val travelYaw = if (movement > 1.0E-7) {
            Math.toDegrees(atan2(targetX - currentX, targetZ - currentZ)).toFloat()
        } else {
            movingYaw
        }
        return selectExitDirections(currentMap, split, previousIndex, movement, travelYaw)
            .filter { towardEnd -> isBeyondEndpoint(currentMap, split, targetX, targetZ, towardEnd) }
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

    private fun isBeyondEndpoint(
        map: RailMap,
        split: Int,
        targetX: Double,
        targetZ: Double,
        towardEnd: Boolean,
    ): Boolean {
        val endpoint = if (towardEnd) map.endRP else map.startRP
        val yaw = Math.toRadians(outwardYaw(map, split, towardEnd).toDouble())
        val outwardDistance =
            (targetX - endpoint.posX) * sin(yaw) + (targetZ - endpoint.posZ) * cos(yaw)
        return outwardDistance > CROSSING_EPSILON
    }

    private fun findAdjacentSectionCore(
        world: World,
        currentCore: TileEntityLargeRailSectionCore,
        towardEnd: Boolean,
    ): TileEntityLargeRailSectionCore? {
        val positions = currentCore.getRailGroupCorePositions()
        val currentIndex = positions.indexOfFirst { pos ->
            pos[0] == currentCore.xCoord && pos[1] == currentCore.yCoord && pos[2] == currentCore.zCoord
        }
        if (currentIndex < 0) return null

        val adjacentIndex = currentIndex + if (towardEnd) 1 else -1
        val adjacentPos = positions.getOrNull(adjacentIndex) ?: return null
        world.chunkProvider.loadChunk(adjacentPos[0] shr 4, adjacentPos[2] shr 4)
        val adjacent = world.getTileEntity(adjacentPos[0], adjacentPos[1], adjacentPos[2])
                as? TileEntityLargeRailSectionCore ?: return null
        return adjacent.takeIf { currentCore.isSameLogicalRail(it) }
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

    private const val CROSSING_EPSILON = 1.0E-7
}
