package jp.ngt.ngtlib.block;

import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public abstract class TileEntityPlaceable extends TileEntityCustom {
    private float offsetX, offsetY, offsetZ, rotation;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.setOffset(
                nbt.getFloat("offsetX"),
                nbt.getFloat("offsetY"),
                nbt.getFloat("offsetZ"),
                false
        );
        this.setRotation(nbt.getFloat("Yaw"), false);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("Yaw", this.rotation);
        nbt.setFloat("offsetX", this.offsetX);
        nbt.setFloat("offsetY", this.offsetY);
        nbt.setFloat("offsetZ", this.offsetZ);
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ, boolean sync) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        if (sync) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float par1, boolean synch) {
        this.rotation = par1 % 360.0F;
        if (synch) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
        int yaw = MathHelper.floor_double(NGTMath.normalizeAngle(-player.rotationYaw + 180.0D + (rotationInterval / 2.0D)) / (double) rotationInterval);
        this.setRotation((float) yaw * rotationInterval, synch);
    }
}