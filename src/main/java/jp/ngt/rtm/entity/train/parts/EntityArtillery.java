package jp.ngt.rtm.entity.train.parts;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.entity.EntityBullet;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.item.ItemAmmunition;
import jp.ngt.rtm.item.ItemAmmunition.BulletType;
import jp.ngt.rtm.modelpack.cfg.FirearmConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetFirearm;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityArtillery extends EntityCargoWithModel<ModelSetFirearm> {
    private float barrelYaw;
    private float barrelPitch;

    public static final int MaxRecoilCount = 20;
    public int recoilCount;

    public EntityArtillery(World par1) {
        super(par1);
        this.setSize(3.0F, 2.5F);
    }

    public EntityArtillery(World par1, ItemStack itemStack, int x, int y, int z) {
        super(par1, itemStack, x, y, z);
    }

    public EntityArtillery(World par1, EntityVehicleBase par2, ItemStack par3, float[] par4Pos, byte id) {
        super(par1, par2, par3, par4Pos, id);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(28, -1);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.setBarrelYaw(nbt.getFloat("barrelYaw"));
        this.setBarrelPitch(nbt.getFloat("barrelPitch"));
        this.setAmmo(nbt.getInteger("ammo"));
    }

    @Override
    protected void readCargoFromNBT(NBTTagCompound nbt) {
        //yaw&pitchを上書きしないように
        String name = nbt.getString("ModelName");
        if (name.isEmpty()) {
            name = nbt.getString("name");//互換
        }
        this.setModelName(name);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setFloat("barrelYaw", this.getBarrelYaw());
        nbt.setFloat("barrelPitch", this.getBarrelPitch());
        nbt.setInteger("ammo", this.hasAmmo());
    }

    @Override
    protected void writeCargoToNBT(NBTTagCompound nbt) {
        nbt.setString("ModelName", this.getModelName());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.worldObj.isRemote) {
            if (this.recoilCount > 0) {
                --this.recoilCount;
            }
        }

        if (this.riddenByEntity != null) {
            float yaw = MathHelper.wrapAngleTo180_float(-this.riddenByEntity.rotationYaw - this.rotationYaw);
            this.setBarrelYaw(yaw);
            this.setBarrelPitch(this.riddenByEntity.rotationPitch);
        }
    }

    @Override
    public void updateRiderPosition() {
        if (this.riddenByEntity != null) {
            FirearmConfig cfg = this.getModelSet().getConfig();
            Vec3 v31 = Vec3.createVectorHelper(cfg.playerPos[0], cfg.playerPos[1], cfg.playerPos[2]);
            v31.rotateAroundX(NGTMath.toRadians(this.rotationPitch));
            v31.rotateAroundY(NGTMath.toRadians(this.rotationYaw));
            Vec3 v32 = v31.addVector(this.posX, this.posY, this.posZ);
            this.riddenByEntity.setPosition(v32.xCoord, v32.yCoord + this.riddenByEntity.getYOffset(), v32.zCoord);
        }
    }

    public void updateYawAndPitch(EntityPlayer player)//RTMTickHandlerClient
    {
        FirearmConfig cfg = this.getModelSet().getConfig();
        float speed = 0.0F;
        float riderYaw = MathHelper.wrapAngleTo180_float(-player.rotationYaw - this.rotationYaw);
        float yaw = this.getBarrelYaw();

        if (true)//Math.abs(riderYaw - yaw) >= 1.0F)
        {
            speed = cfg.rotationSpeedY;
            yaw += (riderYaw - yaw) * speed;

            if (yaw > cfg.yaw[0]) {
                yaw = cfg.yaw[0];
            }

            if (yaw < cfg.yaw[1]) {
                yaw = cfg.yaw[1];
            }

            player.rotationYaw = player.prevRotationYaw = -yaw - this.rotationYaw;
        }

        float riderPitch = player.rotationPitch;
        float pitch = this.getBarrelPitch();

        if (true)//Math.abs(riderPitch - pitch) >= 1.0F)
        {
            speed = cfg.rotationSpeedX;
            pitch += (riderPitch - pitch) * speed;

            if (pitch > -cfg.pitch[1]) {
                pitch = -cfg.pitch[1];
            }

            if (pitch < -cfg.pitch[0]) {
                pitch = -cfg.pitch[0];
            }

            player.rotationPitch = player.prevRotationPitch = pitch;
        }
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (super.interactFirst(player)) {
            return true;
        }

        if (this.riddenByEntity == null || !(this.riddenByEntity instanceof EntityPlayer) || this.riddenByEntity == player) {
            ItemStack itemstack = player.inventory.getCurrentItem();
            if (itemstack != null) {
                int type = this.getAmmoType(itemstack.getItemDamage());
                if (itemstack.getItem() instanceof ItemAmmunition && type >= 0) {
                    if (!this.worldObj.isRemote) {
                        if (this.hasAmmo() < 0) {
                            this.setAmmo(type);
                            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, Blocks.tnt.stepSound.func_150496_b(), (Blocks.tnt.stepSound.getVolume() + 1.0F) / 2.0F, Blocks.tnt.stepSound.getPitch() * 0.8F);
                            --itemstack.stackSize;
                        }
                    }
                    return true;
                } else if (itemstack.getItem() == RTMItem.paddle) {
                    if (!this.worldObj.isRemote) {
                        if (this.hasAmmo() >= 0) {
                            this.fire(player);
                        }
                    }
                    return true;
                }
            }

            if (!this.worldObj.isRemote) {
                player.mountEntity(this);
            }
        }
        return true;
    }

    public void onFireKeyDown(EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == RTMItem.bullet) {
                int type = this.getAmmoType(stack.getItemDamage());
                if (type >= 0) {
                    this.setAmmo(type);
                    if (!player.capabilities.isCreativeMode) {
                        --stack.stackSize;
                        if (stack.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(i, null);
                        }
                    }
                    this.fire(player);
                    return;
                }
            }
        }
    }

    /**
     * Server Only
     */
    private void fire(EntityPlayer player) {
        RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, "fire", this));

        FirearmConfig cfg = this.getModelSet().getConfig();
        Vec3 v30 = Vec3.createVectorHelper(cfg.muzzlePos[0] - cfg.modelPartsX.pos[0], cfg.muzzlePos[1] - cfg.modelPartsX.pos[1], cfg.muzzlePos[2] - cfg.modelPartsX.pos[2]);
        v30.rotateAroundX(NGTMath.toRadians(-this.getBarrelPitch()));
        v30 = v30.addVector(cfg.modelPartsX.pos[0] - cfg.modelPartsY.pos[0], cfg.modelPartsX.pos[1] - cfg.modelPartsY.pos[1], cfg.modelPartsX.pos[2] - cfg.modelPartsY.pos[2]);
        v30.rotateAroundY(NGTMath.toRadians(this.getBarrelYaw()));
        v30 = v30.addVector(cfg.modelPartsY.pos[0] - cfg.modelPartsN.pos[0], cfg.modelPartsY.pos[1] - cfg.modelPartsN.pos[1], cfg.modelPartsY.pos[2] - cfg.modelPartsN.pos[2]);
        //v30.rotateAroundX(NGTMath.toRadians(this.rotationPitch));
        v30.rotateAroundY(NGTMath.toRadians(this.rotationYaw));
        v30 = v30.addVector(this.posX, this.posY, this.posZ);

        double xRand;
        double yRand;
        double zRand;
        for (int i = 0; i < 8; ++i) {
            xRand = this.worldObj.rand.nextDouble() * 2.0D - 1.0D;
            yRand = this.worldObj.rand.nextDouble() * 2.0D - 1.0D;
            zRand = this.worldObj.rand.nextDouble() * 2.0D - 1.0D;
            if (this.worldObj.isRemote) {
                //this.worldObj.spawnParticle("largeexplode", v32.xCoord + xRand, v32.yCoord + yRand, v32.zCoord + zRand, 0.0D, 0.0D, 0.0D);
            } else {
                this.worldObj.spawnParticle("largeexplode", v30.xCoord + xRand, v30.yCoord + yRand, v30.zCoord + zRand, 0.0D, 0.0D, 0.0D);
                this.worldObj.playSoundEffect(v30.xCoord + xRand, v30.yCoord + yRand, v30.zCoord + zRand, "random.explode", 1.0F, 1.0F);
            }
        }

        Vec3 v31 = Vec3.createVectorHelper(cfg.modelPartsX.pos[0] - cfg.modelPartsY.pos[0], cfg.modelPartsX.pos[1] - cfg.modelPartsY.pos[1], cfg.modelPartsX.pos[2] - cfg.modelPartsY.pos[2]);
        v31.rotateAroundY(NGTMath.toRadians(this.getBarrelYaw()));
        v31 = v31.addVector(cfg.modelPartsY.pos[0] - cfg.modelPartsN.pos[0], cfg.modelPartsY.pos[1] - cfg.modelPartsN.pos[1], cfg.modelPartsY.pos[2] - cfg.modelPartsN.pos[2]);
        //v31.rotateAroundX(NGTMath.toRadians(this.rotationPitch));
        v31.rotateAroundY(NGTMath.toRadians(this.rotationYaw));
        v31 = v31.addVector(this.posX, this.posY, this.posZ);

        double mX = v30.xCoord - v31.xCoord;
        double mY = v30.yCoord - v31.yCoord;
        double mZ = v30.zCoord - v31.zCoord;
        BulletType type = BulletType.getBulletType(this.hasAmmo());
        EntityBullet entity = new EntityBullet(this.worldObj, type, v30.xCoord, v30.yCoord, v30.zCoord, mX, mY, mZ);
        this.worldObj.spawnEntityInWorld(entity);

        if (!player.capabilities.isCreativeMode) {
            int damage = this.hasAmmo() * 4 + 2;
            player.entityDropItem(new ItemStack(RTMItem.bullet, 1, damage), 1.0F);//薬莢ドロップ
        }

        this.setAmmo(-1);
    }

    public float getBarrelYaw() {
        return this.barrelYaw;
    }

    public void setBarrelYaw(float par1) {
        this.barrelYaw = par1;
    }

    public float getBarrelPitch() {
        return this.barrelPitch;
    }

    public void setBarrelPitch(float par1) {
        this.barrelPitch = par1;
    }

    @Override
    public String getModelType() {
        return "ModelFirearm";
    }

    @Override
    public void setModelName(String par1) {
        super.setModelName(par1);
        this.setBarrelYaw(0.0F);
        this.setBarrelPitch(0.0F);
    }

    public void setAmmo(int par1) {
        this.dataWatcher.updateObject(28, par1);
    }

    public int hasAmmo() {
        return this.dataWatcher.getWatchableObjectInt(28);
    }

    public int getAmmoType(int par1) {
        int i0 = par1 / 4;
        if (par1 % 4 == 0 && (i0 == 0 || i0 == 5)) {
            return i0;
        }
        return -1;
    }

    @Override
    public String getDefaultName() {
        return "40cmArtillery";
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(RTMItem.itemCargo, 1, 1);
    }
}