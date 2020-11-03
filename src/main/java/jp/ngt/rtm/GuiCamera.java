package jp.ngt.rtm;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import net.minecraft.entity.player.EntityPlayer;

@SideOnly(Side.CLIENT)
public class GuiCamera extends GuiScreenCustom {
    public GuiCamera(EntityPlayer player) {
    }

    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);
        int halfW = this.width / 2;
        this.drawCenteredString(this.fontRendererObj, "R", halfW - 90, 25, 16711680);
        this.drawCenteredString(this.fontRendererObj, "G", halfW - 90, 45, 65280);
        this.drawCenteredString(this.fontRendererObj, "B", halfW - 90, 65, 255);
        this.drawCenteredString(this.fontRendererObj, "Hex", halfW - 90, 90, 16777215);
        this.drawCenteredString(this.fontRendererObj, "Alpha", halfW - 95, 115, 16777215);
        this.drawCenteredString(this.fontRendererObj, "Radius", halfW + 12, 65, 16777215);
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertex((halfW + 8), 20.0F, this.zLevel);
        tessellator.addVertex((halfW + 8), 52.0F, this.zLevel);
        tessellator.addVertex((halfW + 40), 52.0F, this.zLevel);
        tessellator.addVertex((halfW + 40), 20.0F, this.zLevel);
        tessellator.draw();
    }
}
