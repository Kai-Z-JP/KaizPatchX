package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.BogieController.UpdateFlag;
import jp.ngt.rtm.network.PacketNotice;
import jp.ngt.rtm.rail.util.RailMapTurntable;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class TileEntityTurnTableCore extends TileEntityLargeRailCore {
    public static final float ROTATION_INC = 0.5F;
    public static final float ROTATION_STEP = 15.0F;

    private boolean isGettingPower;
    private float prevRotation;
    private float rotation;

    @Override
    protected void readRailData(NBTTagCompound nbt) {
        super.readRailData(nbt);
        this.rotation = nbt.getFloat("Rotation");

    }

    @Override
    protected void writeRailData(NBTTagCompound nbt) {
        super.writeRailData(nbt);
        nbt.setFloat("Rotation", this.rotation);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        boolean b = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
        if (this.isGettingPower ^ b) {
            this.isGettingPower = b;
        }

        if (!this.getWorldObj().isRemote) {
            float f0 = this.rotation % ROTATION_STEP;
            if (this.isGettingPower || (f0 != 0.0F)) {
                this.rotation += ROTATION_INC;
                if (this.rotation >= 360.0F) {
                    this.rotation = 0.0F;
                }
                RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, "TT:" + this.getRotation(), this));

                ((RailMapTurntable) this.getRailMap(null)).setRotation(this.rotation);
                this.updateTrainYaw();
            }
        } else {
            float f0 = this.rotation % ROTATION_STEP;
            if (!(this.isGettingPower || (f0 != 0.0F))) {
                this.prevRotation = this.rotation;
            }
        }
    }

    //ServerOnly
    private void updateTrainYaw() {
        List<EntityTrainBase> list = this.getWorldObj().getEntitiesWithinAABB(EntityTrainBase.class,
                AxisAlignedBB.getBoundingBox(this.xCoord - 5, this.yCoord, this.zCoord - 5, this.xCoord + 5, this.yCoord + 3, this.zCoord + 5));
        for (EntityTrainBase train : list) {
            Vec3 vec = PooledVec3.create(train.posX - (this.xCoord + 0.5D), 0.0D, train.posZ - (this.zCoord + 0.5D));
            vec = vec.rotateAroundY(ROTATION_INC);
            train.setPositionAndRotation((this.xCoord + 0.5D) + vec.getX(), train.posY, (this.zCoord + 0.5D) + vec.getZ(), train.rotationYaw + ROTATION_INC, train.rotationPitch);
            train.bogieController.updateBogiePos(train, 0, UpdateFlag.YAW);
            train.bogieController.updateBogiePos(train, 1, UpdateFlag.YAW);
        }
    }

    @Override
    public void createRailMap() {
        if (this.isLoaded())//同期ができてない状態でのRailMapの生成を防ぐ
        {
            RailPosition start = this.railPositions[0];
            RailPosition end = this.railPositions[1];
            int r = 0;
            if (start.blockX == end.blockX) {
                r = Math.abs(start.blockZ - end.blockZ) / 2;
            } else if (start.blockZ == end.blockZ) {
                r = Math.abs(start.blockX - end.blockX) / 2;
            }
            this.railmap = new RailMapTurntable(start, end, this.xCoord, this.yCoord, this.zCoord, r);
        }
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float rotation) {
        this.prevRotation = this.rotation;
        this.rotation = rotation;
    }

    public float getPrevRotation() {
        return this.prevRotation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected AxisAlignedBB getRenderAABB() {
        if (this.isLoaded()) {
            int startX = this.railPositions[0].blockX;
            int startZ = this.railPositions[0].blockZ;
            int endX = this.railPositions[1].blockX;
            int endZ = this.railPositions[1].blockZ;
            int lenHalf = (startX == endX) ? Math.abs(endZ - startZ) / 2 : Math.abs(endX - startX) / 2;
            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.xCoord - lenHalf, this.yCoord, this.zCoord - lenHalf,
                    this.xCoord + lenHalf + 1, this.yCoord + 3, this.zCoord + lenHalf + 1);
            return aabb;
        }
        return null;
    }

    @Override
    public void sendPacket() {
        super.sendPacket();

        if (this.worldObj == null || !this.worldObj.isRemote) {
            RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, "TT:" + this.getRotation(), this));
        }
    }

//    @Override
//    public String getRailShapeName() {
//        RailMap map = this.getRailMap(null);
//        StringBuilder sb = new StringBuilder();
//        sb.append("Type:TurnTable, ");
//        sb.append("X:").append(map.getEndRP().blockX - map.getStartRP().blockX).append(", ");
//        sb.append("Y:").append(map.getEndRP().blockY - map.getStartRP().blockY).append(", ");
//        sb.append("Z:").append(map.getEndRP().blockZ - map.getStartRP().blockZ);
//        return sb.toString();
//    }
}