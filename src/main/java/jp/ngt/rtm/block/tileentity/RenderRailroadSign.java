package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderRailroadSign extends TileEntitySpecialRenderer {
    public void renderRailroadSignAt(TileEntityRailroadSign tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4, (float) par6 + 0.5F);

        GL11.glTranslatef(tileEntity.getOffsetX(), 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, tileEntity.getOffsetY(), 0.0F);
        GL11.glTranslatef(0.0F, 0.0F, tileEntity.getOffsetZ());

        GL11.glPushMatrix();
        float f0 = 1.25F;
        boolean flipVertical = tileEntity.getWorldObj().getBlock(tileEntity.xCoord, tileEntity.yCoord + 1, tileEntity.zCoord) != Blocks.air;
        if (flipVertical) {
            f0 = -0.25F;
        }
        GL11.glTranslatef(0.0F, f0, 0.0F);
        GL11.glRotatef(tileEntity.getRotation(), 0.0F, 1.0F, 0.0F);

        Tessellator tessellator = Tessellator.instance;
        this.bindTexture(tileEntity.getProperty().getTexture());
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.25D, -0.25D, 0.0675D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(0.25D, 0.25D, 0.0675D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-0.25D, 0.25D, 0.0675D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(-0.25D, -0.25D, 0.0675D, 0.0D, 1.0D);
        tessellator.draw();

        GL11.glDisable(GL11.GL_LIGHTING);
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(0);
        tessellator.addVertexWithUV(-0.25D, -0.25D, 0.0625D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(-0.25D, 0.25D, 0.0625D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(0.25D, 0.25D, 0.0625D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(0.25D, -0.25D, 0.0625D, 1.0D, 1.0D);
        tessellator.draw();
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (flipVertical) {
            GL11.glTranslatef(0.0F, -0.5F, 0.0F);
        }
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(0x404040);
        NGTRenderer.renderPole(tessellator, 0.0625D, 1.5D, false);//UV指定しない
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
        this.renderRailroadSignAt((TileEntityRailroadSign) par1, par2, par4, par6, par8);
    }
}