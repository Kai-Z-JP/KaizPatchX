package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import net.minecraft.client.gui.FontRenderer
import kotlin.math.max

@SideOnly(Side.CLIENT)
internal class DataFormGridLayout(
    definition: DataFormConfig,
    components: Collection<DataFormComponent>,
    fontRenderer: FontRenderer,
    cellWidth: Int
) {
    val rowOffsets: IntArray
    val contentHeight: Int

    init {
        val heights = IntArray(definition.rowCount)
        val spanningComponents = ArrayList<Pair<DataFormComponent, Int>>()
        components.forEach { component ->
            val row = component.field.row
            if (row in heights.indices) {
                val controlWidth = max(
                    20,
                    cellWidth * component.field.columnSpan - DataFormMetrics.CONTROL_MARGIN * 2
                )
                val measuredHeight = component.measuredRowHeight(fontRenderer, controlWidth)
                if (component.field.rowSpan == 1) {
                    heights[row] = max(heights[row], measuredHeight)
                } else {
                    spanningComponents += component to measuredHeight
                }
            }
        }

        heights.indices.forEach { row ->
            if (heights[row] == 0) {
                heights[row] = DataFormMetrics.ROW_HEIGHT
            }
        }
        spanningComponents
            .sortedWith(compareBy({ it.first.field.rowSpan }, { it.first.field.row }, { it.first.field.column }))
            .forEach { (component, measuredHeight) ->
                ensureSpannedHeight(
                    heights,
                    component.field.row,
                    component.field.rowSpan,
                    measuredHeight
                )
            }

        rowOffsets = IntArray(heights.size)
        var offset = 0
        heights.forEachIndexed { index, height ->
            rowOffsets[index] = offset
            offset += height
        }
        contentHeight = offset
    }
}

private fun ensureSpannedHeight(heights: IntArray, startRow: Int, rowSpan: Int, requiredHeight: Int) {
    val endRow = (startRow + rowSpan).coerceAtMost(heights.size)
    val actualSpan = endRow - startRow
    if (actualSpan <= 0) {
        return
    }

    val currentHeight = (startRow until endRow).sumOf { heights[it] }
    val missingHeight = requiredHeight - currentHeight
    if (missingHeight <= 0) {
        return
    }

    val heightPerRow = missingHeight / actualSpan
    val remainder = missingHeight % actualSpan
    repeat(actualSpan) { offset ->
        heights[startRow + offset] += heightPerRow + if (offset < remainder) 1 else 0
    }
}
