package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.electric.MachineType;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityTurnstile extends TileEntityMachineBase {
    private int count = 0;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.count = nbt.getInteger("Count");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Count", this.count);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.count > 0) {
            --this.count;
        } else {
            int meta = this.getBlockMetadata();
            if (meta >= 4) {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, meta - 4, 2);
                //this.worldObj.playAuxSFX(1003, this.xCoord, this.yCoord, this.zCoord, 0);
            }
        }
    }

    /**
     * 通り抜けられる
     */
    public boolean canThrough() {
        return this.count > 0;
    }

    public void setCount(int par1) {
        this.count = par1;
        if (!this.worldObj.isRemote) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.Turnstile;
    }

    @Override
    protected String getDefaultName() {
        return "Turnstile01";
    }
}