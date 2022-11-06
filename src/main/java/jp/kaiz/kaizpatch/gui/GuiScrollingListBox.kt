package jp.kaiz.kaizpatch.gui

import cpw.mods.fml.client.GuiScrollingList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

class GuiScrollingListBox(
    private val parentScreen: GuiScreen,
    private val xPosition: Int,
    private val yPosition: Int,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val index: () -> Int,
    private val displayStringList: List<String>,
    private val onSelect: Int.() -> Unit
) : GuiScreen() {
    private val guiScroll = object : GuiScrollingList(
        this.parentScreen.mc, this.screenWidth, this.screenHeight,
        this.yPosition, this.yPosition + this.screenHeight, this.xPosition,
        10
    ) {
        override fun getSize(): Int = this@GuiScrollingListBox.displayStringList.size

        override fun elementClicked(index: Int, doubleClick: Boolean) {
            if (index != this@GuiScrollingListBox.index()) {
                this@GuiScrollingListBox.onSelect(index)
            }
        }

        override fun isSelected(index: Int): Boolean {
            return index == this@GuiScrollingListBox.index()
        }

        override fun drawBackground() {
        }

        override fun drawSlot(index: Int, right: Int, top: Int, height: Int, tessellator: Tessellator) {
            this@GuiScrollingListBox.drawCenteredString(
                this@GuiScrollingListBox.mc.fontRenderer,
                this@GuiScrollingListBox.displayStringList[index],
                this.left + (this.listWidth - 6) / 2,
                top - 1,
                0xFFFFFF
            )
        }
    }

    override fun setWorldAndResolution(mc: Minecraft, scaledWidth: Int, scaledHeight: Int) {
        if (this.mc != null && this.width != scaledWidth && this.height != scaledHeight) {
            super.setWorldAndResolution(mc, scaledWidth, scaledHeight)
            this.parentScreen.setWorldAndResolution(mc, scaledWidth, scaledHeight)
            mc.displayGuiScreen(this.parentScreen)
        } else {
            super.setWorldAndResolution(mc, scaledWidth, scaledHeight)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (this.inRange(mouseX, mouseY)) {
            this.parentScreen.drawScreen(-1, -1, partialTicks)
        } else {
            this.parentScreen.drawScreen(mouseX, mouseY, partialTicks)
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
        val scale = ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight).scaleFactor


        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(
            this.xPosition * scale,
            Minecraft.getMinecraft().displayHeight - (this.yPosition + this.screenHeight) * scale,
            this.screenWidth * scale,
            this.screenHeight * scale
        )
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
        this.guiScroll.drawScreen(mouseX, mouseY, partialTicks)
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL11.glPopMatrix()
    }

    override fun mouseClicked(x: Int, y: Int, mouseEvent: Int) {
        if (!this.inRange(x, y)) {
            this.mc.displayGuiScreen(this.parentScreen)
        }
    }

    private fun inRange(x: Int, y: Int): Boolean {
        return x >= xPosition && y >= yPosition && x < xPosition + screenWidth && y < yPosition + screenHeight
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}