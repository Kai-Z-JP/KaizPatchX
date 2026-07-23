package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.DataFormField
import net.minecraft.client.gui.FontRenderer
import kotlin.math.max

@SideOnly(Side.CLIENT)
internal class TextFormComponent(
    override val field: DataFormField
) : DataFormComponent {
    private val mutableLabels = ArrayList<DataFormLabel>(1)

    override val rowHeight: Int = DataFormMetrics.TEXT_ROW_HEIGHT
    override val labels: List<DataFormLabel>
        get() = mutableLabels

    override fun measuredRowHeight(fontRenderer: FontRenderer, controlWidth: Int): Int = max(
        DataFormMetrics.TEXT_ROW_HEIGHT,
        DataFormMetrics.CONTROL_MARGIN * 2 +
                wrappedLines(fontRenderer, controlWidth).size * DataFormMetrics.TEXT_LINE_HEIGHT
    )

    override fun build(context: DataFormBuildContext) {
        mutableLabels.clear()
        wrappedLines(context.fontRenderer, context.controlWidth).forEachIndexed { index, line ->
            val lineY = context.localY + DataFormMetrics.CONTROL_MARGIN +
                    index * DataFormMetrics.TEXT_LINE_HEIGHT
            if (context.isControlVisible(lineY, DataFormMetrics.LABEL_HEIGHT)) {
                mutableLabels += DataFormLabel(
                    line,
                    context.localX + DataFormMetrics.CONTROL_MARGIN,
                    lineY,
                    context.controlWidth
                )
            }
        }
    }

    private fun wrappedLines(fontRenderer: FontRenderer, controlWidth: Int): List<String> =
        fontRenderer.listFormattedStringToWidth(field.resolvedText(), max(1, controlWidth))
            .filterIsInstance<String>()
            .ifEmpty { listOf("") }
}
