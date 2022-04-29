package jp.ngt.rtm.entity.npc;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.entity.EntityBullet;
import jp.ngt.rtm.item.ItemGun;
import jp.ngt.rtm.item.ItemGun.GunType;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetNPC;
import jp.ngt.rtm.modelpack.state.ResourceState;
import jp.ngt.rtm.util.PathNavigateCustom;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityNPC extends EntityTameable implements IModelSelector, IRangedAttackMob {
    public static final float SPEED = 0.45F;
    public static final float ATTACK_POWER = 1.0F;

    private final ResourceState state = new ResourceState(this);
    private ModelSetNPC myModelSet;
    private Role myRole = Role.MANNEQUIN;
    private final EntityDummyPlayer playerDummy;

    protected int useItemCount;

    public InventoryNPC inventory = new InventoryNPC(this);

    public EntityNPC(World world) {
        super(world);
        this.setSize(0.6F, 1.8F);
        this.setNavigator(new PathNavigateCustom(this, world));
        this.getNavigator().setAvoidsWater(true);
        this.playerDummy = new EntityDummyPlayer(world, this);
    }

    public EntityNPC(World world, EntityPlayer player) {
        this(world);
        this.func_152115_b(player.getUniqueID().toString());
    }

    protected void setNavigator(PathNavigate navigator) {
        NGTUtil.setValueToField(EntityLiving.class, this, navigator, "navigator", "field_70699_by");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(SPEED);
        //EntityMobじゃないので新たに追加
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(ATTACK_POWER);
    }

    @Override
    public void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(20, "MannequinNGT01");
        this.dataWatcher.addObject(21, (byte) 0);
        this.dataWatcher.addObject(22, "");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        nbt.setString("ModelName", this.getModelName());
        nbt.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        nbt.setTag("State", this.getResourceState().writeToNBT());
        nbt.setString("menu", this.getMenu());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        this.setModelName(nbt.getString("ModelName"));
        NBTTagList nbttaglist = nbt.getTagList("Inventory", 10);
        this.inventory.readFromNBT(nbttaglist);
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
        this.setMenu(nbt.getString("menu"));

        this.onInventoryChanged();
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entity) {
        return null;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 0;
    }

    @Override
    protected Item getDropItem() {
        return RTMItem.itemMotorman;
    }

    @Override
    protected void dropFewItems(boolean par1, int par2) {
    }

    protected void dropEntity() {
        int damage = this instanceof EntityMotorman ? 0 : 1;
        this.entityDropItem(new ItemStack(this.getDropItem(), 1, damage), 0.5F);
    }

    @Override
    public void onUpdate() {
        //インベントリ開いてる間は止まらせる
        if (this.inventory.isOpening) {
            return;
        }

        super.onUpdate();

        this.playerDummy.setPosition(this.posX, this.posY, this.posZ);
        this.playerDummy.rotationYaw = this.rotationYaw;
        this.playerDummy.rotationPitch = this.rotationPitch;

        if (this.isUsingItem()) {
            ItemStack item = this.getHeldItem();
            boolean hasGun = (item != null && item.getItem() instanceof ItemGun);

            if (!hasGun || this.useItemCount > item.getMaxItemUseDuration()) {
                if (!this.worldObj.isRemote) {
                    if (hasGun) {
                        item.onPlayerStoppedUsing(this.worldObj, this.playerDummy, this.useItemCount);
                    }
                    this.setUseItem(false);
                }
                this.useItemCount = 0;
            } else {
                if (!this.worldObj.isRemote) {
                    item.getItem().onUsingTick(item, this.playerDummy, this.useItemCount);
                }
            }

            ++this.useItemCount;
        } else {
            this.useItemCount = 0;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.worldObj.isRemote) {
            this.healNPC();
        }
    }

    @Override
    protected void updateEntityActionState() {
        if (this.myRole != Role.MANNEQUIN) {
            super.updateEntityActionState();
        }
    }

    public Role getRole() {
        return this.myRole;
    }

    protected void healNPC() {
        if (this.ticksExisted % 3 == 0 && this.getHealth() < this.getMaxHealth()) {
            int index = this.inventory.hasItem(ItemFood.class);
            if (index >= 0) {
                ItemStack stack = this.inventory.getStackInSlot(index);
                this.heal((float) ((ItemFood) stack.getItem()).func_150905_g(stack));
                --stack.stackSize;
                if (stack.stackSize <= 0) {
                    this.inventory.setInventorySlotContents(index, null);
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.worldObj.isRemote) {
            this.inventory.dropAllItems();
            if (source.getEntity() instanceof EntityPlayer && !((EntityPlayer) source.getEntity()).capabilities.isCreativeMode) {
                this.dropEntity();
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float par2) {
        Entity attacker = damageSource.getEntity();
        if ((attacker instanceof EntityPlayer) && attacker.equals(this.getOwner())) {
            if (this.myRole == Role.MANNEQUIN && !((EntityPlayer) damageSource.getEntity()).capabilities.isCreativeMode) {
                return false;
            }
            par2 = 10000.0F;
        }

        return super.attackEntityFrom(damageSource, par2);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        float power = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        int knockback = 0;
        ItemStack stack = this.getHeldItem();

        if (target instanceof EntityLivingBase) {
            power += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) target);
            knockback += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) target);
        }

        boolean flag = target.attackEntityFrom(DamageSource.causeMobDamage(this), power);

        if (flag) {
            if (knockback > 0) {
                double vx = -MathHelper.sin(NGTMath.toRadians(this.rotationYaw)) * (float) knockback * 0.5F;
                double vz = MathHelper.cos(NGTMath.toRadians(this.rotationYaw)) * (float) knockback * 0.5F;
                target.addVelocity(vx, 0.1D, vz);
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                target.setFire(j * 4);
            }

            if (target instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) target, this);
            }

            EnchantmentHelper.func_151385_b(this, target);
        }

        return flag;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float strength) {
        if (!this.isUsingItem()) {
            ItemStack item = this.getHeldItem();
            if (item != null && item.getItem() instanceof ItemGun) {
                item.useItemRightClick(this.worldObj, this.playerDummy);
                this.setUseItem(true);
            }
        }
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (!this.worldObj.isRemote) {
            player.openGui(RTMCore.instance, RTMCore.guiIdNPC, this.worldObj, this.getEntityId(), 0, 0);
        }
        return true;
    }

    public boolean isUsingItem() {
        return this.dataWatcher.getWatchableObjectByte(21) == 1;
    }

    public void setUseItem(boolean par1) {
        this.dataWatcher.updateObject(21, (byte) (par1 ? 1 : 0));
    }

    public int getItemUseCount() {
        return this.useItemCount;
    }

    public String getMenu() {
        return this.dataWatcher.getWatchableObjectString(22);
    }

    public void setMenu(String s) {
        this.dataWatcher.updateObject(22, s);
    }

    @Override
    public double getYOffset() {
        return this.yOffset - 0.5F;
    }

    @Override
    protected void damageArmor(float damage) {
        this.inventory.damageArmor(this, damage);
    }

    @Override
    public int getTotalArmorValue() {
        return this.inventory.getTotalArmorValue();
    }

    @Override
    public ItemStack getEquipmentInSlot(int index) {
        return index == 0 ? this.inventory.mainInventory[0] : this.inventory.armorInventory[index - 1];
    }

    @Override
    public ItemStack getHeldItem() {
        return this.inventory.mainInventory[0];
    }

    @Override
    public void setCurrentItemOrArmor(int index, ItemStack item) {
        if (index == 0) {
            this.inventory.mainInventory[0] = item;
        } else {
            this.inventory.armorInventory[index - 1] = item;
        }
    }

    @Override
    public ItemStack[] getLastActiveItems() {
        return this.inventory.armorInventory;
    }

    @Override
    public ItemStack func_130225_q(int index) {
        return this.inventory.armorInventory[3 - index];//RenderBiped.shouldRenderPass()
    }

    @Override
    public String getModelType() {
        return "ModelNPC";
    }

    @Override
    public String getModelName() {
        return this.dataWatcher.getWatchableObjectString(20);
    }

    @Override
    public void setModelName(String name) {
        this.dataWatcher.updateObject(20, name);//ミニチュアでDWは非同期のため
        if (!this.worldObj.isRemote) {
            this.onInventoryChanged();//初期化
        }
    }

    public ModelSetNPC getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy() || !this.myModelSet.getConfig().getName().equals(this.getModelName())) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet(this.getModelType(), this.getModelName());
            if (!this.myModelSet.isDummy()) {
                this.myModelSet.dataFormatter.initDataMap(this.getResourceState().getDataMap());
            }
            this.myRole = Role.getRole(this.myModelSet.getConfig().role);
            this.myRole.init(this);
        }
        return this.myModelSet;
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

    public boolean isMotorman() {
        return false;
    }

    public void onInventoryChanged() {
        this.getModelSet();
        this.myRole.onInventoryChanged(this);
    }

    public EntityBullet getBullet(GunType type) {
        if (this.getAttackTarget() == null) {
            return new EntityBullet(this.worldObj, this, type.speed, type.bulletType);
        }
        return new EntityBullet(this.worldObj, this, this.getAttackTarget(), type.speed, type.bulletType);
    }
}