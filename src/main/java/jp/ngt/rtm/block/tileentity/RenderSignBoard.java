package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.texture.SignBoardProperty;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderSignBoard extends TileEntitySpecialRenderer {
    public void renderSignBoardAt(TileEntitySignBoard tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);

        SignBoardProperty sbp = tileEntity.getProperty();
        float height = sbp.height / 2.0F;
        float width = sbp.width / 2.0F;
        float depth = sbp.depth / 2.0F;
        int meta = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);//getBlockMetadata();
        byte dir = tileEntity.getDirection();
        float minV = 0.0F;
        float maxV = 1.0F;
        if (sbp.frame > 1) {
            minV = (float) (tileEntity.counter / sbp.animationCycle) / ((float) sbp.frame);
            maxV = (float) ((tileEntity.counter / sbp.animationCycle) + 1) / ((float) sbp.frame);
        }

        GL11.glRotatef((float) dir * -90.0F, 0.0F, 1.0F, 0.0F);

        if (meta == 0) {
            GL11.glTranslatef(0.0F, 0.5F - height, 0.0F);//up
        } else if (meta == 1) {
            GL11.glTranslatef(0.0F, height - 0.5F, 0.0F);//down
        } else {
            //v24で変更
            if ((dir == 1 && meta == 4) || (dir == 3 && meta == 5)) {
                //GL11.glTranslatef(0.0F, 0.0F, 0.5F - depth);
                GL11.glTranslatef(0.0F, 0.0F, depth - 0.5F);
            } else if ((dir == 0 && meta == 3) || (dir == 2 && meta == 2)) {
                GL11.glTranslatef(0.0F, 0.0F, depth - 0.5F);
            } else if ((dir == 1 && meta == 3) || (dir == 3 && meta == 2)) {
                //GL11.glTranslatef(0.5F - width, 0.0F, 0.0F);
                GL11.glTranslatef(width - 0.5F, 0.0F, 0.0F);
            } else if ((dir == 0 && meta == 4) || (dir == 2 && meta == 5)) {
                //
                GL11.glTranslatef(0.5F - width, 0.0F, 0.0F);
            } else if ((dir == 0 && meta == 5) || (dir == 2 && meta == 4)) {
                //
                GL11.glTranslatef(width - 0.5F, 0.0F, 0.0F);
            } else {
                //GL11.glTranslatef(width - 0.5F, 0.0F, 0.0F);
                GL11.glTranslatef(0.5F - width, 0.0F, 0.0F);
            }
        }

        GL11.glTranslatef(tileEntity.getOffsetX(), tileEntity.getOffsetY(), tileEntity.getOffsetZ());
        GL11.glRotatef(tileEntity.getRotation(), 0.0F, 1.0F, 0.0F);

        GL11.glDisable(GL11.GL_LIGHTING);
        Tessellator tessellator = Tessellator.instance;
        this.bindTexture(sbp.getTexture());
        double u0 = sbp.backTexture == 1 ? 0.5D : 1.0D;
        double u1 = sbp.backTexture == 1 ? 0.5D : 0.0D;
        tessellator.startDrawingQuads();
        //Front
        tessellator.addVertexWithUV(width, -height, depth, u0, maxV);
        tessellator.addVertexWithUV(width, height, depth, u0, minV);
        tessellator.addVertexWithUV(-width, height, depth, 0.0D, minV);
        tessellator.addVertexWithUV(-width, -height, depth, 0.0D, maxV);

        int color = sbp.color;
        boolean flag1 = false;
        if (sbp.backTexture == 2) {
            tessellator.draw();
            flag1 = true;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(color, 255);
        }

        //Back
        tessellator.addVertexWithUV(-width, -height, -depth, 1.0D, maxV);
        tessellator.addVertexWithUV(-width, height, -depth, 1.0D, minV);
        tessellator.addVertexWithUV(width, height, -depth, u1, minV);
        tessellator.addVertexWithUV(width, -height, -depth, u1, maxV);
        tessellator.draw();

		/*if(flag1)
		{
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}*/

        //GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        color -= 0x101010;//影
        if (color < 0) {
            color = 0;
        }
        tessellator.setColorRGBA_I(color, 255);
        //Top
        tessellator.addVertex(width, height, depth);
        tessellator.addVertex(width, height, -depth);
        tessellator.addVertex(-width, height, -depth);
        tessellator.addVertex(-width, height, depth);
        //Bottom
        tessellator.addVertex(-width, -height, depth);
        tessellator.addVertex(-width, -height, -depth);
        tessellator.addVertex(width, -height, -depth);
        tessellator.addVertex(width, -height, depth);
        //Left
        tessellator.addVertex(width, -height, -depth);
        tessellator.addVertex(width, height, -depth);
        tessellator.addVertex(width, height, depth);
        tessellator.addVertex(width, -height, depth);
        //Right
        tessellator.addVertex(-width, -height, depth);
        tessellator.addVertex(-width, height, depth);
        tessellator.addVertex(-width, height, -depth);
        tessellator.addVertex(-width, -height, -depth);

        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderSignBoardAt((TileEntitySignBoard) tileEntity, par2, par4, par6, par8);
    }
}