package jp.ngt.rtm.gui

import net.minecraft.client.gui.GuiButton

abstract class TrainControlPanelPage(protected val gui: GuiTrainControlPanel) {
    open fun init() {
    }

    open fun actionPerformed(button: GuiButton): Boolean {
        return false
    }

    open fun handleMouseWheel(delta: Int): Boolean {
        return false
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    open fun keyTyped(char: Char, keyCode: Int): Boolean {
        return false
    }
}
