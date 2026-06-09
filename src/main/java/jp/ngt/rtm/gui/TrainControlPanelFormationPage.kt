package jp.ngt.rtm.gui

import jp.ngt.rtm.entity.train.util.FormationEntry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import org.lwjgl.opengl.GL11

class TrainControlPanelFormationPage(gui: GuiTrainControlPanel) : TrainControlPanelPage(gui) {
    override fun init() {
        val formation = gui.panelTrain.formation ?: return
        for (i in 0 until formation.size()) {
            val entry = formation[i] ?: continue
            val v = if (i == 0) 0 else if (i == formation.size() - 1) 2 else 1
            val x = gui.panelLeft + 8 + (i % 5) * 32
            val y = gui.panelTop + 25 + (i / 5) * 32
            gui.addPanelButton(TrainFormationButton(200 + i, entry, x, y, v))
        }
    }
}

private class TrainFormationButton(
    id: Int,
    private val car: FormationEntry,
    posX: Int,
    posY: Int,
    private val v: Int
) : GuiButton(id, posX, posY, 32, 16, (car.entryId + 1).toString()) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (!visible) {
            return
        }

        mc.textureManager.bindTexture(TabTrainControlPanel.TAB_Formation.texture)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        val u = if (car.train.isControlCar) 1 else 0
        field_146123_n =
            mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
        drawTexturedModalRect(xPosition, yPosition, 192 + u * 32, v * 16, width, height)
        mouseDragged(mc, mouseX, mouseY)

        if (car.train.riddenByEntity == mc.thePlayer) {
            drawTexturedModalRect(xPosition + 12, yPosition - 16, 180, 0, 10, 16)
        }

        drawCenteredString(mc.fontRenderer, displayString, xPosition + width / 2, yPosition + 2, 0x000000)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return super.mousePressed(mc, mouseX, mouseY)
    }
}
