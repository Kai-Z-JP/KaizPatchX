package jp.ngt.rtm.gui

import jp.ngt.rtm.entity.train.protection.TrainProtectionPluginInfo
import jp.ngt.rtm.entity.train.protection.TrainProtectionPluginManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class TrainControlPanelProtectionPage(gui: GuiTrainControlPanel) : TrainControlPanelPage(gui) {
    private var protectionScroll = 0
    private var protectionPlugins: List<TrainProtectionPluginInfo> = emptyList()

    override fun init() {
        refreshProtectionPlugins()
        protectionScroll = 0
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton != 0 || !isInProtectionList(mouseX, mouseY)) {
            return false
        }

        refreshProtectionPlugins()
        val row = (mouseY - protectionListTop) / PROTECTION_ROW_HEIGHT
        val index = protectionScroll + row
        if (index !in protectionPlugins.indices) {
            return true
        }

        val plugin = protectionPlugins[index]
        val onX = protectionListLeft + PROTECTION_LIST_WIDTH - 62
        val offX = protectionListLeft + PROTECTION_LIST_WIDTH - 31
        val toggleY = protectionListTop + row * PROTECTION_ROW_HEIGHT + 2

        if (mouseY >= toggleY && mouseY < toggleY + PROTECTION_TOGGLE_HEIGHT) {
            if (mouseX >= onX && mouseX < onX + PROTECTION_TOGGLE_WIDTH) {
                setProtectionPluginState(plugin.id, true)
                return true
            }
            if (mouseX >= offX && mouseX < offX + PROTECTION_TOGGLE_WIDTH) {
                setProtectionPluginState(plugin.id, false)
                return true
            }
        }
        return true
    }

    private fun setProtectionPluginState(id: String, enabled: Boolean) {
        if (gui.panelTrain.isProtectionPluginEnabled(id) == enabled) {
            return
        }
        gui.playPanelClickSound()
        gui.sendProtectionPluginState(id, enabled)
    }

    override fun handleMouseWheel(delta: Int): Boolean {
        if (!isMouseInProtectionList()) {
            return false
        }
        scrollProtectionPlugins(if (delta > 0) -1 else 1)
        return true
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        refreshProtectionPlugins()
        val x = protectionListLeft
        val y = protectionListTop

        gui.panelFontRenderer.drawString("Train Protection", x, gui.panelTop + 6, 4210752)
        gui.drawPanelRect(
            x - 1,
            y - 1,
            x + PROTECTION_LIST_WIDTH + 1,
            y + PROTECTION_LIST_HEIGHT + 1,
            0xFF555555.toInt()
        )
        gui.drawPanelRect(x, y, x + PROTECTION_LIST_WIDTH, y + PROTECTION_LIST_HEIGHT, 0xFFE8E8E8.toInt())

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        gui.enableGuiScissor(x, y, PROTECTION_LIST_WIDTH, PROTECTION_LIST_HEIGHT)
        for (row in 0 until protectionVisibleRows) {
            val index = protectionScroll + row
            if (index >= protectionPlugins.size) {
                break
            }

            val plugin = protectionPlugins[index]
            val rowY = y + row * PROTECTION_ROW_HEIGHT
            val enabled = gui.panelTrain.isProtectionPluginEnabled(plugin.id)
            val hovered =
                mouseX >= x && mouseX < x + PROTECTION_LIST_WIDTH && mouseY >= rowY && mouseY < rowY + PROTECTION_ROW_HEIGHT
            gui.drawPanelRect(
                x,
                rowY,
                x + PROTECTION_LIST_WIDTH,
                rowY + PROTECTION_ROW_HEIGHT - 1,
                if (hovered) 0xFFE0E7F0.toInt() else 0xFFF4F4F4.toInt()
            )
            drawProtectionPluginName(plugin.displayName, x + 4, rowY + 5, PROTECTION_LIST_WIDTH - 70)
            drawProtectionToggle(x + PROTECTION_LIST_WIDTH - 62, rowY + 2, "ON", enabled)
            drawProtectionToggle(x + PROTECTION_LIST_WIDTH - 31, rowY + 2, "OFF", !enabled)
        }

        if (protectionPlugins.isEmpty()) {
            gui.panelFontRenderer.drawString("No plugins", x + 4, y + 5, 0x666666)
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)

        if (protectionMaxScroll > 0) {
            val barX = x + PROTECTION_LIST_WIDTH - 4
            val barHeight = max(12, PROTECTION_LIST_HEIGHT * protectionVisibleRows / protectionPlugins.size)
            val barTravel = PROTECTION_LIST_HEIGHT - barHeight
            val barY = y + barTravel * protectionScroll / protectionMaxScroll
            gui.drawPanelRect(barX, y, barX + 3, y + PROTECTION_LIST_HEIGHT, 0xFFB8B8B8.toInt())
            gui.drawPanelRect(barX, barY, barX + 3, barY + barHeight, 0xFF666666.toInt())
        }
    }

    private fun refreshProtectionPlugins() {
        protectionPlugins = TrainProtectionPluginManager.getPluginInfos()
        protectionScroll = max(0, min(protectionScroll, protectionMaxScroll))
    }

    private val protectionVisibleRows: Int
        get() = PROTECTION_LIST_HEIGHT / PROTECTION_ROW_HEIGHT

    private val protectionMaxScroll: Int
        get() = max(0, protectionPlugins.size - protectionVisibleRows)

    private val protectionListLeft: Int
        get() = gui.panelLeft + PROTECTION_LIST_X

    private val protectionListTop: Int
        get() = gui.panelTop + PROTECTION_LIST_Y

    private fun isMouseInProtectionList(): Boolean {
        val mouseX = Mouse.getEventX() * gui.screenWidth / gui.panelMinecraft.displayWidth
        val mouseY = gui.screenHeight - Mouse.getEventY() * gui.screenHeight / gui.panelMinecraft.displayHeight - 1
        return isInProtectionList(mouseX, mouseY)
    }

    private fun isInProtectionList(mouseX: Int, mouseY: Int): Boolean {
        val x = protectionListLeft
        val y = protectionListTop
        return mouseX >= x && mouseX < x + PROTECTION_LIST_WIDTH && mouseY >= y && mouseY < y + PROTECTION_LIST_HEIGHT
    }

    private fun scrollProtectionPlugins(amount: Int) {
        refreshProtectionPlugins()
        protectionScroll = max(0, min(protectionScroll + amount, protectionMaxScroll))
    }

    private fun drawProtectionPluginName(name: String, x: Int, y: Int, width: Int) {
        val text = gui.panelFontRenderer.trimStringToWidth(name, width)
        gui.panelFontRenderer.drawString(text, x, y, 0x303030)
    }

    private fun drawProtectionToggle(x: Int, y: Int, label: String, selected: Boolean) {
        val color = if (selected) 0xFF4E8F5A.toInt() else 0xFF9A9A9A.toInt()
        gui.drawPanelRect(x, y, x + PROTECTION_TOGGLE_WIDTH, y + PROTECTION_TOGGLE_HEIGHT, color)
        gui.drawPanelCenteredString(label, x + PROTECTION_TOGGLE_WIDTH / 2, y + 3, 0xFFFFFF)
    }

    companion object {
        private const val PROTECTION_ROW_HEIGHT = 18
        private const val PROTECTION_LIST_X = 8
        private const val PROTECTION_LIST_Y = 18
        private const val PROTECTION_LIST_WIDTH = 160
        private const val PROTECTION_LIST_HEIGHT = 108
        private const val PROTECTION_TOGGLE_WIDTH = 28
        private const val PROTECTION_TOGGLE_HEIGHT = 14
    }
}
