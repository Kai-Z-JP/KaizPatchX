package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderMarkerBlock extends TileEntitySpecialRenderer {
	private String[] displayStrings;

	public RenderMarkerBlock() {
		super();

		int i0 = (RTMCore.markerDisplayDistance / 10);
		this.displayStrings = new String[i0];
		for (int i = 0; i < this.displayStrings.length; ++i) {
			this.displayStrings[i] = String.valueOf((i + 1) * 10) + "m";
		}
	}

	public void renderTileEntityMarker(TileEntityMarker tileEntity, double par2, double par4, double par6, float par8) {
		if (!tileEntity.displayDistance) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GLHelper.disableLighting();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glTranslatef((float) par2, (float) par4, (float) par6);

		if (tileEntity.getDisplayMode() == 1 && tileEntity.getGrid() != null) {
			this.renderGrid(tileEntity);
		}

		if (tileEntity.getDisplayMode() == 2) {
			RailPosition rp0 = tileEntity.getMarkerRP();
			double x = rp0.posX - (double) rp0.blockX;
			double y = rp0.posY - (double) rp0.blockY;
			double z = rp0.posZ - (double) rp0.blockZ;

			if (tileEntity.getRailMaps() != null) {
				this.renderLine(tileEntity, (float) x, (float) y, (float) z);
			}

			if (tileEntity.startY > 0) {
				this.renderAnchor(tileEntity, (float) x, (float) y, (float) z);
			}
		}

		if (tileEntity.displayDistance) {
			this.renderDistanceMark(tileEntity);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLHelper.enableLighting();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	/**
	 * グリッドの描画
	 */
	private void renderGrid(TileEntityMarker tileEntity) {
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.setColorOpaque_I(0);

		for (int i = 0; i < tileEntity.getGrid().size(); ++i) {
			int[] ia = tileEntity.getGrid().get(i);
			this.renderFrame(tessellator, ia[0] - tileEntity.xCoord, ia[1] - tileEntity.yCoord, ia[2] - tileEntity.zCoord, 1.0D, 1.0D, 1.0D);
		}

		tessellator.draw();
		GL11.glPopMatrix();
	}

	/**
	 * 距離表示描画
	 */
	private void renderDistanceMark(TileEntityMarker tileEntity) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 0.0625F, 0.5F);
		int meta = tileEntity.getBlockMetadata();
		int color = tileEntity.getBlockType().getRenderColor(meta);
		byte dir = BlockMarker.getMarkerDir(tileEntity.getBlockType(), meta);
		GL11.glRotatef((float) dir * 45.0F, 0.0F, 1.0F, 0.0F);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(color);
		float f0 = 0.4F;

		if (tileEntity.getBlockType() == RTMBlock.markerSlope) {
			int i = 0;
			switch (tileEntity.getBlockMetadata() / 4) {
				case 0:
					i = 16;
					break;
				case 1:
					i = 8;
					break;
				case 2:
					i = 4;
					break;
				case 3:
					i = 2;
					break;
			}
			float f1 = (float) i;
			tessellator.addVertex(-f0, 0.0F, f0 + f1);
			tessellator.addVertex(-f0, 0.0F, -f0 + f1);
			tessellator.addVertex(f0, 0.0F, -f0 + f1);
			tessellator.addVertex(f0, 0.0F, f0 + f1);
		} else {
			for (int i = 1; i < this.displayStrings.length; ++i) {
				float f1 = (float) i * 10.0F;
				tessellator.addVertex(-f0, 0.0F, f0 + f1);
				tessellator.addVertex(-f0, 0.0F, -f0 + f1);
				tessellator.addVertex(f0, 0.0F, -f0 + f1);
				tessellator.addVertex(f0, 0.0F, f0 + f1);
			}
		}

		tessellator.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		FontRenderer fontrenderer = RenderManager.instance.getFontRenderer();
		if (tileEntity.getBlockType() == RTMBlock.markerSlope) {
			int i = 0;
			switch (tileEntity.getBlockMetadata() / 4) {
				case 0:
					i = 16;
					break;
				case 1:
					i = 8;
					break;
				case 2:
					i = 4;
					break;
				case 3:
					i = 2;
					break;
			}
			GL11.glTranslatef(0.0F, 0.0F, (float) i);
			GL11.glScalef(-0.25F, -0.25F, 0.25F);
			String s = String.valueOf(i);
			int i0 = (fontrenderer.getStringWidth(s) / 2);
			fontrenderer.drawString(s, -i0 / 2, -10, color);
		} else {
			for (int i = 0; i < this.displayStrings.length; ++i) {
				GL11.glTranslatef(0.0F, 0.0F, 10.0F);

				GL11.glPushMatrix();
				GL11.glScalef(-0.25F, -0.25F, 0.25F);
				String s = this.displayStrings[i];
				int i0 = (fontrenderer.getStringWidth(s) / 2);
				fontrenderer.drawString(s, -i0 / 2, -10, color);
				GL11.glPopMatrix();
			}
		}

		GL11.glPopMatrix();
	}

	private void renderLine(TileEntityMarker tileEntity, float x, float y, float z) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		Tessellator tessellator = Tessellator.instance;
		for (RailMap rm : tileEntity.getRailMaps()) {
			GL11.glPushMatrix();
			float x0 = (float) (rm.getStartRP().posX - tileEntity.getMarkerRP().posX);
			float y0 = (float) (rm.getStartRP().posY - tileEntity.getMarkerRP().posY);
			float z0 = (float) (rm.getStartRP().posZ - tileEntity.getMarkerRP().posZ);
			GL11.glTranslatef(x0, y0, z0);
			tessellator.startDrawing(GL11.GL_LINE_STRIP);
			tessellator.setColorOpaque_I(0x004000);
			int max = (int) ((float) rm.getLength() * 2.0F);
			double[] p2 = rm.getRailPos(max, 0);
			double h2 = rm.getRailHeight(max, 0);
			float[][] rp = new float[max + 1][5];
			for (int i = 0; i < max + 1; ++i) {
				double[] p1 = rm.getRailPos(max, i);
				tessellator.addVertex(p1[1] - p2[1], rm.getRailHeight(max, i) - h2, p1[0] - p2[0]);
			}
			tessellator.draw();
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
	}

	private void renderAnchor(TileEntityMarker tileEntity, float x, float y, float z) {
		this.changeAnchor(tileEntity);

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINE_STRIP);
		tessellator.setColorOpaque_I(0x00FF00);
		RailPosition rp = tileEntity.getMarkerRP();
		double dx = MathHelper.sin(NGTMath.toRadians(rp.anchorDirection)) * rp.anchorLength;
		double dz = MathHelper.cos(NGTMath.toRadians(rp.anchorDirection)) * rp.anchorLength;
		tessellator.addVertex(0.0F, 0.0F, 0.0F);
		tessellator.addVertex(dx, 0.0F, dz);
		tessellator.draw();

		GL11.glPopMatrix();
	}

	private void changeAnchor(TileEntityMarker tileEntity)//Minecraft.1518-this.objectMouseOver.hitVec
	{
		if (tileEntity.getCoreMarker() == null) {
			return;
		}

		Minecraft mc = NGTUtilClient.getMinecraft();
		if (!tileEntity.followMouseMoving || !mc.thePlayer.equals(tileEntity.followingPlayer)) {
			return;
		}

		MovingObjectPosition target = BlockUtil.getMOPFromPlayer(mc.thePlayer, 128.0D, true);
		if (target == null || target.typeOfHit != MovingObjectType.BLOCK) {
			return;
		}

		Vec3 vec3 = target.hitVec;
		double dx = vec3.xCoord - tileEntity.getMarkerRP().posX;
		double dz = vec3.zCoord - tileEntity.getMarkerRP().posZ;
		if (dx != 0.0D && dz != 0.0D) {
			float dirRad = (float) Math.atan2(dx, dz);
			float length = (float) (dx / MathHelper.sin(dirRad));
			tileEntity.getMarkerRP().anchorDirection = NGTMath.toDegrees(dirRad);
			tileEntity.getMarkerRP().anchorLength = length;
			tileEntity.getCoreMarker().updateRailMap();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8) {
		this.renderTileEntityMarker((TileEntityMarker) par1TileEntity, par2, par4, par6, par8);
	}

	private final void renderFrame(Tessellator tessellator, double minX, double minY, double minZ, double width, double height, double depth) {
		double maxX = minX + width;
		double maxY = minY + height;
		double maxZ = minZ + depth;

		tessellator.addVertex(minX, minY, minZ);//minY
		tessellator.addVertex(maxX, minY, minZ);

		tessellator.addVertex(minX, minY, maxZ);
		tessellator.addVertex(maxX, minY, maxZ);

		tessellator.addVertex(minX, minY, minZ);
		tessellator.addVertex(minX, minY, maxZ);

		tessellator.addVertex(maxX, minY, minZ);
		tessellator.addVertex(maxX, minY, maxZ);

		tessellator.addVertex(minX, minY, minZ);//tate
		tessellator.addVertex(minX, maxY, minZ);

		tessellator.addVertex(maxX, minY, minZ);
		tessellator.addVertex(maxX, maxY, minZ);

		tessellator.addVertex(minX, minY, maxZ);
		tessellator.addVertex(minX, maxY, maxZ);

		tessellator.addVertex(maxX, minY, maxZ);
		tessellator.addVertex(maxX, maxY, maxZ);

		tessellator.addVertex(minX, maxY, minZ);//maxY
		tessellator.addVertex(maxX, maxY, minZ);

		tessellator.addVertex(minX, maxY, maxZ);
		tessellator.addVertex(maxX, maxY, maxZ);

		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(minX, maxY, maxZ);

		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(maxX, maxY, maxZ);
	}
}