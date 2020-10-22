package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityFluorescent extends TileEntity {
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

		if (this.getBlockMetadata() == 2) {
			++this.count;
			if (this.count == 3) {
				//明るさ更新
				this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);
				this.count = 0;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return NGTUtil.getChunkLoadDistanceSq();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
		return bb;
	}
}