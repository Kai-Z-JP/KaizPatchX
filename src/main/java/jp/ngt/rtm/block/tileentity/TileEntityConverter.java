package jp.ngt.rtm.block.tileentity;

import jp.ngt.ngtlib.block.TileEntityCustom;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

public class TileEntityConverter extends TileEntityCustom {
    private int[] corePos = {0, 0, 0};
    private TileEntityConverterCore core;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.corePos = nbt.getIntArray("core");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setIntArray("core", this.corePos);
    }

    @Override
    public void updateEntity() {
        if (this.getCore() != null) {
            if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord)) {
                this.core.powered = true;
            }
        }
    }

    /**
     * null有り
     */
    public TileEntityConverterCore getCore() {
        if (this.core == null) {
            TileEntity tile = this.worldObj.getTileEntity(this.corePos[0], this.corePos[1], this.corePos[2]);
            if (tile instanceof TileEntityConverterCore) {
                this.core = (TileEntityConverterCore) tile;
            }
        }
        return this.core;
    }

    public void setCorePos(int x, int y, int z) {
        this.corePos = new int[]{x, y, z};
    }

    protected void sendPacket() {
        NGTUtil.sendPacketToClient(this);
    }

    @Override
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
    }

    @Override
    public void setPos(int x, int y, int z, int prevX, int prevY, int prevZ) {
        super.setPos(x, y, z, prevX, prevY, prevZ);
        this.corePos[0] += prevX - x;
        this.corePos[1] += prevY - y;
        this.corePos[2] += prevZ - z;
    }
}