package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.modelpack.texture.FlagProperty;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderFlag extends TileEntitySpecialRenderer {
	private void renderFlag(TileEntityFlag tileEntity, double par2, double par4, double par6, float par8) {
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 1.0F, (float) par6 + 0.5F);
		float yaw = tileEntity.getRotation();
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);

		FlagProperty property = tileEntity.getProperty();
		float wind = 1.0F;//MathHelper.sin(NGTMath.toRadians((float)tileEntity.tick * 0.64F)) * 0.5F + 0.5F;
		float windInv = 1.0F - wind;
		float h = property.height;
		float w = property.width;

		GL11.glShadeModel(GL11.GL_SMOOTH);
		this.bindTexture(property.getTexture());
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		for (int i = 0; i < property.resolutionV; ++i) {
			float v0 = (float) i / (float) property.resolutionV;
			float v1 = (float) (i + 1) / (float) property.resolutionV;

			for (int j = 0; j < property.resolutionU; ++j) {
				float u0 = (float) j / (float) property.resolutionU;
				float u1 = (float) (j + 1) / (float) property.resolutionU;
				float u0w = u0 * w;
				float u1w = u1 * w;

				float r0 = this.getR(tileEntity.wave, u1, v0);
				float d0 = this.getWave(r0, u1);
				float nr0 = this.getNormalR(r0 + yaw);
				tessellator.setNormal(MathHelper.sin(nr0), 0.0F, MathHelper.cos(nr0));
				tessellator.addVertexWithUV(d0, -(v0 + windInv * u1w) * h, u1w * wind, u1, v0);

				float r1 = this.getR(tileEntity.wave, u1, v1);
				float d1 = this.getWave(r1, u1);
				float nr1 = this.getNormalR(r1 + yaw);
				tessellator.setNormal(MathHelper.sin(nr1), 0.0F, MathHelper.cos(nr1));
				tessellator.addVertexWithUV(d1, -(v1 + windInv * u1w) * h, u1w * wind, u1, v1);

				float r2 = this.getR(tileEntity.wave, u0, v1);
				float d2 = this.getWave(r2, u0);
				float nr2 = this.getNormalR(r2 + yaw);
				tessellator.setNormal(MathHelper.sin(nr2), 0.0F, MathHelper.cos(nr2));
				tessellator.addVertexWithUV(d2, -(v1 + windInv * u0w) * h, u0w * wind, u0, v1);

				float r3 = this.getR(tileEntity.wave, u0, v0);
				float d3 = this.getWave(r3, u0);
				float nr3 = this.getNormalR(r3 + yaw);
				tessellator.setNormal(MathHelper.sin(nr3), 0.0F, MathHelper.cos(nr3));
				tessellator.addVertexWithUV(d3, -(v0 + windInv * u0w) * h, u0w * wind, u0, v0);
			}
		}
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}

	//正弦波のX
	private float getR(float r, float u, float v) {
		//return NGTMath.toRadians(r + (540.0F * u * u) + (90.0F * v));
		//return NGTMath.toRadians(r + 30.0F * (60.0F * u / (u + 1.0F)) + 90.0F * v);
		return -NGTMath.toRadians(r + (360.0F / (3.0F * u + 1.0F)) * (v + 1));
	}

	//正弦波のY
	private float getWave(float r, float u) {
		return MathHelper.sin(r) * u * 0.15F;
	}

	//法線の角度
	private float getNormalR(float r) {
		return NGTMath.toRadians(45.0F * MathHelper.cos(r) + 90.0F);
	}

	@Override
	public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
		this.renderFlag((TileEntityFlag) par1, par2, par4, par6, par8);
	}
}