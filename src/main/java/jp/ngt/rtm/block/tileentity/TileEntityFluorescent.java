package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.block.OrnamentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class TileEntityFluorescent extends TileEntityOrnament {
    private int count = 0;
    public byte dirF;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.dirF = nbt.getByte("dir");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("dir", this.dirF);
    }

    public byte getDir() {
        return this.dirF;
    }

    public void setDir(byte byte0) {
        this.dirF = byte0;

    }

    @Override
    public Packet getDescriptionPacket() {
        NGTUtil.sendPacketToClient(this);
        return null;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.worldObj != null && !this.worldObj.isRemote) {
            int meta = this.getBlockMetadata();
            if (meta >= 2 && this.getModelName().equals(this.getDefaultName())) {
                this.setModelName(meta == 2 ? "Fluorescent01Broken" : "FluorescentCovered01");
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
    }

    @Override
    public OrnamentType getOrnamentType() {
        return OrnamentType.Lamp;
    }

    @Override
    protected String getDefaultName() {
        return "Fluorescent01";
    }
}