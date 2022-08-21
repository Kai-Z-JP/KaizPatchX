package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.electric.MachineType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityPoint extends TileEntityMachineBase {
    private boolean activated = false;
    private float move = 24.0F;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.activated = nbt.getBoolean("Activated");
        this.move = nbt.getFloat("Move");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("Activated", this.activated);
        nbt.setFloat("Move", this.move);
    }

    public boolean isActivated() {
        return this.activated;
    }

    public void setActivated(boolean par1) {
        this.activated = par1;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public float getMove() {
        return this.move;
    }

    /**
     * 1m = 16.0F
     */
    public void setMove(float par1) {
        this.move = par1;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.Point;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
                this.xCoord - 1 + this.getOffsetX(),
                this.yCoord + this.getOffsetY(),
                this.zCoord - 1 + this.getOffsetZ(),
                this.xCoord + 2 + this.getOffsetX(),
                this.yCoord + 1 + this.getOffsetY(),
                this.zCoord + 2 + this.getOffsetZ());
    }

    @Override
    protected String getDefaultName() {
        return "Point01A";
    }
}