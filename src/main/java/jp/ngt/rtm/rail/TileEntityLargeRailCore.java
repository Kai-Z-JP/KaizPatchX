package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMRail;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.network.PacketLargeRailCore;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;

import java.util.Arrays;

public abstract class TileEntityLargeRailCore extends TileEntityLargeRailBase {
    public boolean breaking;
    private long lastRenderTime;
    private TileEntityLargeRailBase lastRenderTileEntity;
    protected boolean isCollidedTrain = false;
    public boolean colliding = false;
    private int signal = 0;

    //private byte railShape;
    private byte railShapeTemp = -1;
    private RailProperty property = ItemRail.getDefaultProperty();

    protected RailPosition[] railPositions;
    protected RailMap railmap;

    @SideOnly(Side.CLIENT)
    private AxisAlignedBB renderAABB;

    @SideOnly(Side.CLIENT)
    public DisplayList glList;
    /**
     * レールを再描画するかどうか(明るさ変更等)
     */
    @SideOnly(Side.CLIENT)
    public boolean shouldRerenderRail;

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
        this.readRailData(nbt);
    }

    protected void readRailData(NBTTagCompound nbt) {
        this.railPositions = new RailPosition[2];
        if (nbt.hasKey("StartRP")) {
            this.railPositions[0] = RailPosition.readFromNBT(nbt.getCompoundTag("StartRP"));
            this.railPositions[1] = RailPosition.readFromNBT(nbt.getCompoundTag("EndRP"));
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
        NBTTagCompound nbtProp = new NBTTagCompound();
        this.property.writeToNBT(nbtProp);
        nbt.setTag("Property", nbtProp);
        this.writeRailData(nbt);
    }

    protected void writeRailData(NBTTagCompound nbt) {
        nbt.setTag("StartRP", this.railPositions[0].writeToNBT());
        nbt.setTag("EndRP", this.railPositions[1].writeToNBT());
    }

    public void createRailMap() {
        if (this.isLoaded())//同期ができてない状態でのRailMapの生成を防ぐ
        {
            this.railmap = new RailMap(this.railPositions[0], this.railPositions[1]);
        }
    }

    public boolean shouldRender(TileEntityLargeRailBase tileEntity) {
        return this.lastRenderTime == Minecraft.getSystemTime() && this.lastRenderTileEntity != tileEntity;
    }

    public void setTickRender(TileEntityLargeRailBase tileEntity) {
        this.lastRenderTime = Minecraft.getSystemTime();
        this.lastRenderTileEntity = tileEntity;
    }

    /**
     * レール情報の読み込みが完了してるかどうか(=RailPositionが存在する)
     */
    public boolean isLoaded() {
        return this.railPositions != null;
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
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
    }

    public void sendPacket() {
        RTMCore.NETWORK_WRAPPER.sendToAll(new PacketLargeRailCore(this, PacketLargeRailCore.TYPE_NORMAL));
    }

    @Override
    public void onChunkUnload() {
        if (this.worldObj.isRemote) {
            GLHelper.deleteGLList(this.glList);
        }
    }

    @Override
    public void invalidate() {
        if (this.worldObj.isRemote) {
            GLHelper.deleteGLList(this.glList);
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
                this.sendPacket();
                this.markDirty();
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
        return new RailMap[]{this.getRailMap(null)};
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

//	@Override
//	@SideOnly(Side.CLIENT)
//	public AxisAlignedBB getRenderBoundingBox() {
//		if (!this.isLoaded()) {
//			return INFINITE_EXTENT_AABB;
//		}
//
//		if (this.renderAABB == null) {
//			this.renderAABB = this.getRenderAABB();
//			if (this.renderAABB == null) {
//				return INFINITE_EXTENT_AABB;
//			}//ぬるぽ回避
//		}
//		return this.renderAABB;
//	}

    /**
     * レールの描画用AABBを取得<br>
     * 呼び出しは最初の1回のみ
     */
    @SideOnly(Side.CLIENT)
    protected AxisAlignedBB getRenderAABB() {
        int[] size = this.getRailSize();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(size[0] - 1, size[1], size[2] - 1, size[3] + 2, size[4] + 2, size[5] + 2);
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
}