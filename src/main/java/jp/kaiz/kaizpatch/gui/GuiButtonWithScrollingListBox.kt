package jp.kaiz.kaizpatch.gui

import jp.ngt.ngtlib.gui.GuiButtonCustom
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GL11
import kotlin.math.min


class GuiButtonWithScrollingListBox(
    id: Int,
    xPosition: Int,
    yPosition: Int,
    width: Int,
    height: Int,
    private var index: () -> Int,
    private val displayStringList: List<String>,
    private val displayFormat: String,
    private val onSelect: Int.() -> Unit
) : GuiButtonCustom(id, xPosition, yPosition, width, height, "") {

    private lateinit var listScreen: GuiScrollingListBox

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            mc.textureManager.bindTexture(buttonTextures)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            field_146123_n =
                mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
            val k = getHoverState(field_146123_n)
            GL11.glEnable(GL11.GL_BLEND)
            OpenGlHelper.glBlendFunc(770, 771, 1, 0)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            drawTexturedModalRect(xPosition, yPosition, 0, 46 + k * 20, width / 2, height)
            drawTexturedModalRect(
                xPosition + width / 2,
                yPosition, 200 - width / 2, 46 + k * 20, width / 2, height
            )
            mouseDragged(mc, mouseX, mouseY)
            var l = 14737632
            if (packedFGColour != 0) {
                l = packedFGColour
            } else if (!enabled) {
                l = 10526880
            } else if (field_146123_n) {
                l = 16777120
            }
            val currentIndex = index()
            drawCenteredString(
                mc.fontRenderer,
                if (displayStringList.size > currentIndex) displayFormat.format(displayStringList[currentIndex])
                else displayFormat.format("null"),
                xPosition + width / 2, yPosition + (height - 8) / 2, l
            )
        }
    }

    override fun mousePressed(mc: Minecraft, x: Int, y: Int): Boolean {
        val inRange = enabled && visible &&
                x >= xPosition && y >= yPosition &&
                x < xPosition + super.width && y < yPosition + super.height
        if (inRange) {
            listScreen = GuiScrollingListBox(
                mc.currentScreen,
                xPosition,
                yPosition + height,
                width,
                min(
                    displayStringList.size * 10 + 10,
                    mc.currentScreen.height - yPosition - height - 5
                ),
                index,
                displayStringList,
                onSelect
            )
            this.enabled = false
            mc.displayGuiScreen(listScreen)
        }
        return inRange
    }
}