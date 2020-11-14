package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.stream.IntStream;

public class TileEntityPipe extends TileEntity {
	/**
	 * 0:x, 1:y, 2:z
	 */
	private byte direction;
	/**
	 * 0:Non, 1:Block, 2:Pipe 3, Slot
	 */
	public byte[] connection = new byte[6];

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.direction = nbt.getByte("dir");
		this.connection = nbt.getByteArray("connection");
		if (this.connection.length < 6) {
			this.connection = new byte[6];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("dir", this.direction);
		nbt.setByteArray("connection", this.connection);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
	}

	public byte getDirection() {
		return this.direction;
	}

	public void setDirection(byte par1) {
		this.direction = par1;
		this.sendPacket();
	}

	public void searchConnection() {
		IntStream.range(0, 6).forEach(i -> {
			int x0 = this.xCoord + BlockUtil.facing[i][0];
			int y0 = this.yCoord + BlockUtil.facing[i][1];
			int z0 = this.zCoord + BlockUtil.facing[i][2];
			Block block = this.worldObj.getBlock(x0, y0, z0);
			if (block == RTMBlock.slot) {
				this.connection[i] = 3;
			} else if (block == RTMBlock.pipe) {
				this.connection[i] = 2;
			} else if (block.isOpaqueCube()) {
				this.connection[i] = 1;
			} else {
				this.connection[i] = 0;
			}
		});

		if (this.getBlockMetadata() % 2 == 0) {
			//まっすぐパイプ
		}

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
		return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
	}
}