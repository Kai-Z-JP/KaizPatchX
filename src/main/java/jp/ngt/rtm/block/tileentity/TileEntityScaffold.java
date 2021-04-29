package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityScaffold extends TileEntity {
    private byte dir;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.dir = nbt.getByte("direction");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("direction", this.dir);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
    }

    public byte getDir() {
        return this.dir;
    }

    public void setDir(byte par1) {
        this.dir = par1;
        this.sendPacket();
    }

    protected void sendPacket() {
        if (!this.worldObj.isRemote) {
            NGTUtil.sendPacketToClient(this);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 2, this.zCoord + 1);
    }
}