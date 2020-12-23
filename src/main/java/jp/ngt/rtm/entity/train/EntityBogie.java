package jp.ngt.rtm.entity.train;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.protection.Lockable;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.ngtlib.world.NGTWorld;
import jp.ngt.rtm.RTMAchievement;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.entity.EntityBumpingPost;
import jp.ngt.rtm.entity.train.parts.EntityVehiclePart;
import jp.ngt.rtm.entity.train.util.BogieController;
import jp.ngt.rtm.entity.vehicle.VehicleTrackerEntry;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class EntityBogie extends Entity implements Lockable {
    private static final ResourceLocation JOINT_SOUND = new ResourceLocation("rtm", "train.joint");
    private static final ResourceLocation JOINT_SOUND_REVERB = new ResourceLocation("rtm", "train.joint_reverb");
    /**
     * ベジェ曲線の分割精度(1m当たり)
     */
    private static final byte SPLIT_VALUE = 32;//64だとカーブでゆっくりになる

    private static final byte DW_TrainId = 20;
    private static final byte DW_IsFront = 21;
    private static final byte DW_BogieId = 22;

    private EntityTrainBase parentTrain;
    /**
     * 連結可能かどうか
     */
    public boolean isActivated = false;
    /**
     * 走る向き（≠ rotationYaw）
     */
    public float movingYaw;
    private UUID unloadedTrain;
    /**
     * BogieTrackerEntryを生成したかどうか
     */
    private boolean tracked;

    private TileEntityLargeRailCore currentRailObj;
    private RailMap currentRailMap;
    /**
     * {x, y, z}
     */
    private final double[] posBuf = new double[3];
    /**
     * {yaw, pitch, yaw2}
     */
    private final float[] rotationBuf = new float[4];
    private int split = -1;
    private int prevPosIndex;
    //private int point = -1;
    private float jointDelay;
    private boolean reverbSound;
    private int jointIndex;

    public float rotationRoll;
    public float prevRotationRoll;

    @SideOnly(Side.CLIENT)
    private int carPosRotationInc;
    @SideOnly(Side.CLIENT)
    private float carYaw, carPitch, carRoll;

    public EntityBogie(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.preventEntitySpawning = true;
        this.setBogieSize(EntityTrainBase.TRAIN_WIDTH, EntityTrainBase.TRAIN_HEIGHT);
        this.yOffset = EntityTrainBase.TRAIN_HEIGHT;
    }

    public EntityBogie(World world, byte id) {
        this(world);
        this.setBogieId(id);
    }

    /**
     * モデル変更時にEntityTrainから
     */
    public void setBogieSize(float width, float height) {
        this.setSize(width, height);
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_TrainId, 0);//getTrain
        this.dataWatcher.addObject(DW_IsFront, (byte) 0);//isFront
        this.dataWatcher.addObject(DW_BogieId, (byte) 0);//bogieId
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("isFront", this.isFront());
        nbt.setByte("bogieId", this.getBogieId());
        if (this.getTrain() != null) {
            UUID uuid = this.getTrain().getUniqueID();
            if (uuid != null) {
                long l0 = uuid.getMostSignificantBits();
                long l1 = uuid.getLeastSignificantBits();
                nbt.setLong("trainUUID_Most", l0);
                nbt.setLong("trainUUID_Least", l1);
            }
        } else {
            NGTLog.debug("Can't write connected train to nbt. X:" + this.posX + "Z:" + this.posZ);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.setFront(nbt.getBoolean("isFront"));
        this.setBogieId(nbt.getByte("bogieId"));
        if (nbt.hasKey("trainUUID_Most", 4) && nbt.hasKey("trainUUID_Least", 4)) {
            long l0 = nbt.getLong("trainUUID_Most");
            long l1 = nbt.getLong("trainUUID_Least");
            UUID uuid = new UUID(l0, l1);
            if (!this.loadTrainFromUUID(uuid)) {
                this.unloadedTrain = uuid;
            }
        } else {
            NGTLog.debug("doesn't have train UUID");
        }
    }

    private boolean loadTrainFromUUID(UUID uuid) {
        for (int j = 0; j < this.worldObj.loadedEntityList.size(); ++j) {
            Entity entity = (Entity) this.worldObj.loadedEntityList.get(j);
            if (uuid.equals(entity.getUniqueID()) && entity instanceof EntityTrainBase) {
                this.setTrain((EntityTrainBase) entity);
                ((EntityTrainBase) entity).setBogie(this.getBogieId(), this);
                return true;
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1)//World.getCollidingBoundingBoxes()
    {
        return (par1 instanceof EntityVehiclePart) ? null : par1.getBoundingBox();
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

    /**
     * @return 位置更新が成功したらtrue
     */
    public boolean updateBogiePos(float speed, float trainLength, EntityBogie frontBogie) {
        if (this.updateCollision()) {
            return false;
        }

        this.movingYaw = MathHelper.wrapAngleTo180_float(this.rotationYaw + (this.isFront() ? 0.0F : 180.0F));

        double px = this.posX + (double) NGTMath.sin(this.movingYaw) * (double) speed;
        double py = this.posY;
        double pz = this.posZ + (double) NGTMath.cos(this.movingYaw) * (double) speed;

        if (!this.resetRailObj(px, py, pz)) {
            this.getTrain().stopTrain(true);
            return false;
            //慣性運動実装予定
        }

        RailMap rm = this.currentRailMap;
        int pIndex = 0;
        if (frontBogie == null || this.prevPosIndex == -1) {
            pIndex = rm.getNearlestPoint(this.split, px, pz);
        } else {
            //移動範囲を制限して、「台車からの距離は同じでも位置は真逆」な点の検出を防ぐ
            int indexInc = (int) ((speed + 0.25F) * (float) SPLIT_VALUE);//前回位置からどの程度進むか
            int indexNeg = this.prevPosIndex - indexInc;
            int indexPos = this.prevPosIndex + indexInc;
            int indexMin = Math.max(indexNeg, 0);
            int indexMax = Math.min(indexPos, this.split);
            double[] fp = frontBogie.getPosBuf();
            double dif = Double.MAX_VALUE;
            double tlSq = trainLength * trainLength;
            for (int i = indexMin; i < indexMax; ++i) {
                double[] pxz = rm.getRailPos(this.split, i);
                //先頭台車はまだ位置更新されてないので、bogie.getDistanceSq()は使わない
                double lenTemp = this.getDistanceSq(pxz[1], py, pxz[0], fp[0], fp[1], fp[2]);
                double difTemp = Math.abs(lenTemp - tlSq);
                if (difTemp < dif) {
                    dif = difTemp;
                    pIndex = i;//理想の台車間距離と実際の距離の差が最も小さくなる点を求める
                }
            }
        }
        this.prevPosIndex = pIndex;

        double[] posZX = rm.getRailPos(this.split, pIndex);
        py = rm.getRailHeight(this.split, pIndex) + this.yOffset;
        float railYaw = MathHelper.wrapAngleTo180_float(rm.getRailRotation(this.split, pIndex));
        float movYaw = EntityBogie.fixBogieYaw(this.movingYaw, railYaw);
        float yaw = EntityBogie.fixBogieYaw(this.rotationYaw, movYaw);
        float pitch = EntityBogie.fixBogiePitch(rm.getRailPitch(this.split, pIndex), railYaw, yaw);
        float cant = rm.getCant(this.split, pIndex);

        if (Math.abs(MathHelper.wrapAngleTo180_float(railYaw - yaw)) > 90.0F) {
            cant *= -1.0F;
        }

        if ((this.posX == posZX[1]) && (this.posZ == posZX[0])) {
            return true;
        }//低速時に斜めレールで走行しない問題回避

        this.posBuf[0] = posZX[1];
        this.posBuf[1] = py;
        this.posBuf[2] = posZX[0];
        this.rotationBuf[0] = yaw;
        this.rotationBuf[1] = pitch;
        this.rotationBuf[2] = movYaw;
        this.rotationBuf[3] = cant;


        if (this.jointDelay > 0.0F) {
            this.jointDelay -= speed;
            if (this.jointDelay <= 0.0F) {
                this.playJointSound();
            }
        }

        return true;
    }

    private double getDistanceSq(double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @return 移動先にレールがあればtrue
     */
    private boolean resetRailObj(double px, double py, double pz) {
        TileEntityLargeRailCore coreObj = this.getRail(px, py, pz);

        if (coreObj == null) {
            return false;
        } else {
            if (this.currentRailObj == coreObj)//前回と同じレール上にいる
            {
            } else//新しいレール上に移動
            {
                this.currentRailObj = coreObj;
                if (coreObj instanceof TileEntityLargeRailSwitchCore) {
                    TileEntityLargeRailSwitchCore switchObj = (TileEntityLargeRailSwitchCore) coreObj;
                    this.currentRailMap = switchObj.getSwitch().getNearestPoint(this).getActiveRailMap(this.worldObj);
                } else {
                    this.currentRailMap = coreObj.getRailMap(this);
                }
                this.split = (int) (this.currentRailMap.getLength() * (double) SPLIT_VALUE);
                this.prevPosIndex = -1;
                this.onChangeRail(px, py, pz);
            }

            return true;
        }
    }

    /**
     * 別レールに移動した際呼び出し
     */
    protected void onChangeRail(double px, double py, double pz) {
        TileEntityLargeRailBase railObj = TileEntityLargeRailBase.getRailFromCoordinates(this.worldObj, px, py, pz);
        this.reverbSound = railObj.isReberbSound();
        TrainConfig cfg = this.getTrain().getModelSet().getConfig();
        if (!cfg.muteJointSound) {
            this.jointIndex = 0;
            this.playJointSound();
        }
    }

    protected boolean reverseJointArray() {
        boolean b0 = this.getTrain().getTrainDirection() == 0;
        boolean b1 = this.getBogieId() == 0;
        return b0 ^ b1;
    }

    protected void playJointSound() {
        EntityTrainBase train = this.getTrain();
        TrainConfig cfg = train.getModelSet().getConfig();
        if (!cfg.muteJointSound) {
            float pitch = (train.getSpeed() / cfg.maxSpeed[cfg.maxSpeed.length - 1]) * 0.5F + 1.0F;
            ResourceLocation sound = this.reverbSound ? JOINT_SOUND_REVERB : JOINT_SOUND;
            RTMCore.proxy.playSound(this, sound, 1.0F, pitch);

            int size = cfg.jointDelay[this.getBogieId()].length;
            if (this.jointIndex < size - 1) {
                //現在の正しいIndex
                int index0 = this.reverseJointArray() ? size - this.jointIndex - 1 : this.jointIndex;
                ++this.jointIndex;
                int index1 = this.reverseJointArray() ? size - this.jointIndex - 1 : this.jointIndex;
                float fcur = cfg.jointDelay[this.getBogieId()][index0];
                float fnex = cfg.jointDelay[this.getBogieId()][index1];
                this.jointDelay = Math.abs(fnex - fcur);
            }
        }
    }

    private TileEntityLargeRailCore getRail(double px, double py, double pz) {
        TileEntityLargeRailBase railObj = TileEntityLargeRailBase.getRailFromCoordinates(this.worldObj, px, py, pz);
        if (railObj == null) {
            this.errorLog(px, pz, "Rail not found > x:%s z:%s");
            return null;
        }

        TileEntityLargeRailCore coreObj = railObj.getRailCore();
        if (coreObj == null) {
            this.errorLog(px, pz, "Illegal rail > x:%s z:%s");
            return null;
        }

        return coreObj;
    }

    private void errorLog(double px, double pz, String msg) {
        if (this.getTrain().riddenByEntity instanceof EntityPlayer) {
            int x = MathHelper.floor_double(px);
            int z = MathHelper.floor_double(pz);
            NGTLog.sendChatMessage((EntityPlayer) this.getTrain().riddenByEntity, msg, x, z);
        }
    }

    public double[] getPosBuf() {
        return this.posBuf;
    }

    public void moveBogie(EntityTrainBase train, double x, double y, double z, BogieController.UpdateFlag flag) {
        this.setPosition(x, y, z);
        switch (flag) {
            case ALL:
                this.setRotation(this.rotationBuf[0], this.rotationBuf[1]);
                this.movingYaw = this.rotationBuf[2];
                this.rotationRoll = this.rotationBuf[3];
                break;
            case YAW:
                float movYaw = EntityBogie.fixBogieYaw(this.movingYaw, train.rotationYaw);
                float yaw = EntityBogie.fixBogieYaw(this.rotationYaw, movYaw);
                this.rotationYaw = yaw % 360.0F;
                this.movingYaw = movYaw;
                break;
            case NONE:
                break;
        }
    }

    public void updateBogie() {
        super.onUpdate();

        this.prevRotationRoll = this.rotationRoll;

        if (this.worldObj.isRemote) {
            this.updatePosAndRotationClient();

            this.func_145775_I();//レールパーティクル用
        }
    }

//	@SideOnly(Side.CLIENT)
//	protected void updatePosAndRotationClient() {
//		this.prevPosX = this.posX;
//		this.prevPosY = this.posY;
//		this.prevPosZ = this.posZ;
//
//		double x = this.posX, y = this.posY, z = this.posZ;
//
//		if (this.carPosRotationInc > 0) {
//			float d0 = 1.0F / (float) this.carPosRotationInc;
//			x = this.posX + (this.carX - this.posX) * d0;
//			y = this.posY + (this.carY - this.posY) * d0;
//			z = this.posZ + (this.carZ - this.posZ) * d0;
//			this.rotationYaw += MathHelper.wrapAngleTo180_float((float) (this.carYaw - (double) this.rotationYaw)) * d0;
//			this.rotationPitch += (this.carPitch - this.rotationPitch) * d0;
//			this.rotationRoll += (this.carRoll - this.rotationRoll) * d0;
//			--this.carPosRotationInc;
//		}
//
//		EntityTrainBase train = this.getTrain();
//		if (train != null) {
//			float[][] pos = train.getModelSet().getConfig().getBogiePos();
//			int bogieIndex = this.getBogieId();
//			Vec3 v31 = Vec3.createVectorHelper(pos[bogieIndex][0], pos[bogieIndex][1], pos[bogieIndex][2]);
//			v31.rotateAroundX(NGTMath.toRadians(train.rotationPitch));
//			v31.rotateAroundY(NGTMath.toRadians(train.rotationYaw));
//			x = train.posX + v31.xCoord;
//			y = train.posY + v31.yCoord;
//			z = train.posZ + v31.zCoord;
//		}
//		this.setPosition(x, y, z);
//	}

    @SideOnly(Side.CLIENT)
    protected void updatePosAndRotationClient() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.carPosRotationInc > 0) {
            float d0 = 1.0F / (float) this.carPosRotationInc;
            this.rotationYaw += MathHelper.wrapAngleTo180_float((float) (this.carYaw - (double) this.rotationYaw)) * d0;
            this.rotationPitch += (this.carPitch - (double) this.rotationPitch) * d0;
            this.rotationRoll += (this.carRoll - (double) this.rotationRoll) * d0;
            --this.carPosRotationInc;
        }
        EntityTrainBase train = this.getTrain();
        if (train != null) {
            //Clientでの台車位置決定は車両位置から
            float[][] pos = train.getModelSet().getConfig().getBogiePos();
            int bogieIndex = this.getBogieId();
            Vec3 v31 = new Vec3(pos[bogieIndex][0], pos[bogieIndex][1], pos[bogieIndex][2]);
            v31 = v31.rotateAroundX(train.rotationPitch);
            v31 = v31.rotateAroundY(train.rotationYaw);
            double newX = train.posX + v31.getX();
            double newY = train.posY + v31.getY();
            double newZ = train.posZ + v31.getZ();
            //位置補正はRendererでやる
            this.setPosition(newX, newY, newZ);
        }
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    @Override
    public void onUpdate() {
        this.checkUnloadTrain();

        if (!this.worldObj.isRemote) {
            if (!this.tracked) {
                this.tracked = VehicleTrackerEntry.trackingVehicle(this);
            }
        }
    }

    @Override
    public void setDead() {
        super.setDead();
    }

    private void checkUnloadTrain() {
        if (this.unloadedTrain != null) {
            if (!this.worldObj.isRemote || this.worldObj instanceof NGTWorld)//ミニチュアで台車が?になるの防止
            {
                if (this.loadTrainFromUUID(this.unloadedTrain)) {
                    this.unloadedTrain = null;
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1, float par2) {
        if (this.getTrain() == null || this.getTrain().isDead) {
            if (!this.worldObj.isRemote) {
                this.setDead();
            }
            return true;
        }
        return this.getTrain().attackEntityFrom(par1, par2);
    }

    /**
     * @return 衝突して止まるならtrue
     */
    private boolean updateCollision() {
        double dis = this.width * 2.0F;
        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                AxisAlignedBB.getBoundingBox(
                        this.posX - dis, this.posY - EntityTrainBase.TRAIN_HEIGHT, this.posZ - dis,
                        this.posX + dis, this.posY + EntityTrainBase.TRAIN_HEIGHT, this.posZ + dis));
        return this.collideWithEntities(list);
    }

    /**
     * 連結・モブとの衝突判定<br>
     * getTrain() != null は確認済みであること<br>
     * Server Only
     *
     * @return 衝突して止まるならtrue
     */
    private boolean collideWithEntities(List<Entity> list) {
        EntityTrainBase train = this.getTrain();
        EntityBogie nearestBogie = null;
        double distanceSq = Double.MAX_VALUE;

        for (Entity entity : list) {
            if (entity instanceof EntityBogie) {
                if (this.collideWithBogie((EntityBogie) entity)) {
                    //一番近い台車と連結する
                    double d0 = this.getDistanceSqToEntity(entity);
                    if (d0 < distanceSq) {
                        nearestBogie = (EntityBogie) entity;
                        distanceSq = d0;
                    }
                }
            } else if (entity instanceof EntityBumpingPost) {
                this.collideWithBumpingPost((EntityBumpingPost) entity);
            } else if (entity instanceof EntityLivingBase) {
                this.collideWithLiving((EntityLivingBase) entity);
            }
        }

        if (nearestBogie != null) {
            this.connectBogie(nearestBogie);
            return true;
        }

        return false;
    }

    /**
     * @return 連結可能か
     */
    private boolean collideWithBogie(EntityBogie bogie) {
        EntityTrainBase train = this.getTrain();
        EntityTrainBase train2 = bogie.getTrain();

        if (train2 != null && !train.equals(train2)) {
            //連結してるのは無視
            if (train.getFormation() != null && train.getFormation().containBogie(bogie)) {
                return false;
            }

            //範囲外
            if (!train.inConnectableRange(train2)) {
                return false;
            }

            //equalsはぬるぽ出る
            //if((train.getConnectedTrain(this.bogieId) == train2)){continue;}

            //違う線路上なら無視
            RailMap rm0 = TileEntityLargeRailBase.getRailMapFromCoordinates(this.worldObj, this, this.posX, this.posY, this.posZ);
            RailMap rm1 = TileEntityLargeRailBase.getRailMapFromCoordinates(this.worldObj, bogie, bogie.posX, bogie.posY, bogie.posZ);
            if (!(rm0 != null && rm0.canConnect(rm1))) {
                return false;
            }

            //連結可能
            if (this.isActivated || bogie.isActivated) {
                return true;
            }

            //衝突処理
            float speed0 = train.getSpeed();
            float speed1 = train2.getSpeed();
            boolean b0 = train.getTrainDirection() == this.getBogieId();
            boolean b1 = train2.getTrainDirection() == bogie.getBogieId();

            float sp0 = b0 ? speed0 : -speed0;
            float sp1 = b1 ? -speed1 : speed1;
            if (sp0 - sp1 >= 0.0F) {
                train2.setSpeed(b0 ^ b1 ? speed0 : -speed0);
                train.setSpeed(b0 ^ b1 ? speed1 : -speed1);
            }
        }

        return false;
    }

    /**
     * 連結処理
     */
    private void connectBogie(EntityBogie bogie) {
        this.getTrain().connectTrain(this, bogie);
        this.isActivated = false;
        bogie.isActivated = false;
    }

    private boolean collideWithBumpingPost(EntityBumpingPost entity) {
        EntityTrainBase train = this.getTrain();
        //this.widthを使うと短い車両は止まらない
        double dis = NGTMath.pow((EntityTrainBase.TRAIN_WIDTH / 2.0F) + 0.375F, 2);
        double dsq = this.getDistanceSqToEntity(entity);
        if (train.getTrainDirection() == this.getBogieId() && dsq <= dis) {
            train.stopTrain(true);
            if (train.riddenByEntity instanceof EntityPlayer) {
                ((EntityPlayer) train.riddenByEntity).addStat(RTMAchievement.accidentsWillHappen, 1);
            }
        }
        return false;
    }

    private boolean collideWithLiving(EntityLivingBase entity) {
        EntityTrainBase train = this.getTrain();

        if (!entity.equals(train.riddenByEntity) && !entity.isRiding()) {
            float speed = train.getSpeed();
            double dis = NGTMath.pow((this.width / 2.0F) + 0.375F, 2);

            if (speed > 0.0F && this.getDistanceSqToEntity(entity) < dis)//9.765D
            {
                double d2 = entity.posX - this.posX;
                double d3 = entity.posZ - this.posZ;
                double d4 = (d2 * d2 + d3 * d3);
                entity.addVelocity(d2 / d4 * 10.0D, 0.3D, d3 / d4 * 10.0D);
                DamageSource dmgSource = new EntityDamageSource("train", train);
                float damage = speed * 7.2F;//*0.1*72
                entity.attackEntityFrom(dmgSource, damage);
                if (train.riddenByEntity instanceof EntityPlayer) {
                    ((EntityPlayer) train.riddenByEntity).addStat(RTMAchievement.accidentsWillHappen, 1);
                }
            }
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.carPosRotationInc = par9;
        this.carYaw = par7;
        this.carPitch = par8;
    }

    @SideOnly(Side.CLIENT)
    public void setRoll(float par1) {
        this.carRoll = par1;
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

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (this.getTrain() != null) {
            if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.DRIVE_TRAIN)) {
                return this.getTrain().interactTrain(this, player);
            }
        } else if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == RTMItem.paddle) {
            //Debug
            if (this.unloadedTrain == null) {
                NGTLog.sendChatMessage(player, "UUID:null(bogie, not found train)");
            } else {
                NGTLog.sendChatMessage(player, "UUID:" + this.unloadedTrain.toString() + "(bogie, not found train)");
            }
            return true;
        }
        return true;
    }

    public void setTrain(EntityTrainBase train) {
        this.dataWatcher.updateObject(DW_TrainId, train.getEntityId());
    }

    public EntityTrainBase getTrain() {
        if (this.parentTrain == null) {
            Entity entity = this.worldObj.getEntityByID(this.dataWatcher.getWatchableObjectInt(DW_TrainId));
            if (entity instanceof EntityTrainBase) {
                this.parentTrain = (EntityTrainBase) entity;
            }
        }
        return this.parentTrain;
    }

    public void setFront(boolean par1) {
        byte b = par1 ? (byte) 1 : (byte) 0;
        this.dataWatcher.updateObject(DW_IsFront, b);
    }

    public boolean isFront() {
        byte b = this.dataWatcher.getWatchableObjectByte(DW_IsFront);
        return b == 1;
    }

    public byte getBogieId() {
        return this.dataWatcher.getWatchableObjectByte(DW_BogieId);
    }

    public void setBogieId(byte par1) {
        this.dataWatcher.updateObject(DW_BogieId, par1);
    }

    /**
     * @param yaw1 : 元の向き
     * @param yaw2 : 新しい向き
     */
    public static float fixBogieYaw(float yaw1, float yaw2) {
        float f0 = Math.abs(yaw1 - yaw2);
        f0 = f0 > 180.0F ? 360.0F - f0 : f0;
        return MathHelper.wrapAngleTo180_float(f0 > 90.0F ? yaw2 + 180.0F : yaw2);
    }

    public static float fixBogiePitch(float railPitch, float railYaw, float bogieYaw) {
        return MathHelper.wrapAngleTo180_float(Math.abs(bogieYaw - railYaw) > 45.0F ? -railPitch : railPitch);
    }

    @Override
    public Object getTarget(World world, int x, int y, int z) {
        return this.getTrain();
    }

    @Override
    public boolean lock(EntityPlayer player, String code) {
        return true;
    }

    @Override
    public boolean unlock(EntityPlayer player, String code) {
        return true;
    }

    @Override
    public int getProhibitedAction() {
        return 1;
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return this.parentTrain.getPickedResult(target);
    }
}