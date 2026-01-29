package jp.ngt.ngtlib.block;

import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public abstract class TileEntityPlaceable extends TileEntityCustom {
    private float offsetX, offsetY, offsetZ, roll, pitch, yaw;
    private float scale = 1.0F;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.setOffset(
                nbt.getFloat("offsetX"),
                nbt.getFloat("offsetY"),
                nbt.getFloat("offsetZ"),
                false
        );
        this.setRotationYaw(nbt.getFloat("Yaw"), false);
        this.setRotationRoll(nbt.getFloat("RotationRoll"), false);
        this.setRotationPitch(nbt.getFloat("RotationPitch"), false);
        this.scale = nbt.hasKey("Scale") ? nbt.getFloat("Scale") : 1.0F;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("Yaw", this.yaw);
        nbt.setFloat("RotationRoll", this.roll);
        nbt.setFloat("RotationPitch", this.pitch);
        nbt.setFloat("offsetX", this.offsetX);
        nbt.setFloat("offsetY", this.offsetY);
        nbt.setFloat("offsetZ", this.offsetZ);
        nbt.setFloat("Scale", this.scale);
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

    public float getRotationRoll() {
        return this.roll;
    }

    public void setRotationRoll(float roll, boolean sync) {
        this.roll = roll % 360.0F;
        if (sync) {
            this.syncUpdate();
        }
    }

    public float getRotationPitch() {
        return this.pitch;
    }

    public void setRotationPitch(float pitch, boolean sync) {
        this.pitch = pitch % 360.0F;
        if (sync) {
            this.syncUpdate();
        }
    }

    public float getRotationYaw() {
        return this.yaw;
    }

    public void setRotationYaw(float yaw, boolean sync) {
        this.yaw = yaw % 360.0F;
        if (sync) {
            this.syncUpdate();
        }
    }

    public float getRotation() {
        return this.getRotationYaw();
    }

    public void setRotation(float par1, boolean sync) {
        this.setRotationYaw(par1, sync);
    }

    public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
        int yaw = MathHelper.floor_double(NGTMath.normalizeAngle(-player.rotationYaw + 180.0D + (rotationInterval / 2.0D)) / (double) rotationInterval);
        this.setRotation((float) yaw * rotationInterval, synch);
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale, boolean sync) {
        this.scale = scale;
        if (sync) {
            this.syncUpdate();
        }
    }

    private void syncUpdate() {
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }
}