package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketLargeRailCore;
import jp.ngt.rtm.rail.util.RailMapSlope;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityLargeRailSlopeCore extends TileEntityLargeRailCore {
    private byte slopeType;

    public TileEntityLargeRailSlopeCore() {
        super();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.slopeType = nbt.getByte("slopeType");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setByte("slopeType", this.slopeType);
    }

    @Override
    public void createRailMap() {
        if (this.railPositions != null) {
            this.railmap = new RailMapSlope(this.railPositions[0], this.railPositions[1], this.slopeType);
        }
    }

    public byte getSlopeType() {
        return this.slopeType;
    }

    public void setSlopeType(byte par1) {
        this.slopeType = par1;
    }

    @Override
    public void sendPacket() {
        RTMCore.NETWORK_WRAPPER.sendToAll(new PacketLargeRailCore(this, PacketLargeRailCore.TYPE_SLOPE));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
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
        int startX = this.railPositions[0].blockX;
        int endX = this.railPositions[1].blockX;
        int startZ = this.railPositions[0].blockZ;
        int endZ = this.railPositions[1].blockZ;

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minY = this.yCoord;
        int maxY = this.yCoord;
        int minZ = Math.min(startZ, endZ);
        int maxZ = Math.max(startZ, endZ);
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
}