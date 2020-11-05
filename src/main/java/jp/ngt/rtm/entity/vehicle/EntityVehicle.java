package jp.ngt.rtm.entity.vehicle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.cfg.VehicleConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityVehicle extends EntityVehicleBase<VehicleConfig> implements IModelSelectorWithType {
    protected double speed;
    public float vibration;

    private final List aabbList = new ArrayList();
    private boolean tracked;

    @SideOnly(Side.CLIENT)
    private int vehiclePosRotationInc;
    @SideOnly(Side.CLIENT)
    private double vehicleX, vehicleY, vehicleZ;
    @SideOnly(Side.CLIENT)
    private double vehicleYaw, vehiclePitch, vehicleRoll;

    public EntityVehicle(World world) {
        super(world);
        this.setSize(2.0F, 2.0F);

        if (world.isRemote) {
            this.seatRotation = -MAX_SEAT_ROTATION;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(21, 0.0F);
        this.dataWatcher.addObject(22, (byte) 0);
        this.dataWatcher.addObject(23, 0.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity.canBePushed() ? null : entity.boundingBox;
    }

    @Override
    public double getMountedYOffset() {
        return 0.0D;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
    }

    @Override
    public void onVehicleUpdate() {
        super.onVehicleUpdate();

        this.updateVibration();

        if (this.worldObj.isRemote) {
            //NGTLog.debug("" + this.getAccelerationForward());
        } else {
            //Packet送信しまくり
            //this.updateResourceState();//座席更新

            this.dataWatcher.updateObject(21, (float) this.speed);
            this.dataWatcher.updateObject(22, (byte) (this.onGround ? 1 : 0));
            this.setSpeed((float) this.speed);

            this.updateRotation();
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
    }

    @Override
    protected void updateFallState() {
        if (this.onGround) {
            this.motionY = 0.0D;
        } else {
            super.updateFallState();
        }
    }

    @Override
    protected void updateMovement() {

        if (this.shouldUpdateMotion()) {
            if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) this.riddenByEntity;
                this.updateMotion(living, living.moveStrafing, living.moveForward);
            }
        }

        super.updateMovement();
    }

    @Override
    protected void applyPhysicalEffect() {
        if (!this.shouldUpdateMotion()) {
            if (this.onGround) {
                this.speed *= this.getModelSet().getConfig().getFriction(this.onGround);
                this.motionX *= 0.9;
                this.motionY = 0.0D;
                this.motionZ *= 0.9;
            } else {
                this.speed *= 0.9999D;
                this.motionX *= 0.9900000095367432D;
                this.motionY *= 0.949999988079071D;
                this.motionZ *= 0.9900000095367432D;
            }
        }
    }

    protected void updateVibration() {
        double dxz = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (this.vibration > 0.0F) {
            this.vibration = 0.0F;
        } else {
            this.vibration = dxz > 0.0D ? 0.025F : 0.01F;
        }
    }


    protected boolean shouldUpdateMotion() {
        return this.onGround;
    }

    protected void updateMotion(EntityLivingBase entity, float moveStrafe, float moveForward) {
        VehicleConfig cfg = this.getModelSet().getConfig();

        this.speed += moveForward * cfg.getAcceleration(this.onGround);
        float maxSpeed = cfg.getMaxSpeed(this.onGround);
        float f0 = (moveStrafe + 0.02F) * cfg.getYawCoefficient(this.onGround);
        f0 *= cfg.changeYawOnStopping ? ((this.speed >= 0.0F) ? 1.0F : -1.0F) : (this.speed / maxSpeed);
        float maxYaw = cfg.getMaxYaw(this.onGround);
        if (f0 > maxYaw) {
            f0 = maxYaw;
        } else if (f0 < -maxYaw) {
            f0 = -maxYaw;
        }
        this.rotationYaw += f0;

        if (this.speed > maxSpeed) {
            this.speed = maxSpeed;
        } else if (this.speed < -maxSpeed) {
            this.speed = -maxSpeed;
        }

        Vec3 vec = this.getMotionVec();
        this.motionX = vec.xCoord;
        this.motionZ = vec.zCoord;
        if (moveForward == 0.0F) {
            this.speed *= cfg.getFriction(this.onGround);
        }

        if (Math.abs(this.speed) < 0.001D) {
            this.speed = 0.0D;
            this.motionX = this.motionZ = 0.0D;
        }
    }

    protected Vec3 getMotionVec() {
        Vec3 vec = Vec3.createVectorHelper(0.0D, 0.0D, this.speed);
        float maxSpeed = this.getModelSet().getConfig().getMaxSpeed(this.onGround);
        //滑り再現:1.0~0.0
        float f0 = (float) (1.0D - (this.speed / maxSpeed));
        float f1 = this.prevRotationYaw + (MathHelper.wrapAngleTo180_float(this.rotationYaw - this.prevRotationYaw) * f0);
        float yaw2 = (this.onGround || this.inWater) ? f1 : this.rotationYaw;
        vec.rotateAroundY(NGTMath.toRadians(yaw2));
        return vec;
    }

    protected void updateRotation() {
        if (this.onGround) {
            if (this.motionX != 0.0D || this.motionZ != 0.0D) {
                double hFront = this.getBlockHeight(this.rotationYaw);
                double hBack = this.getBlockHeight(this.rotationYaw + 180.0F);
                double hLeft = this.getBlockHeight(this.rotationYaw + 90.0F);
                double hRight = this.getBlockHeight(this.rotationYaw - 90.0F);
                float yawFB = (float) NGTMath.toDegrees(Math.atan2(hFront - hBack, this.width));
                float yawLR = (float) NGTMath.toDegrees(Math.atan2(hLeft - hRight, this.width));
                this.rotationPitch = yawFB;
                this.rotationRoll = yawLR;
            } else {
                this.rotationPitch *= 0.75F;
                this.rotationRoll *= 0.75F;
            }
        } else {
            this.rotationPitch *= 0.75F;
            this.rotationRoll *= 0.75F;
        }

        if (Math.abs(this.rotationPitch) < 0.01F) {
            this.rotationPitch = 0.0F;
        }

        if (Math.abs(this.rotationRoll) < 0.01F) {
            this.rotationRoll = 0.0F;
        }
    }

    private double getBlockHeight(float yaw) {
        float rad = NGTMath.toRadians(yaw);
        double r = (double) this.width * 0.5D;
        double x = (double) MathHelper.sin(rad) * r + this.posX;
        double z = (double) MathHelper.cos(rad) * r + this.posZ;
        int blockX = MathHelper.floor_double(x);
        int blockZ = MathHelper.floor_double(z);
        //int blockY = this.worldObj.getHeightValue(blockX, blockZ) - 1;
        int blockY = MathHelper.floor_double(this.posY) + 8;
        for (int i = 0; !this.worldObj.isSideSolid(blockX, blockY, blockZ, ForgeDirection.UP) && i < 16; ++i) {
            --blockY;
        }
        AxisAlignedBB aabb = this.worldObj.getBlock(blockX, blockY, blockZ).getCollisionBoundingBoxFromPool(this.worldObj, blockX, blockY, blockZ);
        return aabb != null ? aabb.maxY - this.posY : 0.0D;
    }

    @Override
    public void updateRiderPosition() {
        if (this.riddenByEntity != null) {
            ModelSetVehicleBase<VehicleConfig> set = this.getModelSet();
            float[] pos = set.getConfig().getPlayerPos()[0];
            double d0 = this.vibration + this.riddenByEntity.getYOffset();
            Vec3 vec = Vec3.createVectorHelper(pos[0], (double) pos[1] + d0, pos[2]);
            vec.rotateAroundZ(NGTMath.toRadians(-this.rotationRoll));
            vec.rotateAroundX(NGTMath.toRadians(this.rotationPitch));
            vec.rotateAroundY(NGTMath.toRadians(this.rotationYaw));
            double x = this.posX + vec.xCoord;
            double y = this.posY + vec.yCoord;
            double z = this.posZ + vec.zCoord;
            this.riddenByEntity.setPosition(x, y, z);
        }
    }

    @Override
    protected void updateFallState(double fallDistance, boolean par2) {
        super.updateFallState(fallDistance, par2);
    }

    @Override
    protected void fall(float par1) {
    }

    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int par6) {
        this.vehiclePosRotationInc = par6;
        this.vehicleX = x;
        this.vehicleY = y;
        this.vehicleZ = z;
        this.vehicleYaw = yaw;
        this.vehiclePitch = pitch;
    }

    @SideOnly(Side.CLIENT)
    public void setRoll(float par1) {
        this.vehicleRoll = par1;
    }

    public void setUpDown(int par1) {
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (player.isSneaking()) {
            if (this.worldObj.isRemote) {
                player.openGui(RTMCore.instance, RTMCore.guiIdSelectEntityModel, player.worldObj, this.getEntityId(), 0, 0);
            }
            return true;
        }

        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != player) {
            return true;
        } else {
            if (!this.worldObj.isRemote) {
                player.mountEntity(this);
            }
            return true;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float par2) {
        if (!this.worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getEntity();
            if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_VEHICLE)) {
                this.setDead();

                if (!player.capabilities.isCreativeMode) {
                    this.entityDropItem(this.getVehicleItem(), 0.5F);
                }
            }
        }
        return true;
    }

    protected abstract ItemStack getVehicleItem();

    @Override
    public void applyEntityCollision(Entity entity) {
        if (!this.worldObj.isRemote) {
            if (entity != this.riddenByEntity) {
                if (entity instanceof EntityLivingBase) {
                    double dxz = this.motionX * this.motionX + this.motionZ * this.motionZ;
                    if (dxz > 0.0D) {
                        float strength = (float) (dxz / this.getModelSet().getConfig().getMaxSpeed(this.onGround));
                        if (strength > 0.5F) {
                            entity.attackEntityFrom(DamageSource.causeThornsDamage(this), strength);
                        }
                    }
                }
            }
        }
    }

    @Override
    public float getSpeed() {
        return this.dataWatcher.getWatchableObjectFloat(23);
    }

    public void setSpeed(float par1) {
        this.dataWatcher.updateObject(23, par1);
    }

    @Override
    public String getModelType() {
        return "ModelVehicle";
    }

    @Override
    public String getSubType() {
        return this.getModelSet().getConfig().getSubType();
    }

    @Override
    protected void onModelChanged() {
        super.onModelChanged();

        VehicleConfig cfg = this.getModelSet().getConfig();
        this.setSize(cfg.getSize()[0], cfg.getSize()[1]);
    }
}