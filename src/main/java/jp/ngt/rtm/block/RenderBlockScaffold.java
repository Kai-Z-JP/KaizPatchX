package jp.ngt.rtm.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityScaffold;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public class RenderBlockScaffold implements ISimpleBlockRenderingHandler {
	private static final float F0 = 0.0625F;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		if (modelId == this.getRenderId()) {
			NGTRenderer.renderBlock(F0 * 0.0F, F0 * 0.0F, F0 * 0.0F, F0 * 16.0F, F0 * 1.0F, F0 * 16.0F, renderer, block);

			NGTRenderer.renderBlock(F0 * 15.0F, F0 * 15.0F, F0 * -1.0F, F0 * 17.0F, F0 * 17.0F, F0 * 17.0F, renderer, block);

			NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 1.5F, F0 * 16.5F, F0 * 16.0F, F0 * 2.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 5.5F, F0 * 16.5F, F0 * 16.0F, F0 * 6.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 9.5F, F0 * 16.5F, F0 * 16.0F, F0 * 10.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 13.5F, F0 * 16.5F, F0 * 16.0F, F0 * 14.5F, renderer, block);

			NGTRenderer.renderBlock(F0 * -1.0F, F0 * 15.0F, F0 * -1.0F, F0 * 1.0F, F0 * 17.0F, F0 * 17.0F, renderer, block);

			NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 1.5F, F0 * 0.5F, F0 * 16.0F, F0 * 2.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 5.5F, F0 * 0.5F, F0 * 16.0F, F0 * 6.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 9.5F, F0 * 0.5F, F0 * 16.0F, F0 * 10.5F, renderer, block);
			NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 13.5F, F0 * 0.5F, F0 * 16.0F, F0 * 14.5F, renderer, block);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		if (modelId == this.getRenderId()) {
			NGTRenderer.renderBlock(F0 * 0.0F, F0 * 0.0F, F0 * 0.0F, F0 * 16.0F, F0 * 1.0F, F0 * 16.0F, true, renderer, block, x, y, z);

			NGTRenderer.renderBlock(F0 * 0.0F, F0 * -4.0F, F0 * 15.0F, F0 * 16.0F, F0 * 0.0F, F0 * 16.0F, true, renderer, block, x, y, z);
			NGTRenderer.renderBlock(F0 * 0.0F, F0 * -4.0F, F0 * 0.0F, F0 * 16.0F, F0 * 0.0F, F0 * 1.0F, true, renderer, block, x, y, z);
			NGTRenderer.renderBlock(F0 * 15.0F, F0 * -4.0F, F0 * 0.0F, F0 * 16.0F, F0 * 0.0F, F0 * 16.0F, true, renderer, block, x, y, z);
			NGTRenderer.renderBlock(F0 * 0.0F, F0 * -4.0F, F0 * 0.0F, F0 * 1.0F, F0 * 0.0F, F0 * 16.0F, true, renderer, block, x, y, z);

			boolean b0 = true;
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityScaffold) {
				b0 = ((TileEntityScaffold) tile).getDir() == 0;
			}
			byte flag0 = BlockScaffold.getConnectionType(world, x + 1, y, z);
			byte flag1 = BlockScaffold.getConnectionType(world, x - 1, y, z);
			byte flag2 = BlockScaffold.getConnectionType(world, x, y, z + 1);
			byte flag3 = BlockScaffold.getConnectionType(world, x, y, z - 1);

			if ((b0 && flag0 == 0) || (!b0 && flag0 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3)))//XPos
			{
				float f1 = (flag2 == 3) ? 16.0F : 17.0F;
				float f2 = (flag3 == 3) ? 0.0F : -1.0F;
				NGTRenderer.renderBlock(F0 * 15.0F, F0 * 15.0F, F0 * f2, F0 * 17.0F, F0 * 17.0F, F0 * f1, true, renderer, block, x, y, z);

				NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 1.5F, F0 * 16.5F, F0 * 16.0F, F0 * 2.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 5.5F, F0 * 16.5F, F0 * 16.0F, F0 * 6.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 9.5F, F0 * 16.5F, F0 * 16.0F, F0 * 10.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 15.5F, F0 * 0.0F, F0 * 13.5F, F0 * 16.5F, F0 * 16.0F, F0 * 14.5F, true, renderer, block, x, y, z);
			}

			if ((b0 && flag1 == 0) || (!b0 && flag1 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3)))//XNeg
			{
				float f1 = (flag2 == 3) ? 16.0F : 17.0F;
				float f2 = (flag3 == 3) ? 0.0F : -1.0F;
				NGTRenderer.renderBlock(F0 * -1.0F, F0 * 15.0F, F0 * f2, F0 * 1.0F, F0 * 17.0F, F0 * f1, true, renderer, block, x, y, z);

				NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 1.5F, F0 * 0.5F, F0 * 16.0F, F0 * 2.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 5.5F, F0 * 0.5F, F0 * 16.0F, F0 * 6.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 9.5F, F0 * 0.5F, F0 * 16.0F, F0 * 10.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * -0.5F, F0 * 0.0F, F0 * 13.5F, F0 * 0.5F, F0 * 16.0F, F0 * 14.5F, true, renderer, block, x, y, z);
			}

			if ((!b0 && flag2 == 0) || (b0 && flag2 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3)))//ZPos
			{
				float f1 = (flag0 == 3) ? 16.0F : 17.0F;
				float f2 = (flag1 == 3) ? 0.0F : -1.0F;
				NGTRenderer.renderBlock(F0 * f2, F0 * 15.0F, F0 * 15.0F, F0 * f1, F0 * 17.0F, F0 * 17.0F, true, renderer, block, x, y, z);

				NGTRenderer.renderBlock(F0 * 1.5F, F0 * 0.0F, F0 * 15.5F, F0 * 2.5F, F0 * 16.0F, F0 * 16.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 5.5F, F0 * 0.0F, F0 * 15.5F, F0 * 6.5F, F0 * 16.0F, F0 * 16.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 9.5F, F0 * 0.0F, F0 * 15.5F, F0 * 10.5F, F0 * 16.0F, F0 * 16.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 13.5F, F0 * 0.0F, F0 * 15.5F, F0 * 14.5F, F0 * 16.0F, F0 * 16.5F, true, renderer, block, x, y, z);
			}

			if ((!b0 && flag3 == 0) || (b0 && flag3 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3)))//ZNeg
			{
				float f1 = (flag0 == 3) ? 16.0F : 17.0F;
				float f2 = (flag1 == 3) ? 0.0F : -1.0F;
				NGTRenderer.renderBlock(F0 * f2, F0 * 15.0F, F0 * -1.0F, F0 * f1, F0 * 17.0F, F0 * 1.0F, true, renderer, block, x, y, z);

				NGTRenderer.renderBlock(F0 * 1.5F, F0 * 0.0F, F0 * -0.5F, F0 * 2.5F, F0 * 16.0F, F0 * 0.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 5.5F, F0 * 0.0F, F0 * -0.5F, F0 * 6.5F, F0 * 16.0F, F0 * 0.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 9.5F, F0 * 0.0F, F0 * -0.5F, F0 * 10.5F, F0 * 16.0F, F0 * 0.5F, true, renderer, block, x, y, z);
				NGTRenderer.renderBlock(F0 * 13.5F, F0 * 0.0F, F0 * -0.5F, F0 * 14.5F, F0 * 16.0F, F0 * 0.5F, true, renderer, block, x, y, z);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int par1) {
		return true;
	}

	@Override
	public int getRenderId() {
		return RTMBlock.renderIdScaffold;
	}
}