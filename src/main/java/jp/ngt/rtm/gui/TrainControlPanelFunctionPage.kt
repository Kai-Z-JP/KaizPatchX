package jp.ngt.rtm.gui

import jp.kaiz.kaizpatch.gui.GuiButtonWithScrollingListBox
import org.lwjgl.input.Keyboard

class TrainControlPanelFunctionPage(gui: GuiTrainControlPanel) : TrainControlPanelPage(gui) {
    override fun init() {
        val buttons = gui.customButtons
        val tips = gui.customButtonTips
        for (i in buttons.indices) {
            val x = gui.panelLeft + 4 + (i % 3) * (54 + 3)
            val y = gui.panelTop + 4 + (i / 3) * (20 + 4)
            val button = GuiButtonWithScrollingListBox(
                CUSTOM_BUTTON_ID + i,
                x,
                y,
                54,
                20,
                { gui.panelDataMap.getInt("Button$i") },
                buttons[i].asList(),
                "%s"
            ) {
                gui.panelDataMap.setInt("Button$i", this, 3)
            }
            button.addTips(tips[i])
            gui.addPanelButton(button)
        }
    }

    override fun handleMouseWheel(delta: Int): Boolean {
        val step = if (delta > 0) -1 else 1
        val button = gui.panelButtons.firstOrNull { it.func_146115_a() && it.id >= CUSTOM_BUTTON_ID } ?: return false
        val index = button.id - CUSTOM_BUTTON_ID
        val values = gui.customButtons[index]
        var value = gui.panelDataMap.getInt("Button$index") + step
        if (value >= values.size) {
            value = 0
        } else if (value < 0) {
            value = values.size - 1
        }
        button.func_146113_a(gui.panelMinecraft.soundHandler)
        gui.panelDataMap.setInt("Button$index", value, 3)
        return true
    }

    override fun keyTyped(char: Char, keyCode: Int): Boolean {
        if (keyCode != Keyboard.KEY_F) {
            return false
        }

        val button = gui.panelButtons.firstOrNull { it.id >= CUSTOM_BUTTON_ID && it.func_146115_a() } ?: return false
        val index = button.id - CUSTOM_BUTTON_ID
        val value = gui.panelDataMap.getInt("Button$index")
        val formation = gui.panelTrain.formation ?: return true

        formation.trainStream
            .filter { it != null }
            .forEach { it.resourceState.dataMap.setInt("Button$index", value, 3) }
        button.func_146113_a(gui.panelMinecraft.soundHandler)
        return true
    }

    companion object {
        const val CUSTOM_BUTTON_ID = 2000
    }
}
