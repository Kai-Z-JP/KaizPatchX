package jp.ngt.rtm.entity.vehicle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.modelpack.cfg.VehicleConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityPlane extends EntityVehicle {
	public EntityPlane(World world) {
		super(world);
		this.stepHeight = 0.5F;
	}

	@Override
	protected boolean shouldUpdateMotion() {
		return true;
	}

	@Override
	protected void updateFallState() {
		if (this.speed == 0.0D) {
			super.updateFallState();
		}
	}

	@Override
	protected void updateMotion(EntityLivingBase entity, float moveStrafe, float moveForward) {
		VehicleConfig cfg = this.getModelSet().getConfig();

		this.speed += moveForward * cfg.getAcceleration(this.onGround);
		float maxSpeed = cfg.getMaxSpeed(this.onGround);
		float f0 = (float) ((moveStrafe + 0.02F) * cfg.getYawCoefficient(this.onGround));
		if (!cfg.changeYawOnStopping) {
			f0 *= (this.speed / maxSpeed);
		}
		float maxYaw = cfg.getMaxYaw(this.onGround);
		if (f0 > maxYaw) {
			f0 = maxYaw;
		} else if (f0 < -maxYaw) {
			f0 = -maxYaw;
		}
		this.rotationYaw += f0;

		if (this.speed > maxSpeed) {
			this.speed = maxSpeed;
		} else if (this.speed < 0.0D) {
			this.speed = 0.0D;
		}

		Vec3 vec = this.getMotionVec();
		this.motionX = vec.xCoord;
		this.motionZ = vec.zCoord;
		double d0 = 0.05D * (1.0D - (this.speed / maxSpeed));
		//this.motionY = this.speed > 0.0D ? vec.yCoord : this.motionY - 0.05D;
		this.motionY = vec.yCoord - d0;

		if (moveForward == 0.0F) {
			this.speed *= cfg.getFriction(this.onGround);
		}

		if (Math.abs(this.speed) < 0.001D) {
			this.speed = 0.0D;
			this.motionX = this.motionZ = 0.0D;
		}

		if (this.speed > 0.0D && !this.onGround) {
			this.rotationRoll = moveStrafe * (float) (this.speed / maxSpeed) * -cfg.getRollCoefficient(this.onGround);
		} else {
			this.rotationRoll *= 0.75F;
		}

		if (Math.abs(this.rotationRoll) < 0.01F) {
			this.rotationRoll = 0.0F;
		}
	}

	@Override
	protected Vec3 getMotionVec() {
		if ((this.onGround && this.rotationPitch < 0.0F) || this.inWater) {
			return super.getMotionVec();
		}
		Vec3 vec = Vec3.createVectorHelper(0.0D, 0.0D, this.speed);
		vec.rotateAroundX(NGTMath.toRadians(this.rotationPitch));
		vec.rotateAroundY(NGTMath.toRadians(this.rotationYaw));
		return vec;
	}

	@Override
	protected void updateRotation() {
		this.rotationPitch *= 0.99F;

		if (Math.abs(this.rotationPitch) < 0.01F) {
			this.rotationPitch = 0.0F;
		}
	}

	@Override
	public void setUpDown(int par1) {
		if (this.speed > 0.0D) {
			if (par1 != 0) {
				VehicleConfig cfg = this.getModelSet().getConfig();
				this.rotationPitch += cfg.getPitchCoefficient(this.onGround) * (float) par1 * (float) (this.speed / cfg.getMaxSpeed(this.onGround));
				if (this.onGround && this.rotationPitch < 1.0F) {
					this.rotationPitch = 0.0F;
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean disableUnmount() {
		float speed = this.dataWatcher.getWatchableObjectFloat(21);
		boolean onGround = this.dataWatcher.getWatchableObjectByte(22) == 1;
		return !onGround && speed > 0.0F;
	}

	@Override
	public String getDefaultName() {
		return "NGT-1";
	}

	@Override
	protected ItemStack getVehicleItem() {
		return new ItemStack(RTMItem.itemVehicle, 1, 2);
	}
}