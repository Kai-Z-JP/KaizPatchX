package jp.ngt.ngtlib.block;

import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

public abstract class TileEntityPlaceable extends TileEntity {
	private float rotation;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.setRotation(nbt.getFloat("Yaw"), false);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setFloat("Yaw", this.rotation);
	}

	public float getRotation() {
		return this.rotation;
	}

	public void setRotation(float par1, boolean synch) {
		this.rotation = par1 % 360.0F;
		if (synch) {
			this.getDescriptionPacket();
			this.markDirty();
		}
	}

	public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
		int yaw = MathHelper.floor_double(NGTMath.normalizeAngle(-player.rotationYaw + 180.0D + (rotationInterval / 2.0D)) / (double) rotationInterval);
		this.setRotation((float) yaw * rotationInterval, synch);
	}
}