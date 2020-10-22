package jp.ngt.rtm.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityScaffoldStairs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderBlockScaffoldStairs extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {
	public static RenderBlockScaffoldStairs INSTANCE = new RenderBlockScaffoldStairs();

	private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/blocks/framework.png");
	private DisplayList[] displayLists = new DisplayList[3];

	private RenderBlockScaffoldStairs() {
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		if (modelId == this.getRenderId()) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			GL11.glTranslatef(0.0F, -0.5F, 0.0F);
			NGTUtilClient.bindTexture(texture);
			this.renderModel(0);
			this.renderModel(1);
			this.renderModel(2);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPopMatrix();
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int par1) {
		return true;
	}

	@Override
	public int getRenderId() {
		return RTMBlock.renderIdScaffoldStairs;
	}

	public void renderScaffoldStairsAt(TileEntityScaffoldStairs tile, double par2, double par4, double par6, float par8) {
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glTranslatef((float) par2 + 0.5F, (float) par4, (float) par6 + 0.5F);
		byte dir = tile.getDir();
		float yaw = 180.0F - ((float) dir * 90.0F);
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);

		int j = RTMBlock.scaffoldStairs.getRenderColor(tile.getBlockMetadata());
		float f1 = (float) (j >> 16 & 255) * 0.00392157F;//=/255F
		float f2 = (float) (j >> 8 & 255) * 0.00392157F;
		float f3 = (float) (j & 255) * 0.00392157F;
		GL11.glColor4f(f1, f2, f3, 1.0F);

		this.bindTexture(texture);

		byte flag0 = BlockScaffoldStairs.getConnectionType(tile.getWorldObj(), tile.xCoord + 1, tile.yCoord, tile.zCoord, tile.getDir());
		byte flag1 = BlockScaffoldStairs.getConnectionType(tile.getWorldObj(), tile.xCoord - 1, tile.yCoord, tile.zCoord, tile.getDir());
		byte flag2 = BlockScaffoldStairs.getConnectionType(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord + 1, tile.getDir());
		byte flag3 = BlockScaffoldStairs.getConnectionType(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord - 1, tile.getDir());

		if ((dir == 0 && flag1 < 3) || (dir == 1 && flag3 < 3) || (dir == 2 && flag0 < 3) || (dir == 3 && flag2 < 3)) {
			this.renderModel(2);
		}

		if (!(dir == 0 && flag0 >= 3) && !(dir == 1 && flag2 >= 3) && !(dir == 2 && flag1 >= 3) && !(dir == 3 && flag3 >= 3)) {
			this.renderModel(1);
		}

		this.renderModel(0);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float par8) {
		this.renderScaffoldStairsAt((TileEntityScaffoldStairs) tile, par2, par4, par6, par8);
	}

	private final void renderModel(int par1) {
		if (!GLHelper.isValid(this.displayLists[par1])) {
			this.displayLists[par1] = GLHelper.generateGLList();
			GLHelper.startCompile(this.displayLists[par1]);
			IModelNGT model = ModelLoader.loadModel(new ResourceLocation("rtm", "models/Model_ScaffoldStairs.mqo"), VecAccuracy.LOW, GL11.GL_QUADS);
			NGTRenderHelper.renderCustomModelEveryParts(model, (byte) 0, false, false, GL11.GL_QUADS, "part" + par1);
			GLHelper.endCompile();
		} else {
			GLHelper.callList(this.displayLists[par1]);
		}
	}
}