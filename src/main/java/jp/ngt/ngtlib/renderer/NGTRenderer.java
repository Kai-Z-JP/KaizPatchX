package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.world.IBlockAccessNGT;
import jp.ngt.ngtlib.world.NGTBlockAccess;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class NGTRenderer {
	public static void renderPole(Tessellator tessellator, double r, double length, boolean useTexture) {
		double minU;
		double maxU;
		double minV;
		double maxV;
		float[][] sp = ModelSolid.sphere;
		int l0 = (int) (length * 16.0D);

		for (int l = 0; l < l0; ++l) {
			for (int i1 = 0; i1 < 16; ++i1) {
				minU = (double) i1 * 0.0625D;
				maxU = (double) (i1 + 1) * 0.0625D;
				minV = (double) l * 0.0625D;
				maxV = (double) (l + 1) * 0.0625D;
				double l1 = l * 0.0625D;

				int ii0 = 64 + i1;
				int ii1 = 64 + i1;
				int ii2 = 64 + (i1 + 1) % 16;
				int ii3 = 64 + (i1 + 1) % 16;

				addVertex(tessellator, useTexture, sp[ii0][0] * r, sp[ii0][1] * r + l1, sp[ii0][2] * r, maxU, maxV);
				addVertex(tessellator, useTexture, sp[ii1][0] * r, sp[ii1][1] * r + 0.0625D + l1, sp[ii1][2] * r, maxU, minV);
				addVertex(tessellator, useTexture, sp[ii2][0] * r, sp[ii2][1] * r + 0.0625D + l1, sp[ii2][2] * r, minU, minV);
				addVertex(tessellator, useTexture, sp[ii3][0] * r, sp[ii3][1] * r + l1, sp[ii3][2] * r, minU, maxV);

				addVertex(tessellator, useTexture, sp[ii3][0] * r, sp[ii3][1] * r + l1, sp[ii3][2] * r, maxU, maxV);
				addVertex(tessellator, useTexture, sp[ii2][0] * r, sp[ii2][1] * r + 0.0625D + l1, sp[ii2][2] * r, maxU, minV);
				addVertex(tessellator, useTexture, sp[ii1][0] * r, sp[ii1][1] * r + 0.0625D + l1, sp[ii1][2] * r, minU, minV);
				addVertex(tessellator, useTexture, sp[ii0][0] * r, sp[ii0][1] * r + l1, sp[ii0][2] * r, minU, maxV);
			}
		}
	}

	private static void addVertex(Tessellator tessellator, boolean b, double x, double y, double z, double u, double v) {
		if (b) {
			tessellator.addVertexWithUV(x, y, z, u, v);
		} else {
			tessellator.addVertex(x, y, z);
		}
	}

	public static void renderSphere(Tessellator tessellator, double r) {
		float[][] sp = ModelSolid.sphere;
		double minU = 0.0D;
		double maxU = 1.0D;
		double minV = 0.0D;
		double maxV = 1.0D;

		for (int i0 = 0; i0 < 8; ++i0)//roZ
		{
			for (int i1 = 0; i1 < 16; ++i1)//roY
			{
				minU = (double) i1 * 0.0625D;
				maxU = (double) (i1 + 1) * 0.0625D;
				minV = (double) i0 * 0.125D;
				maxV = (double) (i0 + 1) * 0.125D;

				int ii0 = i0 * 16 + i1;
				int ii1 = (i0 + 1) * 16 + i1;
				int ii2 = (i0 + 1) * 16 + (i1 + 1) % 16;
				int ii3 = i0 * 16 + (i1 + 1) % 16;

				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV(sp[ii0][0] * r, sp[ii0][1] * r, sp[ii0][2] * r, maxU, maxV);
				tessellator.addVertexWithUV(sp[ii1][0] * r, sp[ii1][1] * r, sp[ii1][2] * r, maxU, minV);
				tessellator.addVertexWithUV(sp[ii2][0] * r, sp[ii2][1] * r, sp[ii2][2] * r, minU, minV);
				tessellator.addVertexWithUV(sp[ii3][0] * r, sp[ii3][1] * r, sp[ii3][2] * r, minU, maxV);

				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV(sp[ii3][0] * r, sp[ii3][1] * r, sp[ii3][2] * r, maxU, maxV);
				tessellator.addVertexWithUV(sp[ii2][0] * r, sp[ii2][1] * r, sp[ii2][2] * r, maxU, minV);
				tessellator.addVertexWithUV(sp[ii1][0] * r, sp[ii1][1] * r, sp[ii1][2] * r, minU, minV);
				tessellator.addVertexWithUV(sp[ii0][0] * r, sp[ii0][1] * r, sp[ii0][2] * r, minU, maxV);
			}
		}
	}

	public static void renderBlock(double par1, double par2, double par3, double par4, double par5, double par6, RenderBlocks renderer, Block block) {
		renderBlock(par1, par2, par3, par4, par5, par6, false, renderer, block, 0, 0, 0);
	}

	public static void renderBlock(double par1, double par2, double par3, double par4, double par5, double par6, RenderBlocks renderer, Block block, int meta) {
		renderBlock(par1, par2, par3, par4, par5, par6, false, renderer, block, 0, 0, 0, meta);
	}

	public static void renderBlock(double par1, double par2, double par3, double par4, double par5, double par6, boolean inWorld, RenderBlocks renderer, Block block, int x, int y, int z) {
		renderBlock(par1, par2, par3, par4, par5, par6, inWorld, renderer, block, x, y, z, 0);
	}

	public static void renderBlock(double par1, double par2, double par3, double par4, double par5, double par6, boolean inWorld, RenderBlocks renderer, Block block, int x, int y, int z, int meta) {
		renderer.setRenderBounds(par1, par2, par3, par4, par5, par6);
		if (inWorld) {
			renderer.renderStandardBlock(block, x, y, z);
		} else {
			Tessellator tessellator = Tessellator.instance;
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1.0F, 0.0F);
			renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));//renderer.getBlockIconFromSide(block, 0)
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1.0F);
			renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1.0F, 0.0F, 0.0F);
			renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
	}

	public static void renderNGTObject(NGTObject par1, boolean changeLightting) {
		World world = NGTUtil.getClientWorld();
		NGTBlockAccess bAccess = new NGTBlockAccess(world, par1);
		renderNGTObject(bAccess, par1, changeLightting, 0, 0);
	}

	/**
	 * @param par1
	 * @param par2
	 * @param changeLightting
	 * @param mode            0:ミニチュア, 1:彫刻
	 *                        <br>
	 *                        <br>
	 *                        ブロックの一括描画<br>
	 *                        ※テクスチャバインドは行わない
	 */
	public static void renderNGTObject(IBlockAccessNGT par1, NGTObject par2, boolean changeLightting, int mode, int pass) {
		GL11.glPushMatrix();
		if (changeLightting) {
			GLHelper.disableLighting();
			;//これないと若干暗くなる
		}
		GL11.glEnable(GL11.GL_ALPHA_TEST);//ガラスや鉄柵が不透明になる

		boolean isSculpture = (mode == 1 && par2.xSize == par2.ySize && par2.xSize == par2.zSize);

		if (isSculpture) {
			float f0 = (float) par2.xSize;
			GL11.glScalef(f0, f0, f0);
		}

		if (pass == 1) {
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		}

		RenderBlocks renderer = new RenderBlocks(par1);
		Tessellator.instance.startDrawingQuads();
		for (int i = 0; i < par2.xSize; ++i) {
			for (int j = 0; j < par2.ySize; ++j) {
				for (int k = 0; k < par2.zSize; ++k) {
					BlockSet set = par1.getBlockSet(i, j, k);
					if (set.block.getMaterial() != Material.air && set.block.getRenderBlockPass() == pass) {
						if (isSculpture) {
							renderBlockAsSculpture(renderer, set, par2, i, j, k);
						} else {
							renderBlockByRenderer(renderer, set.block, i, j, k);
						}
					}
				}
			}
		}
		Tessellator.instance.draw();

		if (pass == 1) {
			GL11.glDisable(GL11.GL_BLEND);
		}

		if (changeLightting) {
			GLHelper.enableLighting();
		}
		GL11.glPopMatrix();
	}

	private static void renderBlockByRenderer(RenderBlocks renderer, Block block, int x, int y, int z) {
		try {
			renderer.renderBlockByRenderType(block, x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void renderBlockAsSculpture(RenderBlocks renderer, BlockSet set, NGTObject ngto, int x, int y, int z) {
		GL11.glPushMatrix();

		Block block = set.block;
		int meta = set.metadata;
		float f0 = (float) ngto.xSize;
		float scale = 1.0F / f0;
		float minX = (float) x / f0;
		float minY = (float) y / f0;
		float minZ = (float) z / f0;
		float maxX = minX + scale;
		float maxY = minY + scale;
		float maxZ = minZ + scale;
		renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
		Tessellator tessellator = Tessellator.instance;
		if (block.shouldSideBeRendered(renderer.blockAccess, x, y - 1, z, 0)) {
			tessellator.setColorOpaque_F(0.5F, 0.5F, 0.5F);
			renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));
		}

		if (block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1)) {
			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
			renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
		}

		if (block.shouldSideBeRendered(renderer.blockAccess, x, y, z - 1, 2)) {
			tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
			renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
		}

		if (block.shouldSideBeRendered(renderer.blockAccess, x, y, z + 1, 3)) {
			tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
			renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
		}

		if (block.shouldSideBeRendered(renderer.blockAccess, x - 1, y, z, 4)) {
			tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
			renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
		}

		if (block.shouldSideBeRendered(renderer.blockAccess, x + 1, y, z, 5)) {
			tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
			renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
		}

		GL11.glPopMatrix();
	}

	public static final void renderFrame(double minX, double minY, double minZ, double width, double height, double depth, int color, int alpha) {
		double maxX = minX + width;
		double maxY = minY + height;
		double maxZ = minZ + depth;

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(GL11.GL_LINES);
		tessellator.setColorRGBA_I(color, alpha);

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

		tessellator.draw();
	}

	public static void renderTileEntities(NGTWorld par1, float par8, int pass) {
		if (!par1.tileEntityLoaded) {
			loadTileEntity(par1);
			par1.tileEntityLoaded = true;
		}

		for (TileEntity tile : par1.getTileEntityList()) {
			renderTileEntityByRenderer(tile, tile.xCoord, tile.yCoord, tile.zCoord, par8, pass);
		}
	}

	private static void loadTileEntity(NGTWorld par1) {
		List<TileEntity> list = new ArrayList<TileEntity>();

		for (int i = 0; i < par1.blockObject.xSize; ++i) {
			for (int j = 0; j < par1.blockObject.ySize; ++j) {
				for (int k = 0; k < par1.blockObject.zSize; ++k) {
					TileEntity tile = par1.getTileEntity(i, j, k);
					if (tile != null && TileEntityRendererDispatcher.instance.hasSpecialRenderer(tile)) {
						list.add(tile);
					}
				}
			}
		}

		par1.setTileEntityList(list);
	}

	public static void renderTileEntityByRenderer(TileEntity tile, double par2, double par4, double par6, float par8, int pass) {
		TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);

		if (tile.shouldRenderInPass(pass) && renderer != null) {
			try {
				renderer.renderTileEntityAt(tile, par2, par4, par6, par8);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Throwable throwable) {
				CrashReport report = CrashReport.makeCrashReport(throwable, "Rendering TileEntity in Miniature");
				CrashReportCategory category = report.makeCategory("TileEntity Details");
				tile.func_145828_a(category);
				throw new ReportedException(report);
			}
		}
	}

	public static void renderEntities(NGTWorld par1, float par8, int pass) {
		List<Entity> list = par1.getEntityList();
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = list.get(i);
			if (!entity.isDead) {
				renderEntityByRenderer(entity, par8, pass);
			}
		}
	}

	public static void renderEntityByRenderer(Entity entity, float par8, int pass) {
		if (!entity.shouldRenderInPass(pass)) {
			return;
		}

		//RenderManager.instance.renderEntitySimple(entity, par8);
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) par8;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) par8;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) par8;
		float f1 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par8;

		int i = entity.isBurning() ? 15728880 : entity.getBrightnessForRender(par8);
		GLHelper.setBrightness(i);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		try {
			RenderManager.instance.func_147939_a(entity, d0, d1, d2, f1, par8, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}