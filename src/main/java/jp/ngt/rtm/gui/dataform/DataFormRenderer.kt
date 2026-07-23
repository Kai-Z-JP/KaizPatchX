package jp.ngt.rtm.gui.dataform

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import kotlin.math.max

internal data class DataFormRenderState(
    val guiLeft: Int,
    val guiTop: Int,
    val width: Int,
    val height: Int,
    val viewportHeight: Int,
    val contentHeight: Int,
    val maxScrollOffset: Int,
    val scrollOffset: Int,
    val title: String,
    val hasFields: Boolean,
    val labels: List<DataFormLabel>,
    val clip: DataFormClip,
    val error: String?
)

internal object DataFormRenderer {
    fun drawBackground(state: DataFormRenderState) {
        Gui.drawRect(
            state.guiLeft,
            state.guiTop,
            state.guiLeft + state.width,
            state.guiTop + state.height,
            DataFormMetrics.BACKGROUND_COLOR
        )
        Gui.drawRect(
            state.guiLeft + DataFormMetrics.GRID_PADDING - 1,
            state.guiTop + DataFormMetrics.LIST_TOP - 1,
            state.guiLeft + state.width - DataFormMetrics.GRID_PADDING + 1,
            state.guiTop + state.height - DataFormMetrics.FOOTER_HEIGHT + 1,
            DataFormMetrics.BORDER_COLOR
        )
        Gui.drawRect(
            state.guiLeft + DataFormMetrics.GRID_PADDING,
            state.guiTop + DataFormMetrics.LIST_TOP,
            state.guiLeft + state.width - DataFormMetrics.GRID_PADDING,
            state.guiTop + state.height - DataFormMetrics.FOOTER_HEIGHT,
            DataFormMetrics.LIST_COLOR
        )

        if (state.maxScrollOffset > 0) {
            drawScrollBar(state)
        }
    }

    fun drawForeground(mc: Minecraft, fontRenderer: FontRenderer, state: DataFormRenderState) {
        fontRenderer.drawString(
            state.title,
            state.guiLeft + DataFormMetrics.GRID_PADDING,
            state.guiTop + DataFormMetrics.TITLE_TOP,
            DataFormMetrics.TEXT_COLOR
        )

        if (!state.hasFields) {
            fontRenderer.drawString(
                "No configurable fields",
                state.guiLeft + DataFormMetrics.GRID_PADDING + 4,
                state.guiTop + DataFormMetrics.LIST_TOP + 6,
                DataFormMetrics.MUTED_TEXT_COLOR
            )
        } else {
            state.clip.withScissor(mc) {
                state.labels.forEach { label ->
                    val text = fontRenderer.trimStringToWidth(label.text, label.width)
                    fontRenderer.drawString(
                        text,
                        state.guiLeft + label.x,
                        state.guiTop + label.y,
                        label.color
                    )
                }
            }
        }

        state.error?.let { error ->
            val text = fontRenderer.trimStringToWidth(
                error,
                state.width - DataFormMetrics.GRID_PADDING * 2
            )
            fontRenderer.drawString(
                text,
                state.guiLeft + DataFormMetrics.GRID_PADDING,
                state.guiTop + state.height - DataFormMetrics.FOOTER_HEIGHT + 2,
                DataFormMetrics.ERROR_COLOR
            )
        }
    }

    private fun drawScrollBar(state: DataFormRenderState) {
        val trackTop = state.guiTop + DataFormMetrics.LIST_TOP
        val barHeight = max(
            DataFormMetrics.MIN_SCROLL_BAR_HEIGHT,
            state.viewportHeight * state.viewportHeight / state.contentHeight
        )
        val barTravel = state.viewportHeight - barHeight
        val barTop = trackTop + barTravel * state.scrollOffset / state.maxScrollOffset
        val right = state.guiLeft + state.width - DataFormMetrics.GRID_PADDING
        Gui.drawRect(
            right - 3,
            trackTop,
            right,
            trackTop + state.viewportHeight,
            DataFormMetrics.SCROLL_TRACK_COLOR
        )
        Gui.drawRect(
            right - 3,
            barTop,
            right,
            barTop + barHeight,
            DataFormMetrics.SCROLL_BAR_COLOR
        )
    }
}
