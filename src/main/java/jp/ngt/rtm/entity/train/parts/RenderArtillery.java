package jp.ngt.rtm.entity.train.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.ClientProxy;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.cfg.ModelConfig.Parts;
import jp.ngt.rtm.modelpack.modelset.ModelSetFirearmClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderArtillery extends Render {
	public void renderArtillery(EntityArtillery par1, double par2, double par4, double par6, float par8, float par9) {
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef((float) par2, (float) par4, (float) par6);

		ModelSetFirearmClient set = (ModelSetFirearmClient) par1.getModelSet();
		if (set == null || set.isDummy()) {
			RTMCore.proxy.renderMissingModel();
		} else if (set.getConfig().fpvMode && !this.shouldRender(par1)) {
			;
		} else {
			GL11.glRotatef(par1.rotationYaw, 0.0F, 1.0F, 0.0F);
			//GL11.glRotatef(-par1.rotationPitch, 1.0F, 0.0F, 0.0F);
			this.bindTexture(set.texture);

			GL11.glTranslatef(set.getConfig().modelPartsN.pos[0], set.getConfig().modelPartsN.pos[1], set.getConfig().modelPartsN.pos[2]);
			this.renderParts(set.model, set.getConfig().modelPartsN);

			float[] posY = set.getConfig().modelPartsY.pos;
			GL11.glTranslatef(posY[0], posY[1], posY[2]);
			GL11.glRotatef(par1.getBarrelYaw(), 0.0F, 1.0F, 0.0F);

			GL11.glPushMatrix();
			GL11.glTranslatef(-posY[0], -posY[1], -posY[2]);
			this.renderParts(set.model, set.getConfig().modelPartsY);
			GL11.glPopMatrix();

			float[] posX = set.getConfig().modelPartsX.pos;
			GL11.glTranslatef(posX[0] - posY[0], posX[1] - posY[1], posX[2] - posY[2]);
			GL11.glRotatef(par1.getBarrelPitch(), 1.0F, 0.0F, 0.0F);

			GL11.glPushMatrix();
			GL11.glTranslatef(-posX[0], -posX[1], -posX[2]);
			this.renderParts(set.model, set.getConfig().modelPartsX);
			GL11.glPopMatrix();

			float[] posB = set.getConfig().modelPartsBarrel.pos;
			GL11.glTranslatef(posB[0] - posX[0], posB[1] - posX[1], posB[2] - posX[2]);

			GL11.glPushMatrix();
			GL11.glTranslatef(-posB[0], -posB[1], -posB[2]);
			if (par1.recoilCount > 0) {
				float recoil = set.getConfig().recoil * ((float) par1.recoilCount / (float) EntityArtillery.MaxRecoilCount);
				GL11.glTranslatef(0.0F, 0.0F, -recoil);
			}
			this.renderParts(set.model, set.getConfig().modelPartsBarrel);
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
	}

	private void renderParts(IModelNGT model, Parts parts) {
		model.renderOnly(RTMCore.smoothing, parts.objects);
	}

	@Override
	public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
		this.renderArtillery((EntityArtillery) par1, par2, par4, par6, par8, par9);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity par1) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity entiy) {
	}

	private boolean shouldRender(EntityArtillery par1) {
		if (par1.riddenByEntity != null && par1.riddenByEntity.equals(NGTUtilClient.getMinecraft().thePlayer)) {
			if (ClientProxy.getViewMode(NGTUtilClient.getMinecraft().thePlayer) == ClientProxy.ViewMode_Artillery) {
				return false;
			}
		}
		return true;
	}
}