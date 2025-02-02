package jp.ngt.rtm.entity.train.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public abstract class EntityVehiclePart extends Entity {
    /**
     * 設置してあるならtrue, 列車の上にあるならfalse
     */
    protected boolean isIndependent;
    private EntityVehicleBase parent;
    private UUID unloadedParent;
    private boolean sorted;
    public boolean needsUpdatePos;

    private EntityLivingBase rider;

    public EntityVehiclePart(World par1) {
        super(par1);
    }

    public EntityVehiclePart(World par1, EntityVehicleBase par2, float[] par3Pos) {
        this(par1);
        this.setVehicle(par2);
        this.setPartPos(par3Pos[0], par3Pos[1], par3Pos[2]);
        this.updatePartPos(par2);
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(20, 0);//getTrain
        this.dataWatcher.addObject(21, 0F);//getVec
        this.dataWatcher.addObject(22, 0F);//getVec
        this.dataWatcher.addObject(23, 0F);//getVec
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Independent", this.isIndependent);
        Vec3 v3 = this.getPartVec();
        nbt.setFloat("vecX", (float) v3.getX());
        nbt.setFloat("vecY", (float) v3.getY());
        nbt.setFloat("vecZ", (float) v3.getZ());
        if (this.getVehicle() != null) {
            long l0 = 0L;
            long l1 = 0L;
            UUID uuid = this.getVehicle().getUniqueID();
            if (uuid != null) {
                l0 = uuid.getMostSignificantBits();
                l1 = uuid.getLeastSignificantBits();
            }
            nbt.setLong("trainUUID_Most", l0);
            nbt.setLong("trainUUID_Least", l1);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.isIndependent = nbt.getBoolean("Independent");
        float vX = nbt.getFloat("vecX");
        float vY = nbt.getFloat("vecY");
        float vZ = nbt.getFloat("vecZ");
        this.setPartPos(vX, vY, vZ);
        if (nbt.hasKey("trainUUID_Most", 4) && nbt.hasKey("trainUUID_Least", 4)) {
            long l0 = nbt.getLong("trainUUID_Most");
            long l1 = nbt.getLong("trainUUID_Least");
            if (l0 != 0L && l1 != 0L) {
                UUID uuid = new UUID(l0, l1);
                if (!this.loadTrainFromUUID(uuid)) {
                    this.unloadedParent = uuid;
                }
            }
        }
    }

    private boolean loadTrainFromUUID(UUID uuid) {
        List<Entity> list = this.worldObj.loadedEntityList;
        for (Entity entity : list) {
            if (uuid.equals(entity.getUniqueID()) && entity instanceof EntityVehicleBase) {
                this.setVehicle((EntityVehicleBase) entity);
                this.onLoadVehicle();
                return true;
            }
        }
        return false;
    }

    /**
     * EntityVehicleBaseへ自身の登録を行う。(セーブデータからの読み込み時のみ)
     */
    public abstract void onLoadVehicle();

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1) {
        if (par1 instanceof EntityVehiclePart || par1 instanceof EntityVehicleBase || par1 instanceof EntityBogie) {
            return null;
        }
        return par1.boundingBox;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    private void checkEntityOrder() {
        if (!this.sorted) {
            EntityVehicleBase vehicle = this.getVehicle();
            if (vehicle != null) {
                int myIndex = this.worldObj.loadedEntityList.indexOf(this);
                int vehicleIndex = this.worldObj.loadedEntityList.indexOf(vehicle);
                if (myIndex < vehicleIndex) {
                    this.worldObj.loadedEntityList.remove(this);
                    this.worldObj.loadedEntityList.add(this);
                }
            }
            this.sorted = true;
        }
    }

    @Override
    public void onUpdate() {
        if (this.worldObj.isRemote) {
            this.checkEntityOrder();
        } else {
            if (this.riddenByEntity != null) {
                if (this.rider == null && this.riddenByEntity instanceof EntityLivingBase) {
                    this.rider = (EntityLivingBase) this.riddenByEntity;
                }
            } else if (this.rider != null) {
                EntityVehicleBase.fixRiderPos(this.rider, this);
                this.rider = null;
            }
        }

        if (this.isIndependent) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
        } else {
            if (this.unloadedParent != null) {
                if (this.loadTrainFromUUID(this.unloadedParent)) {
                    this.unloadedParent = null;
                }
            }

            super.onUpdate();

            EntityVehicleBase vehicle = this.getVehicle();
            if (vehicle != null) {
                this.updatePartPos(vehicle);
            }
        }
    }

    /**
     * 位置を更新
     */
    public void updatePartPos(EntityVehicleBase vehicle) {
        Vec3 v3 = this.getPartVec();
        v3 = v3.rotateAroundZ(-vehicle.rotationRoll);
        v3 = v3.rotateAroundX(vehicle.rotationPitch);
        v3 = v3.rotateAroundY(vehicle.rotationYaw);
        this.setPosition(vehicle.posX + v3.getX(), vehicle.posY + v3.getY(), vehicle.posZ + v3.getZ());
        this.setRotation(vehicle.rotationYaw, vehicle.rotationPitch);
        this.needsUpdatePos = false;
    }

    @Override
    @SideOnly(Side.CLIENT)//NetClientHandler.handleEntity, par9は常に3
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
        if (this.getVehicle() == null || this.getVehicle().getSpeed() == 0.0F) {
            this.setPosition(par1, par3, par5);
            this.setRotation(par7, par8);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double par1, double par3, double par5) {
    }

    @Override
    public void moveEntity(double par1, double par3, double par5) {
    }

    @Override
    public void addVelocity(double par1, double par3, double par5) {
    }

    public void setVehicle(EntityVehicleBase vehicle) {
        this.dataWatcher.updateObject(20, vehicle.getEntityId());
    }

    public EntityVehicleBase getVehicle() {
        if (this.parent == null || this.parent.isDead) {
            Entity entity = this.worldObj.getEntityByID(this.dataWatcher.getWatchableObjectInt(20));
            if (entity instanceof EntityVehicleBase) {
                this.parent = (EntityVehicleBase) entity;
            }
        }
        return this.parent;
    }

    /**
     * EntityTrainとの相対位置を保存
     */
    public void setPartPos(float x, float y, float z) {
        this.dataWatcher.updateObject(21, x);
        this.dataWatcher.updateObject(22, y);
        this.dataWatcher.updateObject(23, z);
    }

    /**
     * EntityTrainとの相対位置を取得
     */
    public Vec3 getPartVec() {
        float x = this.dataWatcher.getWatchableObjectFloat(21);
        float y = this.dataWatcher.getWatchableObjectFloat(22);
        float z = this.dataWatcher.getWatchableObjectFloat(23);
        return PooledVec3.create(x, y, z);
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return this.parent == null ? null : this.parent.getPickedResult(target);
    }
}