package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.ColorUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderPaint extends TileEntitySpecialRenderer {
	private static final float PIXEL_SIZE = 1.0F / 16.0F;
	private static final float GAP = 1.0F / 64.0F;

	private void renderPaintAt(TileEntityPaint tileEntity, double par2, double par4, double par6, float par8) {
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glTranslatef((float) par2, (float) par4, (float) par6);

		GLHelper.disableLighting();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthMask(false);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		double d2 = GAP;
		double d3 = 1.0D - d2;

		for (int i = 0; i < 6; ++i) {
			if (!tileEntity.hasColor(i)) {
				continue;
			}

			for (int j = 0; j < 16; ++j) {
				double d0 = (double) j * PIXEL_SIZE;

				for (int k = 0; k < 16; ++k) {
					int alpha = tileEntity.getAlpha(j, k, i);
					if (alpha == 0) {
						continue;
					}
					int color = tileEntity.getColor(j, k, i);
					double d1 = (double) k * PIXEL_SIZE;

					switch (i) {
						case 0:
							tessellator.setColorRGBA_I(ColorUtil.multiplicating(0x808080, color), alpha);
							tessellator.addVertex(d0, d3, d1 + PIXEL_SIZE);
							tessellator.addVertex(d0, d3, d1);
							tessellator.addVertex(d0 + PIXEL_SIZE, d3, d1);
							tessellator.addVertex(d0 + PIXEL_SIZE, d3, d1 + PIXEL_SIZE);
							break;
						case 1:
							tessellator.setColorRGBA_I(color, alpha);
							tessellator.addVertex(d0 + PIXEL_SIZE, d2, d1 + PIXEL_SIZE);
							tessellator.addVertex(d0 + PIXEL_SIZE, d2, d1);
							tessellator.addVertex(d0, d2, d1);
							tessellator.addVertex(d0, d2, d1 + PIXEL_SIZE);
							break;
						case 2:
							tessellator.setColorRGBA_I(ColorUtil.multiplicating(0xCCCCCC, color), alpha);
							tessellator.addVertex(d0 + PIXEL_SIZE, d1 + PIXEL_SIZE, d3);
							tessellator.addVertex(d0 + PIXEL_SIZE, d1, d3);
							tessellator.addVertex(d0, d1, d3);
							tessellator.addVertex(d0, d1 + PIXEL_SIZE, d3);
							break;
						case 3:
							tessellator.setColorRGBA_I(ColorUtil.multiplicating(0xCCCCCC, color), alpha);
							tessellator.addVertex(d0, d1 + PIXEL_SIZE, d2);
							tessellator.addVertex(d0, d1, d2);
							tessellator.addVertex(d0 + PIXEL_SIZE, d1, d2);
							tessellator.addVertex(d0 + PIXEL_SIZE, d1 + PIXEL_SIZE, d2);
							break;
						case 4:
							tessellator.setColorRGBA_I(ColorUtil.multiplicating(0x999999, color), alpha);
							tessellator.addVertex(d3, d0 + PIXEL_SIZE, d1 + PIXEL_SIZE);
							tessellator.addVertex(d3, d0 + PIXEL_SIZE, d1);
							tessellator.addVertex(d3, d0, d1);
							tessellator.addVertex(d3, d0, d1 + PIXEL_SIZE);
							break;
						case 5:
							tessellator.setColorRGBA_I(ColorUtil.multiplicating(0x999999, color), alpha);
							tessellator.addVertex(d2, d0, d1 + PIXEL_SIZE);
							tessellator.addVertex(d2, d0, d1);
							tessellator.addVertex(d2, d0 + PIXEL_SIZE, d1);
							tessellator.addVertex(d2, d0 + PIXEL_SIZE, d1 + PIXEL_SIZE);
							break;
					}
				}
			}
		}
		tessellator.draw();

		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GLHelper.enableLighting();

		ItemStack stack = NGTUtilClient.getMinecraft().thePlayer.inventory.getCurrentItem();
		if (stack != null && stack.getItem() == RTMItem.crowbar) {
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			RTMCore.proxy.renderMissingModel();
		}

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
		this.renderPaintAt((TileEntityPaint) par1, par2, par4, par6, par8);
	}
}