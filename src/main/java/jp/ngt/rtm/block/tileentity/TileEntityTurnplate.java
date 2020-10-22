package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityTurnplate extends TileEntity {
	public int height;
	public int width;
	public int radius;
	public float speed;

	public boolean isMoving;
	public float rotation;

	public NGTObject blocksObject;
	@SideOnly(Side.CLIENT)
	public World dummyWorld;
	@SideOnly(Side.CLIENT)
	public DisplayList glList;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	public void updateEntity() {
		if (this.isMoving) {
			this.rotation += this.speed;
			this.rotation %= 360.0F;
		}
	}

	/**
	 * Server Only
	 */
	public void onBlockChanged() {
		boolean powered = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
		if (powered) {
			if (this.blocksObject == null) {
				this.setup();
			}
			this.isMoving = true;
		} else {
			this.isMoving = false;
		}
		this.getDescriptionPacket();
	}

	private void setup() {
		//NGTObject ngto = NGTOUtil.copyBlocks(world, x, y, z, width, height, depth);
	}

	/**
	 * ブロック破壊
	 */
	public void removed() {
		;
	}

	@Override
	public Packet getDescriptionPacket() {
		NGTUtil.sendPacketToClient(this);
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
		return INFINITE_EXTENT_AABB;
	}
}