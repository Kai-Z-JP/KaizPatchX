package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButtonColored extends GuiButton {
    private final int color;
    private final int textColor;

    public GuiButtonColored(int id, int xPos, int yPos, int w, int h, String text, int c1, int c2) {
        super(id, xPos, yPos, w, h, text);
        this.color = c1;
        this.textColor = c2;
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.renderBackground();
            this.mouseDragged(mc, x, y);

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, this.textColor);
        }
    }

    private void renderBackground() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(this.color);
        tessellator.addVertex(this.xPosition, this.yPosition + this.height, this.zLevel);
        tessellator.addVertex(this.xPosition + this.width, this.yPosition + this.height, this.zLevel);
        tessellator.addVertex(this.xPosition + this.width, this.yPosition, this.zLevel);
        tessellator.addVertex(this.xPosition, this.yPosition, this.zLevel);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}