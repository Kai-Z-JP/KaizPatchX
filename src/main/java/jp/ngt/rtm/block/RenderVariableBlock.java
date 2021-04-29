package jp.ngt.rtm.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public class RenderVariableBlock implements ISimpleBlockRenderingHandler {
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            Tessellator tessellator = Tessellator.instance;
            IIcon icon = renderer.getBlockIconFromSideAndMetadata(block, 0, 0);
            if (renderer.hasOverrideBlockTexture()) icon = renderer.overrideBlockTexture;

            double d3 = icon.getMinU();
            double d4 = icon.getMinV();
            double d5 = icon.getMaxU();
            double d6 = icon.getMaxV();
            double[] p0 = new double[]{0.0D, 0.0D, 0.0D};
            double[] p1 = new double[]{0.0D, 0.0D, 1.0D};
            double[] p2 = new double[]{1.0D, 0.0D, 1.0D};
            double[] p3 = new double[]{1.0D, 0.0D, 0.0D};
            double[] p4 = new double[]{0.0D, 1.0D, 0.0D};
            double[] p5 = new double[]{0.0D, 1.0D, 1.0D};
            double[] p6 = new double[]{1.0D, 1.0D, 1.0D};
            double[] p7 = new double[]{1.0D, 1.0D, 0.0D};
            boolean flagX0 = world.getBlock(x - 1, y, z).isOpaqueCube() || world.getBlock(x - 1, y, z) == RTMBlock.variableBlock;
            boolean flagX1 = world.getBlock(x + 1, y, z).isOpaqueCube() || world.getBlock(x + 1, y, z) == RTMBlock.variableBlock;
            boolean flagY0 = world.getBlock(x, y - 1, z).isOpaqueCube() || world.getBlock(x, y - 1, z) == RTMBlock.variableBlock;
            boolean flagY1 = world.getBlock(x, y + 1, z).isOpaqueCube() || world.getBlock(x, y + 1, z) == RTMBlock.variableBlock;
            boolean flagZ0 = world.getBlock(x, y, z - 1).isOpaqueCube() || world.getBlock(x, y, z - 1) == RTMBlock.variableBlock;
            boolean flagZ1 = world.getBlock(x, y, z + 1).isOpaqueCube() || world.getBlock(x, y, z + 1) == RTMBlock.variableBlock;

            if (!flagX0 && !flagY0 && !flagZ0) {
                p0[0] = 0.5D;
                p0[1] = 0.5D;
                p0[2] = 0.5D;
            }
            if (!flagX0 && !flagY0 && !flagZ1) {
                p1[0] = 0.5D;
                p1[1] = 0.5D;
                p1[2] = 0.5D;
            }
            if (!flagX1 && !flagY0 && !flagZ1) {
                p2[0] = 0.5D;
                p2[1] = 0.5D;
                p2[2] = 0.5D;
            }
            if (!flagX1 && !flagY0 && !flagZ0) {
                p3[0] = 0.5D;
                p3[1] = 0.5D;
                p3[2] = 0.5D;
            }
            if (!flagX0 && !flagY1 && !flagZ0) {
                p4[0] = 0.5D;
                p4[1] = 0.5D;
                p4[2] = 0.5D;
            }
            if (!flagX0 && !flagY1 && !flagZ1) {
                p5[0] = 0.5D;
                p5[1] = 0.5D;
                p5[2] = 0.5D;
            }
            if (!flagX1 && !flagY1 && !flagZ1) {
                p6[0] = 0.5D;
                p6[1] = 0.5D;
                p6[2] = 0.5D;
            }
            if (!flagX1 && !flagY1 && !flagZ0) {
                p7[0] = 0.5D;
                p7[1] = 0.5D;
                p7[2] = 0.5D;
            }

            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            if (true)//flagX0)
            {
                tessellator.addVertexWithUV(x + p6[0], y + p6[1], z + p6[2], d3, d4);
                tessellator.addVertexWithUV(x + p2[0], y + p2[1], z + p2[2], d3, d6);
                tessellator.addVertexWithUV(x + p3[0], y + p3[1], z + p3[2], d5, d6);
                tessellator.addVertexWithUV(x + p7[0], y + p7[1], z + p7[2], d5, d4);
            }

            if (true)//flagX1)
            {
                tessellator.addVertexWithUV(x + p4[0], y + p4[1], z + p4[2], d3, d4);
                tessellator.addVertexWithUV(x + p0[0], y + p0[1], z + p0[2], d3, d6);
                tessellator.addVertexWithUV(x + p1[0], y + p1[1], z + p1[2], d5, d6);
                tessellator.addVertexWithUV(x + p5[0], y + p5[1], z + p5[2], d5, d4);
            }

            if (true)//flagY0)
            {
                tessellator.addVertexWithUV(x + p3[0], y + p3[1], z + p3[2], d3, d4);
                tessellator.addVertexWithUV(x + p2[0], y + p2[1], z + p2[2], d3, d6);
                tessellator.addVertexWithUV(x + p1[0], y + p1[1], z + p1[2], d5, d6);
                tessellator.addVertexWithUV(x + p0[0], y + p0[1], z + p0[2], d5, d4);
            }

            if (true)//flagY1)
            {
                tessellator.addVertexWithUV(x + p6[0], y + p6[1], z + p6[2], d3, d4);
                tessellator.addVertexWithUV(x + p7[0], y + p7[1], z + p7[2], d3, d6);
                tessellator.addVertexWithUV(x + p4[0], y + p4[1], z + p4[2], d5, d6);
                tessellator.addVertexWithUV(x + p5[0], y + p5[1], z + p5[2], d5, d4);
            }

            if (true)//flagZ0)
            {
                tessellator.addVertexWithUV(x + p7[0], y + p7[1], z + p7[2], d3, d4);
                tessellator.addVertexWithUV(x + p3[0], y + p3[1], z + p3[2], d3, d6);
                tessellator.addVertexWithUV(x + p0[0], y + p0[1], z + p0[2], d5, d6);
                tessellator.addVertexWithUV(x + p4[0], y + p4[1], z + p4[2], d5, d4);
            }

            if (true)//flagZ1)
            {
                tessellator.addVertexWithUV(x + p5[0], y + p5[1], z + p5[2], d3, d4);
                tessellator.addVertexWithUV(x + p1[0], y + p1[1], z + p1[2], d3, d6);
                tessellator.addVertexWithUV(x + p2[0], y + p2[1], z + p2[2], d5, d6);
                tessellator.addVertexWithUV(x + p6[0], y + p6[1], z + p6[2], d5, d4);
            }

			/*Tessellator tessellator = Tessellator.instance;
			Icon icon = renderer.getBlockIconFromSideAndMetadata(block, 0, 0);
	        if(renderer.hasOverrideBlockTexture())icon = renderer.overrideBlockTexture;
			float[][] sp = ModelSolid.sphere;
			double minU = (double)icon.getMinU();
			double maxU = (double)icon.getMaxU();
			double minV = (double)icon.getMinV();
			double maxV = (double)icon.getMaxV();
			double r = 0.5D;

			for(int i0 = 0; i0 < 8; ++i0)
			{
				for(int i1 = 0; i1 < 16; ++i1)
				{
					int ii0 = i0 * 16 + i1;
					int ii1 = (i0 + 1) * 16 + i1;
					int ii2 = (i0 + 1) * 16 + (i1 + 1) % 16;
					int ii3 = i0 * 16 + (i1 + 1) % 16;
			        tessellator.addVertexWithUV(x + 0.5D + sp[ii3][0] * r, y + 0.5D + sp[ii3][1] * r, z + 0.5D + sp[ii3][2] * r, maxU, maxV);
			        tessellator.addVertexWithUV(x + 0.5D + sp[ii2][0] * r, y + 0.5D + sp[ii2][1] * r, z + 0.5D + sp[ii2][2] * r, maxU, minV);
			        tessellator.addVertexWithUV(x + 0.5D + sp[ii1][0] * r, y + 0.5D + sp[ii1][1] * r, z + 0.5D + sp[ii1][2] * r, minU, minV);
			        tessellator.addVertexWithUV(x + 0.5D + sp[ii0][0] * r, y + 0.5D + sp[ii0][1] * r, z + 0.5D + sp[ii0][2] * r, minU, maxV);
				}
			}*/

            //tessellator.startDrawing(GL11.GL_TRIANGLES);

            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int par1) {
        return false;
    }

    @Override
    public int getRenderId() {
        return RTMBlock.renderIdVariableBlock;
    }
}