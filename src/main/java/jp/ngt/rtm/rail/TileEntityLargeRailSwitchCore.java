package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketLargeRailCore;
import jp.ngt.rtm.rail.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class TileEntityLargeRailSwitchCore extends TileEntityLargeRailCore {
	private SwitchType switchObj;

	private final List<RailMapSwitch> openedRails = new ArrayList<>();

	public TileEntityLargeRailSwitchCore() {
		super();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	protected void readRailData(NBTTagCompound nbt) {
		if (nbt.hasKey("Size")) {
			byte size = nbt.getByte("Size");
			this.railPositions = new RailPosition[size];

			for (int i = 0; i < size; ++i) {
				this.railPositions[i] = RailPosition.readFromNBT(nbt.getCompoundTag("RP" + i));
			}
		} else//1.7.10.19互換
		{
			byte b0 = nbt.getByte("startDir");
			byte[] ba1 = nbt.getByteArray("endDir");
			int x0 = nbt.getInteger("spX");
			int y0 = nbt.getInteger("spY");
			int z0 = nbt.getInteger("spZ");
			int[] xa1 = nbt.getIntArray("epX");
			int[] ya1 = nbt.getIntArray("epY");
			int[] za1 = nbt.getIntArray("epZ");
			boolean s1 = nbt.getBoolean("switch1");
			boolean s2 = nbt.getBoolean("switch2");
			boolean s3 = nbt.getBoolean("switch3");

			byte switchType = nbt.getByte("type");
			this.railPositions = new RailPosition[switchType == 0 ? 3 : 4];
			this.railPositions[0] = this.getRP(xa1[0], ya1[0], za1[0], ba1[0], s1);
			this.railPositions[1] = this.getRP(xa1[1], ya1[1], za1[1], ba1[1], s2);
			this.railPositions[2] = this.getRP(xa1[2], ya1[2], za1[2], ba1[2], s3);
			if (switchType != 0) {
				boolean s4 = nbt.getBoolean("switch4");
				this.railPositions[3] = this.getRP(xa1[3], ya1[3], za1[3], ba1[3], s4);
			}
		}
	}

	private RailPosition getRP(int x, int y, int z, byte dir, boolean b) {

		RailPosition rp = new RailPosition(x, y, z, dir, (byte) (b ? 1 : 0));
		rp.anchorYaw = MathHelper.wrapAngleTo180_float(dir * 45.0F);
		return rp;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	protected void writeRailData(NBTTagCompound nbt) {
		nbt.setByte("Size", (byte) this.railPositions.length);

		for (int i = 0; i < this.railPositions.length; ++i) {
			nbt.setTag("RP" + i, this.railPositions[i].writeToNBT());
		}
	}

	@Override
	public void setRailPositions(RailPosition[] par1) {
		super.setRailPositions(par1);
		this.onBlockChanged();
	}

	@Override
	public void createRailMap() {
		if (this.isLoaded() && this.switchObj == null) {
			this.switchObj = (new RailMaker(this.getWorldObj(), this.railPositions).getSwitch());
		}
	}

	public SwitchType getSwitch() {
		if (this.switchObj == null) {
			this.createRailMap();
		}
		return this.switchObj;
	}

	@Override
	public void sendPacket() {
		RTMCore.NETWORK_WRAPPER.sendToAll(new PacketLargeRailCore(this, PacketLargeRailCore.TYPE_SWITCH));
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (this.getSwitch() != null) {
			this.getSwitch().onUpdate(this.getWorldObj());
		}
	}

	/**
	 * ブロック更新時
	 */
	public void onBlockChanged() {
		this.getSwitch().onBlockChanged(this.getWorldObj());
		if (!this.getWorldObj().isRemote) {
			this.sendPacket();//Clientへ更新を通知
		}
	}

	@Override
	public RailMap getRailMap(Entity entity) {
		SwitchType st = this.getSwitch();
		if (st == null) {
			return null;
		}

		if (entity == null) {
			return this.getAllRailMaps()[0];
		}

		return st.getRailMap(entity);
	}

	@Override
	public RailMapSwitch[] getAllRailMaps() {
		if (this.getSwitch() != null) {
			return this.getSwitch().getAllRailMap();
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected AxisAlignedBB getRenderAABB() {
		int[] size = this.getRailSize();
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(size[0] - 1, size[1], size[2] - 1, size[3] + 2, size[4] + 2, size[5] + 2);
		if (aabb.maxX - aabb.minX <= 3 && aabb.maxZ - aabb.minZ <= 3) {
			return null;
		}
		return aabb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int[] getRailSize() {
		int minX = this.startPoint[0];
		int maxX = this.startPoint[0];
		int minY = this.yCoord;
		int maxY = this.yCoord;
		int minZ = this.startPoint[2];
		int maxZ = this.startPoint[2];
		for (RailPosition rp : this.railPositions) {
			minX = Math.min(minX, rp.blockX);
			maxX = Math.max(maxX, rp.blockX);
			minZ = Math.min(minZ, rp.blockZ);
			maxZ = Math.max(maxZ, rp.blockZ);
		}
		return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
	}
}