package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.entity.EntityBullet;
import jp.ngt.rtm.entity.npc.EntityDummyPlayer;
import jp.ngt.rtm.item.ItemAmmunition.BulletType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemGun extends Item {
    public static final int INTERVAL = 2;

    public final GunType gunType;

    public ItemGun(GunType par1) {
        super();
        this.gunType = par1;
        this.maxStackSize = 1;
        this.setMaxDamage(par1.maxSize);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        if (itemstack.getItemDamage() < itemstack.getMaxDamage())//0→Max
        {
            player.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
        } else if (player.inventory.hasItem(this.getMagazineFromGunType())) {
            if (!player.capabilities.isCreativeMode && !world.isRemote) {
                int i0 = this.setMagazine(player);
                if (i0 < itemstack.getMaxDamage()) {
                    itemstack.setItemDamage(i0);
                    player.entityDropItem(new ItemStack(this.getMagazineFromGunType(), 1, itemstack.getMaxDamage()), 1.0F);
                }
            }
        }

        return itemstack;
    }

    private int setMagazine(EntityPlayer player) {
        Item magazineItem = this.getMagazineFromGunType();
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == magazineItem) {
                if (stack.getItemDamage() < magazineItem.getMaxDamage()) {
                    player.inventory.setInventorySlotContents(i, null);
                    return stack.getItemDamage();
                }
            }
        }
        return 1024;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int count) {
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (stack.getItemDamage() == stack.getMaxDamage()) {
            //player.stopUsingItem();
        } else if (this.onUsingGun(stack, player.worldObj, player, count)) {
            if (!player.capabilities.isCreativeMode) {
                if (stack.isItemStackDamageable()) {
                    stack.setItemDamage(stack.getItemDamage() + 1);
                }
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemStack) {
        return this.gunType.useDuration;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemStack) {
        return EnumAction.bow;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    /**
     * アイテムを使用している間、Tickごとに呼ばれる
     */
    protected boolean onUsingGun(ItemStack itemstack, World world, EntityPlayer player, int count) {
        if (count % INTERVAL > 0) {
            return false;
        }

        Item item = itemstack.getItem();
        if (!this.gunType.rapidFire && count < this.gunType.useDuration) {
            return false;
        }

        if (!world.isRemote) {
            EntityBullet bullet;
            if (player instanceof EntityDummyPlayer) {
                bullet = ((EntityDummyPlayer) player).npc.getBullet(this.gunType);
            } else {
                bullet = new EntityBullet(world, player, this.gunType.speed, this.gunType.bulletType);
            }
            world.spawnEntityInWorld(bullet);
            world.playSoundEffect(player.posX, player.posY, player.posZ, "rtm:item.gun", RTMConfig.gunSoundVol, 1.0F);

            if (!player.capabilities.isCreativeMode) {
                int damage = this.gunType.bulletType.id * 4 + 2;
                player.entityDropItem(new ItemStack(RTMItem.bullet, 1, damage), 0.5F);//薬莢ドロップ
            }
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        int max = itemStack.getMaxDamage();
        list.add(EnumChatFormatting.GRAY + "Bullet:" + (max - itemStack.getItemDamage()) + "/" + max);
    }

    public Item getMagazineFromGunType() {
        switch (this.gunType) {
            case handgun:
                return RTMItem.magazine_handgun;
            case rifle:
                return RTMItem.magazine_rifle;
            case autoloading_rifle:
                return RTMItem.magazine_alr;
            case sniper_rifle:
                return RTMItem.magazine_sr;
            case smg:
                return RTMItem.magazine_smg;
            case amr:
                return RTMItem.magazine_amr;
        }
        return RTMItem.magazine_handgun;
    }

    public enum GunType {
        handgun(BulletType.handgun_9mm, 10, 16, 4.5F, false),
        rifle(BulletType.rifle_7_62mm, 5, 16, 7.5F, false),
        autoloading_rifle(BulletType.rifle_7_62mm, 30, 6, 7.5F, true),
        sniper_rifle(BulletType.rifle_7_62mm, 10, 20, 7.5F, false),
        smg(BulletType.handgun_9mm, 30, 6, 4.5F, true),
        amr(BulletType.rifle_12_7mm, 10, 24, 9.0F, false);

        public final BulletType bulletType;
        public final int maxSize;
        public final int useDuration;
        public final float speed;
        public boolean rapidFire;

        GunType(BulletType par1, int par2, int par3, float par4, boolean par5) {
            this.bulletType = par1;
            this.maxSize = par2;
            this.useDuration = par3;
            this.speed = par4;
            this.rapidFire = par5;
        }
    }
}