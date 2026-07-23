package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.kaiz.kaizpatch.gui.GuiButtonWithScrollingListBox
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11

internal data class DataFormClip(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    fun contains(x: Int, y: Int): Boolean = x >= left && x < right && y >= top && y < bottom

    fun intersects(x: Int, y: Int, width: Int, height: Int): Boolean =
        x < right && x + width > left && y < bottom && y + height > top

    inline fun withScissor(mc: Minecraft, draw: () -> Unit) {
        val scale = ScaledResolution(mc, mc.displayWidth, mc.displayHeight).scaleFactor
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT)
        try {
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(
                left * scale,
                mc.displayHeight - bottom * scale,
                (right - left).coerceAtLeast(0) * scale,
                (bottom - top).coerceAtLeast(0) * scale
            )
            draw()
        } finally {
            GL11.glPopAttrib()
        }
    }
}

@SideOnly(Side.CLIENT)
internal class ClippedButton(
    id: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    text: String,
    private val clip: DataFormClip
) : GuiButton(id, x, y, width, height, text) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        clip.withScissor(mc) {
            if (clip.contains(mouseX, mouseY)) {
                super.drawButton(mc, mouseX, mouseY)
            } else {
                super.drawButton(mc, -1, -1)
            }
        }
    }

    override fun mousePressed(mc: Minecraft, x: Int, y: Int): Boolean =
        clip.contains(x, y) && super.mousePressed(mc, x, y)
}

@SideOnly(Side.CLIENT)
internal class ClippedScrollingListButton(
    id: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    index: () -> Int,
    displayStringList: List<String>,
    onSelect: Int.() -> Unit,
    private val clip: DataFormClip
) : GuiButtonWithScrollingListBox(
    id,
    x,
    y,
    width,
    height,
    index,
    displayStringList,
    "%s",
    onSelect
) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        clip.withScissor(mc) {
            if (clip.contains(mouseX, mouseY)) {
                super.drawButton(mc, mouseX, mouseY)
            } else {
                super.drawButton(mc, -1, -1)
            }
        }
    }

    override fun mousePressed(mc: Minecraft, x: Int, y: Int): Boolean =
        clip.contains(x, y) && super.mousePressed(mc, x, y)
}
