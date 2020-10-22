package jp.ngt.rtm.entity.train;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.cfg.VehicleBaseConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetTrainClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderBogie extends Render {
	public RenderBogie() {
		super();
		this.shadowSize = 1.0F;
	}

	private final void renderBogie(EntityBogie bogie, double par2, double par4, double par6, float par8, float partialTick) {
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		double x = par2;
		double y = par4;
		double z = par6;
		if (bogie.getTrain() != null) {
			EntityTrainBase train = bogie.getTrain();
			//RenderMngで足されてる補完値を引く
			double bogieFX = bogie.lastTickPosX + (bogie.posX - bogie.lastTickPosX) * (double) partialTick;
			double bogieFY = bogie.lastTickPosY + (bogie.posY - bogie.lastTickPosY) * (double) partialTick;
			double bogieFZ = bogie.lastTickPosZ + (bogie.posZ - bogie.lastTickPosZ) * (double) partialTick;

			float[][] pos = train.getModelSet().getConfig().getBogiePos();
			int bogieIndex = bogie.getBogieId();
			Vec3 v31 = new Vec3(pos[bogieIndex][0], pos[bogieIndex][1], pos[bogieIndex][2]);
			v31 = v31.rotateAroundX(train.prevRotationPitch + MathHelper.wrapAngleTo180_float(train.rotationPitch - train.prevRotationPitch) * partialTick);
			v31 = v31.rotateAroundY(train.prevRotationYaw + MathHelper.wrapAngleTo180_float(train.rotationYaw - train.prevRotationYaw) * partialTick);
			double newX = v31.getX() + (train.lastTickPosX + ((train.posX - train.lastTickPosX) * partialTick));
			double newY = v31.getY() + (train.lastTickPosY + ((train.posY - train.lastTickPosY) * partialTick));
			double newZ = v31.getZ() + (train.lastTickPosZ + ((train.posZ - train.lastTickPosZ) * partialTick));
			x = par2 - bogieFX + newX;
			y = par4 - bogieFY + newY;
			z = par6 - bogieFZ + newZ;
		}
		GL11.glTranslatef((float) x, (float) y, (float) z);

		float yaw = bogie.prevRotationYaw + MathHelper.wrapAngleTo180_float(bogie.rotationYaw - bogie.prevRotationYaw) * partialTick;
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
		float pitch = bogie.prevRotationPitch + (bogie.rotationPitch - bogie.prevRotationPitch) * partialTick;
		GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);

		byte index = bogie.getBogieId();
		boolean flag = true;
		if (bogie.getTrain() != null) {
			ModelSetTrainClient modelset = (ModelSetTrainClient) bogie.getTrain().getModelSet();
			if (!modelset.isDummy()) {
				VehicleBaseConfig cfg = modelset.getConfig();
				modelset.bogieModels[index].render(bogie, cfg, 0, partialTick);
				flag = false;
			}
		}

		if (flag) {
			RTMCore.proxy.renderMissingModel();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
		this.renderBogie((EntityBogie) par1, par2, par4, par6, par8, par9);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation("textures/train/bogie.png");
	}
}