package jp.ngt.mcte.editor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class RenderEditor extends Render {
	private static final ResourceLocation texture = new ResourceLocation("mcte", "textures/atc.png");

	public void renderEditor(EntityEditor editor, double par2, double par4, double par6, float par8, float par9) {
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef((float) par2, (float) par4, (float) par6);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		int[] start = editor.getPos(true);
		int[] end = editor.getPos(false);
		int minX = 0;
		int minY = 0;
		int minZ = 0;
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;
		if (!editor.isSelectEnd()) {
			MovingObjectPosition target = editor.getTarget(false);
			if (target == null) {
				return;
			}

			end[0] = target.blockX;
			end[1] = target.blockY;
			end[2] = target.blockZ;
		}

		if (start[0] < end[0]) {
			minX = start[0];
			maxX = end[0];
		} else {
			minX = end[0];
			maxX = start[0];
		}

		if (start[1] < end[1]) {
			minY = start[1];
			maxY = end[1];
		} else {
			minY = end[1];
			maxY = start[1];
		}

		if (start[2] < end[2]) {
			minZ = start[2];
			maxZ = end[2];
		} else {
			minZ = end[2];
			maxZ = start[2];
		}

		double pX = editor.prevPosX + (editor.posX - editor.prevPosX) * par9;
		double pY = editor.prevPosY + (editor.posY - editor.prevPosY) * par9;
		double pZ = editor.prevPosZ + (editor.posZ - editor.prevPosZ) * par9;
		GL11.glTranslatef((float) ((double) minX - pX), (float) ((double) minY - pY), (float) ((double) minZ - pZ));

		double difX = maxX - minX;
		double difY = maxY - minY;
		double difZ = maxZ - minZ;

		this.renderBox(-0.05D, -0.05D, -0.05D, 1.1D + difX, 1.1D + difY, 1.1D + difZ, 0x204020, 32);
		//RenderGlobal.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(-0.05D, -0.05D, -0.05D, 1.1D + difX, 1.1D + difY, 1.1D + difZ), 0x000000);
		this.renderFrame(-0.05D, -0.05D, -0.05D, 1.1D + difX, 1.1D + difY, 1.1D + difZ, 0x000000, 255);

		//始点
		this.renderBox((double) (start[0] - minX) - 0.06D, (double) (start[1] - minY) - 0.06D, (double) (start[2] - minZ) - 0.06D, 1.12D, 1.12D, 1.12D, 0xff0000, 128);
		this.renderFrame((double) (start[0] - minX) - 0.06D, (double) (start[1] - minY) - 0.06D, (double) (start[2] - minZ) - 0.06D, 1.12D, 1.12D, 1.12D, 0xff0000, 255);

		//終点
		this.renderBox((double) (end[0] - minX) - 0.06D, (double) (end[1] - minY) - 0.06D, (double) (end[2] - minZ) - 0.06D, 1.12D, 1.12D, 1.12D, 0x0000ff, 128);
		this.renderFrame((double) (end[0] - minX) - 0.06D, (double) (end[1] - minY) - 0.06D, (double) (end[2] - minZ) - 0.06D, 1.12D, 1.12D, 1.12D, 0x0000ff, 255);

		byte editMode = editor.getEditMode();
		boolean flag1 = editor.isSelectEnd() && (editMode == Editor.EditMode_VisibleBox_0 || editMode == Editor.EditMode_VisibleBox_1);
		MovingObjectPosition target = null;
		if (flag1) {
			target = editor.getTarget(true);

			//ブロック描画
			if (editor.blocksForRenderer != null && target != null) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glPushMatrix();
				GL11.glTranslatef((float) (target.blockX - minX), (float) (target.blockY - minY), (float) (target.blockZ - minZ));
				this.renderBlocks(editor, target.blockX, target.blockY, target.blockZ);
				GL11.glPopMatrix();
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			}

			//コピーボックス描画
			int[] box = editor.getPasteBox();
			if (target != null && box[0] * box[1] * box[2] > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float) (target.blockX - minX), (float) (target.blockY - minY), (float) (target.blockZ - minZ));
				this.renderBox(-0.05D, -0.05D, -0.05D, 0.1D + (double) box[0], 0.1D + (double) box[1], 0.1D + (double) box[2], 0x808020, 64);
				this.renderFrame(-0.05D, -0.05D, -0.05D, 0.1D + (double) box[0], 0.1D + (double) box[1], 0.1D + (double) box[2], 0x000000, 255);
				GL11.glPopMatrix();
			}
		}

		if (editor.hasCloneBox()) {
			GL11.glPushMatrix();
			int[] box = editor.getCloneBox();
			IntStream.range(0, box[3]).forEach(i -> {
				GL11.glTranslatef((float) box[0], (float) box[1], (float) box[2]);
				this.renderBox(-0.05D, -0.05D, -0.05D, 1.1D + difX, 1.1D + difY, 1.1D + difZ, 0x303000, 64);
				this.renderFrame(-0.05D, -0.05D, -0.05D, 1.1D + difX, 1.1D + difY, 1.1D + difZ, 0x000000, 255);
			});
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}

	private void renderBlocks(EntityEditor editor, int x, int y, int z) {
		GL11.glEnable(GL11.GL_CULL_FACE);
		this.bindTexture(TextureMap.locationBlocksTexture);

		if (editor.shouldUpdate() || !GLHelper.isValid(editor.displayList)) {
			if (!GLHelper.isValid(editor.displayList)) {
				editor.displayList = GLHelper.generateGLList();
			}

			GLHelper.startCompile(editor.displayList);
			NGTRenderer.renderNGTObject((NGTWorld) editor.dummyWorld, editor.blocksForRenderer, true, 0, 0);
			GLHelper.endCompile();

			editor.setUpdate(false);
		} else {
			GLHelper.callList(editor.displayList);
		}

		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	@Override
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
		this.renderEditor((EntityEditor) par1Entity, par2, par4, par6, par8, par9);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity par1) {
		return texture;
	}

	private void renderBox(double minX, double minY, double minZ, double width, double height, double depth, int color, int alpha) {
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);//加算
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);//アルファ
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLHelper.setLightmapMaxBrightness();

		double maxX = minX + width;
		double maxY = minY + height;
		double maxZ = minZ + depth;

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_I(color, alpha);

		tessellator.addVertex(maxX, minY, minZ);//maxX
		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(maxX, maxY, maxZ);
		tessellator.addVertex(maxX, minY, maxZ);

		tessellator.addVertex(minX, minY, maxZ);//minX
		tessellator.addVertex(minX, maxY, maxZ);
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(minX, minY, minZ);

		tessellator.addVertex(maxX, maxY, maxZ);//maxY
		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(minX, maxY, maxZ);

		tessellator.addVertex(maxX, minY, minZ);//minY
		tessellator.addVertex(maxX, minY, maxZ);
		tessellator.addVertex(minX, minY, maxZ);
		tessellator.addVertex(minX, minY, minZ);

		tessellator.addVertex(maxX, minY, maxZ);//maxZ
		tessellator.addVertex(maxX, maxY, maxZ);
		tessellator.addVertex(minX, maxY, maxZ);
		tessellator.addVertex(minX, minY, maxZ);

		tessellator.addVertex(minX, minY, minZ);//minZ
		tessellator.addVertex(minX, maxY, minZ);
		tessellator.addVertex(maxX, maxY, minZ);
		tessellator.addVertex(maxX, minY, minZ);

		tessellator.draw();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(true);
	}

	private void renderFrame(double minX, double minY, double minZ, double width, double height, double depth, int color, int alpha) {
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
}