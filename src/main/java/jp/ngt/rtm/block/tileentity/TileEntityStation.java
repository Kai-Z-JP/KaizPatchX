package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityStation extends TileEntity {
	private String stationName;
	public int width = 8, height = 3, depth = 8;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.stationName = nbt.getString("StationName");
		this.width = nbt.getInteger("Width");
		this.height = nbt.getInteger("Height");
		this.depth = nbt.getInteger("Depth");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("StationName", this.stationName);
		nbt.setInteger("Width", this.width);
		nbt.setInteger("Height", this.height);
		nbt.setInteger("Depth", this.depth);
	}

	public void setData(NBTTagCompound nbt) {
		this.readFromNBT(nbt);
		this.getDescriptionPacket();
		this.markDirty();
	}

	public String getName() {
		return this.stationName;
	}

	public void setName(String par1) {
		this.stationName = par1;
	}

	@Override
	public Packet getDescriptionPacket() {
		NGTUtil.sendPacketToClient(this);
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
}