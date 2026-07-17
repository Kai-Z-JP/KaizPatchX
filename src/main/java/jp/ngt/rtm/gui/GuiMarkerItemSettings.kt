package jp.ngt.rtm.gui

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.ngtlib.gui.GuiNumberField
import jp.ngt.ngtlib.gui.GuiScreenCustom
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.item.ItemBlockMarker
import jp.ngt.rtm.network.PacketMarkerItemSettings
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import org.lwjgl.input.Keyboard

@SideOnly(Side.CLIENT)
class GuiMarkerItemSettings(inventory: InventoryPlayer) : GuiScreenCustom() {
    private val hotbarSlot = inventory.currentItem
    private val currentHeight = ItemBlockMarker.getMarkerHeight(inventory.getStackInSlot(hotbarSlot))
    private lateinit var heightField: GuiNumberField

    override fun initGui() {
        super.initGui()

        val centerX = width / 2
        buttonList.clear()
        buttonList.add(GuiButton(0, centerX - 105, height - 28, 100, 20, I18n.format("gui.apply")))
        buttonList.add(GuiButton(1, centerX + 5, height - 28, 100, 20, I18n.format("gui.cancel")))

        heightField = setNumberField(width - 70, 20, 60, 20, currentHeight.toString(), false)
        heightField.isFocused = true
        heightField.setCursorPositionEnd()
        heightField.setSelectionPos(0)
        currentTextField = heightField
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)

        drawCenteredString(
            fontRendererObj,
            I18n.format("Rail Height"),
            width - 70,
            10,
            0xFFFFFF,
        )
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> applyAndClose()
            1 -> mc.displayGuiScreen(null)
        }
        super.actionPerformed(button)
    }

    override fun keyTyped(keyChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            applyAndClose()
        } else {
            super.keyTyped(keyChar, keyCode)
        }
    }

    private fun applyAndClose() {
        RTMCore.NETWORK_WRAPPER.sendToServer(PacketMarkerItemSettings(hotbarSlot, getHeightValue()))
        mc.displayGuiScreen(null)
    }

    private fun getHeightValue(): Int = try {
        ItemBlockMarker.clampMarkerHeight(heightField.text.toInt())
    } catch (_: NumberFormatException) {
        currentHeight
    }
}
