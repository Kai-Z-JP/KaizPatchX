@file:JvmName("RailRenderGeometryFactory")

package jp.ngt.rtm.render

import jp.ngt.rtm.rail.util.RailMap
import kotlin.math.max

data class RailRenderGeometry(
    val positions: Array<FloatArray>,
    val logicalSampleCount: Int,
    val logicalIndices: IntArray,
    val allowsEmpty: Boolean,
)

fun createRailRenderGeometry(
    railMap: RailMap,
    originX: Double,
    originZ: Double,
    minimumSplit: Int = 0,
    endOffset: Int = 0,
): RailRenderGeometry {
    val plan = createRailRenderSamplePlan(railMap, minimumSplit)
    val startPoint = railMap.getRailPos(plan.split, 0)
    val startHeight = railMap.startRP.posY
    val moveX = (startPoint[1] - originX).toFloat()
    val moveZ = (startPoint[0] - originZ).toFloat()
    val endIndexExclusive = if (plan.isSectioned) {
        plan.endIndexExclusive
    } else {
        max(0, plan.split - endOffset) + 1
    }
    val sampleCount = max(0, endIndexExclusive - plan.startIndex)
    val logicalIndices = IntArray(sampleCount) { index ->
        if (plan.isSectioned) plan.startIndex + index else index
    }
    val positions = Array(sampleCount) { localIndex ->
        val logicalIndex = plan.startIndex + localIndex
        val point = plan.getRailPos(logicalIndex)
        floatArrayOf(
            moveX + (point[1] - startPoint[1]).toFloat(),
            (plan.getRailHeight(logicalIndex) - startHeight).toFloat(),
            moveZ + (point[0] - startPoint[0]).toFloat(),
            plan.getRailYaw(logicalIndex),
            -plan.getRailPitch(logicalIndex),
            plan.getRailRoll(logicalIndex),
        )
    }
    val logicalSampleCount = if (plan.isSectioned) plan.logicalSampleCount else positions.size
    return RailRenderGeometry(
        positions,
        logicalSampleCount,
        logicalIndices,
        plan.isSectioned && positions.isEmpty(),
    )
}

fun isRailRenderGeometryValid(geometry: RailRenderGeometry?, minimumSplit: Int): Boolean {
    if (geometry == null) return false
    val sampleCount = geometry.positions.size
    return (sampleCount > 0 || geometry.allowsEmpty) && (sampleCount != 1 || minimumSplit > 0)
}
