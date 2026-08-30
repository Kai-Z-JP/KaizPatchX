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
    private const val NEAREST_SEARCH_MARGIN_METERS = 0.25
    private const val FORWARD_SEARCH_DISTANCE_BLOCKS = 2.0
    private const val FORWARD_SEARCH_STEP_BLOCKS = 0.25
    private const val ENDPOINT_MATCH_DISTANCE_SQ = 1.0E-6
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
            val exitYaw = outwardYaw(currentMap, split, towardEnd)
            findCoreAcrossEndpoint(world, currentCore, currentMap, endpoint, exitYaw)?.let { return it }
        }
        return null
    }

    @JvmStatic
    fun findCrossedConnectedCore(
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
            if (currentCore is TileEntityLargeRailSectionCore) {
                findAdjacentSectionCore(world, currentCore, towardEnd)?.let { return it }
            }
            val endpoint = if (towardEnd) currentMap.endRP else currentMap.startRP
            val exitYaw = outwardYaw(currentMap, split, towardEnd)
            findCoreAcrossEndpoint(world, currentCore, currentMap, endpoint, exitYaw)?.let { return it }
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

    @JvmStatic
    fun findConnectedEntryIndex(previousMap: RailMap?, nextMap: RailMap, nextSplit: Int): Int {
        if (previousMap == null || nextSplit <= 0) return -1
        val previousEndpoints = arrayOf(previousMap.startRP, previousMap.endRP)
        val nextEndpoints = arrayOf(nextMap.startRP, nextMap.endRP)
        var nearestNextEndpoint = -1
        var nearestDistance = Double.MAX_VALUE
        previousEndpoints.forEach { previous ->
            nextEndpoints.forEachIndexed { index, next ->
                val dx = previous.posX - next.posX
                val dz = previous.posZ - next.posZ
                val distance = dx * dx + dz * dz
                if (distance < nearestDistance) {
                    nearestDistance = distance
                    nearestNextEndpoint = index
                }
            }
        }
        if (nearestDistance > ENDPOINT_MATCH_DISTANCE_SQ) return -1
        return if (nearestNextEndpoint == 0) 0 else nextSplit
    }

    @JvmStatic
    fun findNearestPointAround(
        map: RailMap,
        split: Int,
        previousIndex: Int,
        x: Double,
        z: Double,
        movement: Float,
    ): Int {
        if (split <= 0) return 0
        if (previousIndex !in 0..split) return map.getNearlestPoint(split, x, z)

        val indexMargin = ceil((abs(movement) + NEAREST_SEARCH_MARGIN_METERS) * SPLITS_PER_METER)
            .toInt()
            .coerceAtLeast(1)
        val indexMin = max(0, previousIndex - indexMargin)
        val indexMax = min(split, previousIndex + indexMargin)
        var nearest = previousIndex
        var nearestDistance = Double.MAX_VALUE
        for (index in indexMin..indexMax) {
            val point = map.getRailPos(split, index)
            val dx = x - point[1]
            val dz = z - point[0]
            val distance = dx * dx + dz * dz
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearest = index
            }
        }
        return nearest
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
        exitYaw: Float,
    ): TileEntityLargeRailCore? {
        val neighbor = endpoint.neighborPos
        val expectedY = floor(endpoint.posY).toInt()
        val yCandidates = LinkedHashSet<Int>().apply {
            add(neighbor[1])
            verticalOffsets.forEach { add(expectedY + it) }
        }
        for ((x, z) in forwardSearchPositions(endpoint, exitYaw)) {
            world.chunkProvider.loadChunk(x shr 4, z shr 4)
            for (y in yCandidates) {
                val rail = world.getTileEntity(x, y, z) as? TileEntityLargeRailBase ?: continue
                val candidate = rail.railCore ?: continue
                if (candidate === currentCore) continue
                if (currentCore.isSameLogicalRail(candidate) || connects(currentMap, candidate)) return candidate
            }
        }
        return null
    }

    internal fun forwardSearchPositions(endpoint: RailPosition, exitYaw: Float): List<Pair<Int, Int>> {
        val neighbor = endpoint.neighborPos
        val positions = linkedSetOf(neighbor[0] to neighbor[2])
        val yaw = Math.toRadians(exitYaw.toDouble())
        val stepCount = ceil(FORWARD_SEARCH_DISTANCE_BLOCKS / FORWARD_SEARCH_STEP_BLOCKS).toInt()
        repeat(stepCount) { index ->
            val distance = min(
                (index + 1) * FORWARD_SEARCH_STEP_BLOCKS,
                FORWARD_SEARCH_DISTANCE_BLOCKS - CROSSING_EPSILON,
            )
            val x = floor(endpoint.posX + sin(yaw) * distance).toInt()
            val z = floor(endpoint.posZ + cos(yaw) * distance).toInt()
            positions += x to z
        }
        return positions.toList()
    }

    private fun connects(currentMap: RailMap, candidate: TileEntityLargeRailCore): Boolean =
        candidate.allRailMaps?.any { currentMap.canConnect(it) } == true

    private const val CROSSING_EPSILON = 1.0E-7
}
