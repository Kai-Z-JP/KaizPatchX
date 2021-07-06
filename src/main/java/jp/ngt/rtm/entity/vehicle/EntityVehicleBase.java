package jp.ngt.rtm.entity.vehicle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.train.parts.EntityVehiclePart;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.ScriptExecuter;
import jp.ngt.rtm.modelpack.cfg.VehicleBaseConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class EntityVehicleBase<T extends VehicleBaseConfig> extends Entity implements IModelSelectorWithType {
    public static final int MAX_SEAT_ROTATION = 45;
    public static final int MAX_DOOR_MOVE = 60;
    public static final int MAX_PANTOGRAPH_MOVE = 80;
    public static final float TO_ANGULAR_VELOCITY = (float) (360.0D / Math.PI);

    private final ResourceState state = new ResourceState(this);
    /**
     * 直接参照は非推奨
     */
    private ModelSetVehicleBase<T> myModelSet;
    protected List<EntityFloor> vehicleFloors = new ArrayList<>();
    protected final IUpdateVehicle soundUpdater;
    private final ScriptExecuter executer = new ScriptExecuter();

    private boolean floorLoaded;
    /**
     * TrainTrackerEntryを生成したかどうか
     */
    private boolean tracked;
    private EntityLivingBase rider;

    public float rotationRoll;
    public float prevRotationRoll;

    @SideOnly(Side.CLIENT)
    public int seatRotation;
    @SideOnly(Side.CLIENT)
    public int rollsignAnimation;
    @SideOnly(Side.CLIENT)
    public int rollsignV;

    @SideOnly(Side.CLIENT)
    public int doorMoveL;
    @SideOnly(Side.CLIENT)
    public int doorMoveR;
    @SideOnly(Side.CLIENT)
    public int pantograph_F;
    @SideOnly(Side.CLIENT)
    public int pantograph_B;
    @SideOnly(Side.CLIENT)
    public float wheelRotationR;
    @SideOnly(Side.CLIENT)
    public float wheelRotationL;

    @SideOnly(Side.CLIENT)
    protected int vehiclePosRotationInc;
    @SideOnly(Side.CLIENT)
    protected double vehicleX, vehicleY, vehicleZ;
    @SideOnly(Side.CLIENT)
    protected float vehicleYaw, vehiclePitch, vehicleRoll;

    public EntityVehicleBase(World world) {
        super(world);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.soundUpdater = world != null ? RTMCore.proxy.getSoundUpdater(this) : null;
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(20, this.getDefaultName());
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass >= 0;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1) {
        if (par1 instanceof EntityVehiclePart) {
            if (((EntityVehiclePart) par1).getVehicle() == this) {
                return null;
            }
        } else if (par1 instanceof EntityBogie) {
            if (((EntityBogie) par1).getTrain() == this) {
                return null;
            }
        }
        return par1.boundingBox;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setString("ModelName", this.getModelName());
        nbt.setTag("State", this.getResourceState().writeToNBT());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("trainName")) {
            this.setModelName(nbt.getString("trainName"));//互換性
        } else {
            this.setModelName(nbt.getString("ModelName"));
        }
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
    }

    public void setFloor(EntityFloor par1)//EntityFloorから
    {
//		this.floorLoaded = true;
        this.vehicleFloors.add(par1);
    }

    @Override
    public void setDead() {
        super.setDead();

        if (!this.worldObj.isRemote) {
            this.vehicleFloors.stream().filter(Objects::nonNull).forEach(Entity::setDead);
        }
    }

    @Override
    public final void onUpdate() {
        this.onVehicleUpdate();
    }

    protected void onVehicleUpdate() {
        super.onUpdate();

        this.prevRotationRoll = this.rotationRoll;

        if (this.worldObj.isRemote) {
            if (this.soundUpdater != null) {
                this.soundUpdater.update();
            }

            this.updateAnimation();
            this.updatePosAndRotationClient();
        } else//!isRemote
        {
            if (!this.tracked) {
                this.tracked = VehicleTrackerEntry.trackingVehicle(this);
            }

            if (!this.floorLoaded) {
                this.setupFloors(this.myModelSet);
            }

            Entity passenger = this.riddenByEntity;
            if (passenger != null) {
                if (this.rider == null && passenger instanceof EntityLivingBase) {
                    this.rider = (EntityLivingBase) passenger;
                }
            } else if (this.rider != null) {
                //fixRiderPos(this.rider, this);
                this.rider = null;
            }

            this.executer.execScript(this);

            this.updateBlockCollisionState();
            this.updateEntityCollisionState();

            this.updateFallState();
            this.updateMovement();
        }
    }

    /**
     * (Server Only)
     */
    protected void updateBlockCollisionState() {
    }

    /**
     * (Server Only)
     */
    protected void updateEntityCollisionState() {
    }

    protected void updateFallState() {
        this.motionY -= 0.05D;
    }

    protected void updateMovement() {
        this.applyPhysicalEffect();
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
    }

    protected void applyPhysicalEffect() {
        this.motionX *= 0.9900000095367432D;
        this.motionY *= 0.949999988079071D;
        this.motionZ *= 0.9900000095367432D;
    }

    @SideOnly(Side.CLIENT)
    protected void updatePosAndRotationClient() {
        if (this.vehiclePosRotationInc > 0) {
            float d0 = 1.0F / (float) this.vehiclePosRotationInc;
            this.posX += (this.vehicleX - this.posX) * d0;
            this.posY += (this.vehicleY - this.posY) * d0;
            this.posZ += (this.vehicleZ - this.posZ) * d0;
            this.rotationYaw += MathHelper.wrapAngleTo180_float((float) (this.vehicleYaw - (double) this.rotationYaw)) * d0;
            this.rotationPitch += (this.vehiclePitch - (double) this.rotationPitch) * d0;
            this.rotationRoll = this.getRoll() + (this.vehicleRoll - this.getRoll()) * d0;
            --this.vehiclePosRotationInc;
        }

        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * 降りたEntityがハマらないように位置修正
     */
    public static void fixRiderPos(EntityLivingBase entity, Entity vehicle) {
        World world = entity.worldObj;
        AxisAlignedBB aabb = vehicle.getBoundingBox();
        if (entity.posX >= aabb.minX && entity.posX < aabb.maxX && entity.posZ >= aabb.minZ && entity.posZ < aabb.maxZ) {
            double range = 0.5D;
            int x0 = MathHelper.floor_double(aabb.minX - range);
            int x1 = MathHelper.floor_double(aabb.maxX + range);
            int z0 = MathHelper.floor_double(aabb.minZ - range);
            int z1 = MathHelper.floor_double(aabb.maxZ + range);
            int y = MathHelper.floor_double(aabb.minY);
            for (int i = x0; i <= x1; ++i) {
                for (int j = z0; j <= z1; ++j) {
                    for (int k = y - 2; k <= y + 2; ++k) {
                        if ((i != 0 || j != 0) && (i < aabb.minX || i >= aabb.maxX || j < aabb.minZ || j >= aabb.maxZ)) {
                            List list = world.getEntitiesWithinAABBExcludingEntity(entity, entity.boundingBox.getOffsetBoundingBox(i, k, j));
                            if (list.isEmpty() && World.doesBlockHaveSolidTopSurface(world, i, k, j)) {
                                if (world.isAirBlock(i, k + 1, j) && world.isAirBlock(i, k + 2, j)) {
                                    entity.setPositionAndUpdate(i, (double) k + 1.0D, j);
                                    //NGTLog.debug("fixPos");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateAnimation() {
        float speed = this.getSpeed();
        float f0 = speed * TO_ANGULAR_VELOCITY * this.getModelSet().getConfig().wheelRotationSpeed * this.getMoveDir();

        this.wheelRotationR = (this.wheelRotationR + f0) % 360.0F;
        this.wheelRotationL = (this.wheelRotationL + f0) % 360.0F;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int par6) {
        this.vehiclePosRotationInc = par6;
        this.vehicleX = x;
        this.vehicleY = y;
        this.vehicleZ = z;
        this.vehicleYaw = yaw;
        this.vehiclePitch = pitch;

        //NGTLog.debug("x:%4.6f, z:%4.6f", this.posX - this.vehicleX, this.posZ - this.vehicleZ);
    }

    @SideOnly(Side.CLIENT)
    public void setRollAndSpeed(float speed, float roll) {
        this.setSpeed(speed);
        this.vehicleRoll = roll;
    }

    public float getRoll() {
        return this.rotationRoll;
    }

    /**
     * +1.0 or -1.0
     */
    protected float getMoveDir() {
        return 1.0F;
    }

    public abstract float getSpeed();

    public abstract void setSpeed(float par1);

    @Override
    public ModelSetVehicleBase<T> getModelSet() {
        boolean flag = this.myModelSet != null && this.myModelSet.isDummy();
        if (this.myModelSet == null || flag || !this.myModelSet.getConfig().getName().equals(this.getModelName())) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet(this.getModelType(), this.getModelName());
            if (flag && this.myModelSet.isDummy()) {
                return this.myModelSet;
            }
            this.onModelChanged();
        }
        return this.myModelSet;
    }

    protected void onModelChanged() {
        if (this.worldObj == null || !this.worldObj.isRemote) {
            PacketNBT.sendToClient(this);
            this.floorLoaded = false;
        }

        if (this.worldObj.isRemote) {
            this.soundUpdater.onModelChanged();
        }
    }

    /**
     * Server Only
     */
    private void setupFloors(ModelSetVehicleBase<T> par1)//getModelSetでループしないように
    {
        this.vehicleFloors.stream().filter(Objects::nonNull).forEach(Entity::setDead);

        this.vehicleFloors.clear();

        for (int i = 0; i < par1.getConfig().getSlotPos().length; ++i) {
            float[] fa = par1.getConfig().getSlotPos()[i];
            EntityFloor floor = new EntityFloor(this.worldObj, this, new float[]{fa[0], fa[1], fa[2]}, (byte) fa[3]);
            if (this.worldObj.spawnEntityInWorld(floor)) {
                this.vehicleFloors.add(floor);
            } else {
                this.floorLoaded = false;//1つでもスポーン失敗したら、やり直し
                break;
            }
        }
        this.floorLoaded = true;
    }

    @Override
    public String getModelName() {
        return this.dataWatcher.getWatchableObjectString(20);
    }

    @Override
    public void setModelName(String name) {
        this.dataWatcher.updateObject(20, (name.isEmpty() ? this.getDefaultName() : name));//ミニチュアでDWは非同期のため
        if (!this.worldObj.isRemote) {
            this.floorLoaded = false;
        }
    }

    @Override
    public int[] getPos() {
        return new int[]{this.getEntityId(), -1, 0};
    }

    @Override
    public boolean closeGui(String par1, ResourceState par2) {
        return true;
    }

    @Override
    public ResourceState getResourceState() {
        return this.state;
    }

    public abstract String getDefaultName();

    @SideOnly(Side.CLIENT)
    public boolean shouldUseInteriorLight() {
        if (this.getModelSet().getConfig().interiorLights != null) {
            int x = MathHelper.floor_double(this.posX);
            int y = MathHelper.floor_double(this.posY + 0.5D);
            int z = MathHelper.floor_double(this.posZ);
            int v = NGTUtil.getLightValue(this.worldObj, x, y, z);
            return this.useInteriorLight() && v < 7;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected boolean useInteriorLight() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender(float par1) {
        if (this.shouldUseInteriorLight()) {
            return 0xF000F0;
        }
        return super.getBrightnessForRender(par1);
    }
}