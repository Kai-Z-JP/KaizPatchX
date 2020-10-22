package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.network.PacketMarker;
import jp.ngt.rtm.network.PacketNotice;
import jp.ngt.rtm.rail.util.RailMaker;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

public class TileEntityMarker extends TileEntity {
	private RailPosition rp;

	public boolean displayDistance = true;//同期必要なし
	/**
	 * 0:なし, 1:グリッド, 2:ベジェ
	 */
	private byte displayMode;
	public boolean followMouseMoving;//同期必要なし
	public EntityPlayer followingPlayer;
	private TileEntityMarker coreMarker;

	public int startX, startY = -1, startZ;

	private List<int[]> markerPosList;
	private RailMap[] railMaps;

	/**
	 * {{x,y,z}}
	 */
	private List<int[]> grid;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if (nbt.hasKey("RP")) {
			this.rp = RailPosition.readFromNBT(nbt.getCompoundTag("RP"));
		}

        /*this.height = nbt.getByte("Height");
        this.anchorDirection = nbt.getFloat("A_Direction");
        this.anchorLength = nbt.getFloat("A_Length");

        if(nbt.hasKey("GridList"))
        {
        	this.grid = new ArrayList<int[]>();
            NBTTagList tagList = nbt.getTagList("GridList", 11);
        	for(int i = 0; i < tagList.tagCount(); ++i)
        	{
        		int[] ia = tagList.func_150306_c(i);
        		this.grid.add(ia);
        	}
        }*/
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if (this.rp != null) {
			nbt.setTag("RP", this.rp.writeToNBT());
		}

