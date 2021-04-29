package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.item.ItemGun.GunType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ItemMagazine extends Item {
    public final GunType magazineType;

    public ItemMagazine(GunType par1) {
        super();
        this.magazineType = par1;
        this.maxStackSize = 1;
        this.setMaxDamage(par1.maxSize);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        int max = itemStack.getMaxDamage();
        list.add(EnumChatFormatting.GRAY + "Bullet:" + (max - itemStack.getItemDamage()) + "/" + max);
    }
}