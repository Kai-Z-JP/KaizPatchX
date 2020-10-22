package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityMeltedMetalFX extends EntityFX {
	public EntityMeltedMetalFX(World world, double x, double y, double z, double mX, double mY, double mZ) {
		super(world, x, y, z, mX, mY, mZ);
		this.motionX = mX + (Math.random() * 0.01D) - 0.005D;
		this.motionY = 0.0D;
		this.motionZ = mZ + (Math.random() * 0.01D) - 0.005D;
		this.particleMaxAge = (int) (15.0D / (Math.random() * 0.8D + 0.2D));
		this.particleScale = 0.8F;
		this.noClip = false;
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7) {
		super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
	}

	@Override
	public int getBrightnessForRender(float p_70070_1_) {
		int i = (int) (255.0F * (1.0F - ((float) this.particleAge / (float) this.particleMaxAge)));
		return (i << 16) + (i << 8) + i;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setDead();
		}

		this.motionY -= 0.05D;

		if (this.onGround) {
			this.motionY *= -(10.0D + Math.random() * 4.0D * (1.0F - ((float) this.particleAge / (float) this.particleMaxAge)));
			this.motionX += (Math.random() * 0.08D) - 0.04D;
			this.motionZ += (Math.random() * 0.08D) - 0.04D;
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 2;
	}
}