        /*nbt.setByte("Height", this.height);
        nbt.setFloat("A_Direction", this.anchorDirection);
        nbt.setFloat("A_Length", this.anchorLength);

        if(this.grid != null)
        {
        	NBTTagList tagList = new NBTTagList();
            for(int[] ia : this.grid)
            {
            	NBTTagIntArray tag = new NBTTagIntArray(ia);
            	tagList.appendTag(tag);
            }
            nbt.setTag("GridList", tagList);
        }*/
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (this.rp == null) {
			byte dir = BlockMarker.getMarkerDir(this.getBlockType(), this.getBlockMetadata());
			byte type = (byte) (this.getBlockType() == RTMBlock.markerSwitch ? 1 : 0);
			this.rp = new RailPosition(this.xCoord, this.yCoord, this.zCoord, dir, type);
		}
	}

	public RailPosition getMarkerRP() {
		return this.rp;
	}

	public void setMarkerRP(RailPosition par1) {
		this.rp = par1;
	}

	private RailPosition getMarkerRP(int x, int y, int z) {
		TileEntity tile = this.worldObj.getTileEntity(x, y, z);
		if (tile instanceof TileEntityMarker) {
			return ((TileEntityMarker) tile).rp;
		}
		return null;
	}

	public byte getDisplayMode() {
		return this.displayMode;
	}

	public void changeDisplayMode() {
		if (!this.worldObj.isRemote) {
			this.setDisplayMode((byte) ((this.displayMode + 1) % 3));
		}
	}

	public void setDisplayMode(byte par1) {
		this.displayMode = par1;
		if (this.worldObj.isRemote) {
			;
		} else if (this.getCoreMarker() != null) {
			for (int[] ia : this.getCoreMarker().markerPosList) {
				TileEntity tile = this.getWorldObj().getTileEntity(ia[0], ia[1], ia[2]);
				String message = "marker," + par1;
				RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, message, tile));
			}
		}
	}

	public byte increaseHeight() {
		this.rp.setHeight((byte) ((this.rp.height + 1) % 16));
		return this.rp.height;
	}

	public List<int[]> getGrid() {
		return this.grid;
	}

	public RailMap[] getRailMaps() {
		return this.railMaps;
	}

	public void updateRailMap() {
		this.setMarkersPos(this.markerPosList, true);
	}

	/**
	 * マーカーのグリッド表示用RailMap生成
	 */
	public void setMarkersPos(List<int[]> list, boolean isClient) {
		if (list.size() == 2) {
			RailPosition rp0 = this.getMarkerRP(list.get(0)[0], list.get(0)[1], list.get(0)[2]);
			RailPosition rp1 = this.getMarkerRP(list.get(1)[0], list.get(1)[1], list.get(1)[2]);
			if (rp0 != null && rp1 != null) {
				RailMap rm = new RailMap(rp0, rp1);
				this.railMaps = new RailMap[]{rm};
			}
		} else {
			List<RailPosition> list2 = new ArrayList<RailPosition>();
			for (int[] ia : list) {
				RailPosition rp0 = this.getMarkerRP(ia[0], ia[1], ia[2]);
				if (rp0 != null) {
					list2.add(rp0);
				}
			}
			this.railMaps = (new RailMaker(this.getWorldObj(), list2).getSwitch().getAllRailMap());
		}

		if (this.railMaps == null) {
			return;
		}

		this.markerPosList = list;

		if (isClient) {
			this.grid = new ArrayList<int[]>();
			for (RailMap rm : this.railMaps) {
				boolean flag = false;
				this.grid.addAll(rm.getRailBlockList(ItemRail.getDefaultProperty()));
				/*double lengthX = (float)Math.abs(rm.startRP.posX - rm.endRP.posX);
				double lengthZ = (float)Math.abs(rm.startRP.posX - rm.endRP.posX);
				double d0 = lengthX <= lengthZ ? lengthX : lengthZ;
				d0 *= RailPosition.Anchor_Correction_Value;
				TileEntity tile = this.worldObj.getTileEntity(rm.startRP.blockX, rm.startRP.blockY, rm.startRP.blockZ);
				if(tile instanceof TileEntityMarker && ((TileEntityMarker)tile).rp.anchorLength < 0.0F)
				{
					((TileEntityMarker)tile).rp.anchorLength = (float)d0;
					flag = true;
				}

				tile = this.worldObj.getTileEntity(rm.endRP.blockX, rm.endRP.blockY, rm.endRP.blockZ);
				if(tile instanceof TileEntityMarker && ((TileEntityMarker)tile).rp.anchorLength < 0.0F)
				{
					((TileEntityMarker)tile).rp.anchorLength = (float)d0;
					flag = true;
				}

				if(flag){rm.rebuild();}*/
			}
		}

		for (int[] ia : list) {
			TileEntity tile = this.worldObj.getTileEntity(ia[0], ia[1], ia[2]);
			if (tile instanceof TileEntityMarker) {
				((TileEntityMarker) tile).setStartPos(this.xCoord, this.yCoord, this.zCoord);
			}
		}

		if (!isClient) {
			RTMCore.NETWORK_WRAPPER.sendToAll(new PacketMarker(this.xCoord, this.yCoord, this.zCoord, list));
		}
	}

	private void setStartPos(int x, int y, int z) {
		this.startX = x;
		this.startY = y;
		this.startZ = z;

		if (!(this.xCoord == x && this.yCoord == y && this.zCoord == z)) {
			this.markerPosList = null;
			this.railMaps = null;
			this.grid = null;
		}
	}

	public TileEntityMarker getCoreMarker() {
		if (this.startY < 0) {
			return null;
		}

		if (this.coreMarker == null || this.coreMarker.xCoord != this.startX || this.coreMarker.yCoord != this.startY || this.coreMarker.zCoord != this.startZ) {
			this.coreMarker = null;
			TileEntity tile = this.worldObj.getTileEntity(this.startX, this.startY, this.startZ);
			if (tile instanceof TileEntityMarker) {
				this.coreMarker = (TileEntityMarker) tile;
			}
		}
		return this.coreMarker;
	}

	public RailPosition[] getAllRP() {
		List<RailPosition> list2 = new ArrayList<RailPosition>();
		for (int[] ia : this.markerPosList) {
			RailPosition rp0 = this.getMarkerRP(ia[0], ia[1], ia[2]);
			if (rp0 != null) {
				list2.add(rp0);
			}
		}
		return list2.toArray(new RailPosition[list2.size()]);
	}

	@Override
	public Packet getDescriptionPacket() {
		NGTUtil.sendPacketToClient(this);
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
}