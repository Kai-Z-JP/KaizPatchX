package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.gui.InternalButton;
import jp.ngt.rtm.gui.InternalGUI;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.network.PacketMarker;
import jp.ngt.rtm.network.PacketNotice;
import jp.ngt.rtm.rail.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public int editMode;

    public int startX, startY = -1, startZ;

    private List<int[]> markerPosList = new ArrayList<>();

    private RailMap[] railMaps;

    /**
     * {{x,y,z}}
     */
    private List<int[]> grid;

    public float startPlayerPitch;

    public float startPlayerYaw;

    public byte startMarkerHeight;

    private int markerState;

    @SideOnly(Side.CLIENT)
    public InternalGUI gui;

    @SideOnly(Side.CLIENT)
    public InternalButton[] buttons;

    @SideOnly(Side.CLIENT)
    public float[][][] linePos;

    private int count;

    public boolean fitNeighbor = true;

    public TileEntityMarker() {
        this.markerState = MarkerState.DISTANCE.set(this.markerState, true);
        this.markerState = MarkerState.GRID.set(this.markerState, false);
        this.markerState = MarkerState.LINE1.set(this.markerState, false);
        this.markerState = MarkerState.LINE2.set(this.markerState, false);
        this.markerState = MarkerState.ANCHOR21.set(this.markerState, false);
        this.markerState = MarkerState.FIT_NEIGHBOR.set(this.markerState, true);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("RP")) {
            this.rp = RailPosition.readFromNBT(nbt.getCompoundTag("RP"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (this.rp != null) {
            nbt.setTag("RP", this.rp.writeToNBT());
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.rp == null) {
            byte dir = BlockMarker.getMarkerDir(this.getBlockType(), this.getBlockMetadata());
            byte type = (byte) (this.getBlockType() == RTMBlock.markerSwitch ? 1 : 0);
            this.rp = new RailPosition(this.xCoord, this.yCoord, this.zCoord, dir, type);
        }
//		if (this.getWorldObj().isRemote) {
//			if (this.count >= 60) {
//				this.updateStartPos();
//				this.count = 0;
//			}
//			this.count++;
//		}
    }

    public void updateStartPos() {
        if (this.startY != -1) {
            TileEntity tileEntity = this.getWorldObj().getTileEntity(this.startX, this.startY, this.startZ);
            if (!(tileEntity instanceof TileEntityMarker)) {
                this.startY = -1;
            }
        } else {
            ((BlockMarker) getBlockType()).makeRailMap(this, xCoord, yCoord, zCoord, null);
        }
    }

    public RailPosition getMarkerRP() {
        return this.rp;
    }

    public void setMarkerRP(RailPosition par1) {
        this.rp = par1;
    }

    private RailPosition getMarkerRP(int[] pos) {
        return this.getMarkerRP(pos[0], pos[1], pos[2]);
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
        } else if (this.getCoreMarker() != null) {
            this.getCoreMarker().markerPosList.stream()
                    .map(pos -> this.getWorldObj().getTileEntity(pos[0], pos[1], pos[2]))
                    .filter(Objects::nonNull)
                    .forEach(tile -> {
                        String message = "marker," + par1;
                        RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, message, tile));
                    });
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

    public void onChangeRailShape() {

        this.getCoreMarker().updateRailMap();
//		if (!isCoreMarker()) {
//			TileEntityMarker marker = getCoreMarker();
//			if (marker != null) {
//				marker.onChangeRailShape();
//			}
//		} else {
//			RailMap[] maps = new RailMap[this.railMaps.length];
//			for (int i = 0; i < maps.length; i++) {
//				RailPosition rp0 = this.railMaps[i].getStartRP();
//				RailPosition rp1 = this.railMaps[i].getEndRP();
//				rp1.cantCenter = -rp0.cantCenter;
//				maps[i] = new RailMap(rp0, rp1);
//			}
//			this.railMaps = maps;
//			this.linePos = null;
//			createGrids();
//			for (int[] pos : this.markerPosList) {
//				TileEntity tile = this.worldObj.getTileEntity(pos[0], pos[1], pos[2]);
//				if (tile instanceof TileEntityMarker) {
//					TileEntityMarker marker = (TileEntityMarker) tile;
//					marker.railMaps = maps;
//				}
//			}
//		}
    }

    public void updateRailMap() {
        this.setMarkersPos(this.markerPosList);
    }

    /**
     * マーカーのグリッド表示用RailMap生成
     */
    public void setMarkersPos(List<int[]> list) {
        if (list.size() == 2) {
            RailPosition rp0 = this.getMarkerRP(list.get(0));
            RailPosition rp1 = this.getMarkerRP(list.get(1));
            if (rp0 != null && rp1 != null) {
                RailMap rm = new RailMapBasic(rp0, rp1, RailMapBasic.fixRTMRailMapVersionCurrent);
                this.railMaps = new RailMap[]{rm};
            }
        } else {
            List<RailPosition> list2 = new ArrayList<>();
            for (int[] ia : list) {
                RailPosition rp0 = this.getMarkerRP(ia[0], ia[1], ia[2]);
                if (rp0 != null) {
                    list2.add(rp0);
                }
            }
            RailMaker rm = new RailMaker(this.worldObj, list2, RailMapBasic.fixRTMRailMapVersionCurrent);
            SwitchType sw = rm.getSwitch();
            if (sw != null) {
                this.railMaps = sw.getAllRailMap();
            }
        }
        if (this.railMaps == null) {
            return;
        }
        this.markerPosList = list;
        if (this.getWorldObj().isRemote) {
            this.createGrids();
        }
        for (int[] pos : list) {
            TileEntity tile = this.worldObj.getTileEntity(pos[0], pos[1], pos[2]);
            if (tile instanceof TileEntityMarker) {
                TileEntityMarker marker = (TileEntityMarker) tile;
                marker.setStartPos(this.xCoord, this.yCoord, this.zCoord);
                marker.railMaps = this.railMaps;//RailMap同期
            }
        }


        if (!this.getWorldObj().isRemote) {
            RTMCore.NETWORK_WRAPPER.sendToAll(new PacketMarker(this, list));
        }
    }

    private void setStartPos(int x, int y, int z) {
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        if (!(this.xCoord == x && this.yCoord == y && this.zCoord == z)) {
            this.markerPosList.clear();
            this.railMaps = null;
            this.grid = null;
        }
    }


    private void createGrids() {
        this.grid = new ArrayList<>();
        for (RailMap rm : this.railMaps) {
            this.grid.addAll(rm.getRailBlockList(ItemRail.getDefaultProperty()));
        }
    }

    public boolean isCoreMarker() {
        return (this.startX == this.xCoord && this.startY == this.yCoord && this.startZ == this.zCoord);
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
        //GuiRailMarker用
        if (this.markerPosList.isEmpty()) {
            return new RailPosition[]{this.rp};
        }

        List<RailPosition> list2 = new ArrayList<>();
        for (int[] ia : this.markerPosList) {
            RailPosition rp0 = this.getMarkerRP(ia[0], ia[1], ia[2]);
            if (rp0 != null) {
                list2.add(rp0);
            }
        }
        return list2.toArray(new RailPosition[0]);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
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

    public void updateMarkerRM(EntityPlayer player) {
        ((BlockMarker) getBlockType()).makeRailMap(this, xCoord, yCoord, zCoord, player);
    }

    public boolean getState(MarkerState state) {
        return state.get(this.markerState);
    }

    public void flipState(MarkerState state) {
        boolean data = state.get(this.markerState);
        setState(state, !data);
    }

    public void setState(MarkerState state, boolean data) {
        this.updateStartPos();
        if (!isCoreMarker()) {
            TileEntityMarker marker = this.getCoreMarker();
            if (marker != null) {
                marker.setState(state, data);
            } else if (state == MarkerState.DISTANCE) {
                this.markerState = state.set(this.markerState, data);
            }
        } else {
            this.markerState = state.set(this.markerState, data);
            this.markerPosList.stream().map(pos -> this.getWorldObj().getTileEntity(pos[0], pos[1], pos[2])).filter(TileEntityMarker.class::isInstance).map(TileEntityMarker.class::cast).forEach(marker -> marker.markerState = this.markerState);
        }
    }

    public String getStateString(MarkerState state) {
        boolean data = state.get(this.markerState);
        return String.format("%s : %s", state.toString(), data ? "ON" : "OFF");
    }
}