package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMRail;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailMapBasic;
import jp.ngt.rtm.rail.util.RailPosition;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TileEntityLargeRailCore extends TileEntityLargeRailBase {
    public boolean breaking;
    protected boolean isCollidedTrain = false;
    public boolean colliding = false;
    private int signal = 0;

    //private byte railShape;
    private byte railShapeTemp = -1;
    private RailProperty property = ItemRail.getDefaultProperty();
    public final List<RailProperty> subRails = new ArrayList<>();

    protected RailPosition[] railPositions;
    protected RailMap railmap;

    @SideOnly(Side.CLIENT)
    private AxisAlignedBB renderAABB;

    @SideOnly(Side.CLIENT)
    public DisplayList[] glLists;
    /**
     * レールを再描画するかどうか(明るさ変更等)
     */
    @SideOnly(Side.CLIENT)
    public boolean shouldRerenderRail;
    // see RailMapBasic.fixRTMRailMapVersion
    protected int fixRTMRailMapVersion;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("Property")) {
            this.property = RailProperty.readFromNBT(nbt.getCompoundTag("Property"));
        } else//.24互換
        {
            byte shape = nbt.getByte("railShape");
            //int texType = ((BlockLargeRailBase)this.getBlockType()).railTextureType;
            this.railShapeTemp = shape;
            this.property = RTMRail.getProperty(shape, 0);
        }
        //this.railShape = nbt.getByte("railShape");
        this.readRailProperties(nbt);
        this.readRailData(nbt);
    }

    public void readRailProperties(NBTTagCompound nbt) {
        if (nbt.hasKey("Property")) {
            this.property = RailProperty.readFromNBT(nbt.getCompoundTag("Property"));
            this.subRails.clear();
            NBTTagList list = nbt.getTagList("SubRails", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt1 = list.getCompoundTagAt(i);
                RailProperty property = RailProperty.readFromNBT(nbt1);
                this.subRails.add(property);
            }
        } else//.24互換
        {
            byte shape = nbt.getByte("railShape");
            //int texType = ((BlockLargeRailBase)this.getBlockType()).railTextureType;
            this.railShapeTemp = shape;
            this.property = RTMRail.getProperty(shape, 0);
        }
    }

    protected void readRailData(NBTTagCompound nbt) {
        this.railPositions = new RailPosition[2];
        if (nbt.hasKey("StartRP")) {
            this.railPositions[0] = RailPosition.readFromNBT(nbt.getCompoundTag("StartRP"));
            this.railPositions[1] = RailPosition.readFromNBT(nbt.getCompoundTag("EndRP"));
            this.fixRTMRailMapVersion = nbt.getInteger("fixRTMRailMapVersion");
        } else {
            byte b0 = nbt.getByte("startDir");
            byte b1 = nbt.getByte("endDir");
            int x0 = nbt.getInteger("spX");
            int y0 = nbt.getInteger("spY");
            int z0 = nbt.getInteger("spZ");
            int x1 = nbt.getInteger("epX");
            int y1 = nbt.getInteger("epY");
            int z1 = nbt.getInteger("epZ");

            this.railPositions[0] = new RailPosition(x0, y0, z0, b0);
            this.railPositions[1] = new RailPosition(x1, y1, z1, b1);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        //nbt.setByte("railShape", this.railShape);
        this.writeRailProperties(nbt);
        this.writeRailData(nbt);
    }

    public void writeRailProperties(NBTTagCompound nbt) {
        NBTTagCompound nbtProp = new NBTTagCompound();
        this.property.writeToNBT(nbtProp);
        nbt.setTag("Property", nbtProp);
        NBTTagList tagList = new NBTTagList();
        this.subRails.forEach(property -> {
            NBTTagCompound nbtProp1 = new NBTTagCompound();
            property.writeToNBT(nbtProp1);
            tagList.appendTag(nbtProp1);
        });
        nbt.setTag("SubRails", tagList);
    }

    protected void writeRailData(NBTTagCompound nbt) {
        nbt.setTag("StartRP", this.railPositions[0].writeToNBT());
        nbt.setTag("EndRP", this.railPositions[1].writeToNBT());
        nbt.setInteger("fixRTMRailMapVersion", this.fixRTMRailMapVersion);
    }

    public void createRailMap() {
        if (this.isLoaded())//同期ができてない状態でのRailMapの生成を防ぐ
        {
            this.railmap = new RailMapBasic(this.railPositions[0], this.railPositions[1], this.fixRTMRailMapVersion);
        }
    }

    /**
     * レール情報の読み込みが完了してるかどうか(=RailPositionが存在する)
     */
    public boolean isLoaded() {
        return (this.railPositions != null && this.railPositions.length > 0);
    }

    public RailPosition[] getRailPositions() {
        return this.railPositions;
    }

    public void setRailPositions(RailPosition[] par1) {
        this.railPositions = par1;
    }

    public RailProperty getProperty() {
        return this.property;
    }

    public void setProperty(String s, Block block, int p3, float p4) {
        this.property = new RailProperty(s, block, p3, p4);
    }

    public void setProperty(RailProperty p1) {
        this.property = p1;
    }

    public int getSignal() {
        return this.signal;
    }

    public void setSignal(int par1) {
        this.signal = par1;
    }

    @Override
    public TileEntityLargeRailCore getRailCore() {
        return this;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.getRailMap(null).getRailBlockList(this.property).forEach(pos -> this.worldObj.markBlockForUpdate(pos[0], pos[1], pos[2]));
        this.shouldRerenderRail = true;
    }

    @Override
    public void onChunkUnload() {
        if (this.worldObj.isRemote) {
            this.deleteGLList();
        }
    }

    @SideOnly(Side.CLIENT)
    private void deleteGLList() {
        if (this.glLists != null) {
            Arrays.stream(this.glLists).forEach(GLHelper::deleteGLList);
        }
        this.glLists = new DisplayList[this.subRails.size() + 1];
    }


    public void replaceRail(RailProperty state) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        state.writeToNBT(nbtTagCompound);
        this.property = RailProperty.readFromNBT(nbtTagCompound);
        this.subRails.clear();
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void addSubRail(RailProperty state) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        state.writeToNBT(nbtTagCompound);
        RailProperty newState = RailProperty.readFromNBT(nbtTagCompound);
        RailProperty oldState = this.subRails.stream().filter(state1 -> state1.railModel.equals(newState.railModel)).findFirst().orElse(null);
        if (oldState == null) {
            if (!this.getProperty().railModel.equals(newState.railModel)) {
                this.subRails.add(newState);
            }
        } else {
            this.subRails.remove(oldState);
        }
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void invalidate() {
        if (this.worldObj.isRemote) {
            this.deleteGLList();
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.worldObj.isRemote) {
            this.isCollidedTrain = this.colliding;
            this.colliding = false;

            if (this.railShapeTemp >= 0)//setWorld()やreadNBT()ではStackOverflow
            {
                int texType = ((BlockLargeRailBase) this.getBlockType()).railTextureType;
                this.property = RTMRail.getProperty(this.railShapeTemp, texType);
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                this.railShapeTemp = -1;
            }
        }
    }

    @Override
    public RailMap getRailMap(Entity entity) {
        if (this.railmap == null) {
            this.createRailMap();
        }
        return this.railmap;
    }

    public RailMap[] getAllRailMaps() {
        RailMap rm = this.getRailMap(null);
        return rm != null ? new RailMap[]{rm} : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (!this.isLoaded()) {
            return INFINITE_EXTENT_AABB;
        }

        if (this.renderAABB == null) {
            this.renderAABB = this.getRenderAABB();
            if (this.renderAABB == null) {
                return INFINITE_EXTENT_AABB;
            }//ぬるぽ回避
        }
        return this.renderAABB;
    }

    /**
     * レールの描画用AABBを取得<br>
     * 呼び出しは最初の1回のみ
     */
    @SideOnly(Side.CLIENT)
    protected AxisAlignedBB getRenderAABB() {
        int[] size = this.getRailSize();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(size[0] - 3.5, size[1] - 10, size[2] - 3.5, size[3] + 5.5, size[4] + 2, size[5] + 5.5);
        if (aabb.maxX - aabb.minX <= 3 && aabb.maxZ - aabb.minZ <= 3) {
            return null;
        }
        return aabb;
    }

    /**
     * {XMin, YMin, ZMin, XMax, YMax, ZMax}
     */
    @SideOnly(Side.CLIENT)
    public int[] getRailSize() {
        int startX = this.railPositions[0].blockX;
        int startY = this.railPositions[0].blockY;
        int startZ = this.railPositions[0].blockZ;
        int endX = this.railPositions[1].blockX;
        int endY = this.railPositions[1].blockY;
        int endZ = this.railPositions[1].blockZ;

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minY = Math.min(startY, endY);
        int maxY = Math.max(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxZ = Math.max(startZ, endZ);
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    /*@SideOnly(Side.CLIENT)
	public FloatBuffer getRenderMatrix()
	{
		return this.renderMatrix;
	}

	@SideOnly(Side.CLIENT)
	public void setRenderMatrix(FloatBuffer par1)
	{
		this.renderMatrix = par1;
	}

	//{x, y, z, yaw, pitch}
	@SideOnly(Side.CLIENT)
	public float[][] getRenderRailPos()
	{
		return this.renderRailPos;
	}

	@SideOnly(Side.CLIENT)
	public void setRenderRailPos(float[][] par1)
	{
		this.renderRailPos = par1;
	}*/

    @Override
    public void setPos(int x, int y, int z, int prevX, int prevY, int prevZ) {
        int difX = x - prevX;
        int difY = y - prevY;
        int difZ = z - prevZ;
        Arrays.stream(this.railPositions).forEach(rp -> rp.movePos(difX, difY, difZ));
        super.setPos(x, y, z, prevX, prevY, prevZ);
    }

    /**
     * レール形状の説明を取得(アイテム表示用)
     */
    public abstract String getRailShapeName();
}