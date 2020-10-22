package jp.ngt.rtm.rail;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public class RenderBlockLargeRail implements ISimpleBlockRenderingHandler {
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		if (modelId == this.getRenderId()) {
			TileEntityLargeRailBase tile = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);

			TileEntityLargeRailCore core = tile.getRailCore();
			if (core == null) {
				renderer.renderStandardBlock(Blocks.bedrock, x, y, z);
				return true;
			}

			RailProperty prop = core.getProperty();
			if (prop == null) {
				renderer.renderStandardBlock(Blocks.bedrock, x, y, z);
				return true;
			}

			if (prop.block == Blocks.air) {
				return true;
			}

			float[] fa = tile.getBlockHeights(x, y, z, prop.blockHeight, false);
			double y0 = fa[0];
			double y1 = fa[1];
			double y2 = fa[2];
			double y3 = fa[3];

			Tessellator tessellator = Tessellator.instance;
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

	        /*if(((BlockLargeRailBase)block).railTextureType == 4)//雪の厚み
	        {
	        	y0 += 0.18125F;//元0.0625+0.125
	        	y1 += 0.18125F;
	        	y2 += 0.18125F;
	        	y3 += 0.18125F;
	        }*/

	        /*IIcon icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 0, 0);
	        double minU = (double)icon.getMinU();
	        double maxU = (double)icon.getMaxU();
	        double maxV = (double)icon.getMaxV();
	        double v = maxV - (double)icon.getMinV();*/

			IIcon icon;
			double minU;
			double maxU;
			double maxV;
			double v;

			Block sideBlock = world.getBlock(x, y, z + 1);
			if (!sideBlock.isOpaqueCube() && !(sideBlock instanceof BlockLargeRailBase)) {
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 3, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
				tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 1.0D, minU, maxV - (v * this.getV(y0)));
				tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 1.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 1.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y + y1, z + 1.0D, maxU, maxV - (v * this.getV(y1)));
			}

			sideBlock = world.getBlock(x + 1, y, z);
			if (!sideBlock.isOpaqueCube() && !(sideBlock instanceof BlockLargeRailBase)) {
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 5, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
				tessellator.addVertexWithUV(x + 1.0D, y + y1, z + 1.0D, minU, maxV - (v * this.getV(y1)));
				tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 1.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 0.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 0.0D, maxU, maxV - (v * this.getV(y2)));
			}

			sideBlock = world.getBlock(x, y, z - 1);
			if (!sideBlock.isOpaqueCube() && !(sideBlock instanceof BlockLargeRailBase)) {
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 2, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
				tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 0.0D, minU, maxV - (v * this.getV(y2)));
				tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 0.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 0.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y + y3, z + 0.0D, maxU, maxV - (v * this.getV(y3)));
			}

			sideBlock = world.getBlock(x - 1, y, z);
			if (!sideBlock.isOpaqueCube() && !(sideBlock instanceof BlockLargeRailBase)) {
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 4, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
				tessellator.addVertexWithUV(x + 0.0D, y + y3, z + 0.0D, minU, maxV - (v * this.getV(y3)));
				tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 0.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 1.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 1.0D, maxU, maxV - (v * this.getV(y0)));
			}

			//上面
			{
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 1, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
				tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 1.0D, minU, maxV - v);
				tessellator.addVertexWithUV(x + 1.0D, y + y1, z + 1.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 0.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y + y3, z + 0.0D, maxU, maxV - v);
			}

			//下面
			if (!world.getBlock(x, y - 1, z).isOpaqueCube()) {
				icon = renderer.getBlockIconFromSideAndMetadata(prop.block, 0, prop.blockMetadata);
				minU = icon.getMinU();
				maxU = icon.getMaxU();
				maxV = icon.getMaxV();
				v = maxV - (double) icon.getMinV();

				tessellator.setColorOpaque_F(0.5F, 0.5F, 0.5F);
				tessellator.addVertexWithUV(x + 0.0D, y, z + 0.0D, minU, maxV - v);
				tessellator.addVertexWithUV(x + 1.0D, y, z + 0.0D, minU, maxV);
				tessellator.addVertexWithUV(x + 1.0D, y, z + 1.0D, maxU, maxV);
				tessellator.addVertexWithUV(x + 0.0D, y, z + 1.0D, maxU, maxV - v);
			}

			return true;
		}
		return false;
	}

	private double getV(double par1) {
		return par1 > 1.0D ? 1.0D : (par1 < 0.0D ? 0.0D : par1);
	}

	@Override
	public boolean shouldRender3DInInventory(int par1) {
		return false;
	}

	@Override
	public int getRenderId() {
		return RTMBlock.renderIdBlockRail;
	}
}