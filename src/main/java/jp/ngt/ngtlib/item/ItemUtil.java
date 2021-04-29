package jp.ngt.ngtlib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class ItemUtil {
    public static boolean isItemEqual(ItemStack par1, ItemStack par2) {
        if (par1.getItem() == par2.getItem()) {
            int damage1 = par1.getItemDamage();
            int damage2 = par2.getItemDamage();
            return damage1 == damage2 || damage1 == 32767 || damage2 == 32767;
        }
        return false;
    }

    /**
     * アイテムをNBTへ書き込む（スタックサイズをIntegerで保存）
     */
    public static NBTTagCompound writeToNBT(NBTTagCompound par1, ItemStack par2) {
        par1.setShort("id", (short) Item.getIdFromItem(par2.getItem()));
        par1.setInteger("Count", par2.stackSize);
        par1.setShort("Damage", (short) par2.getItemDamage());
        if (par2.stackTagCompound != null) {
            par1.setTag("tag", par2.stackTagCompound);
        }
        return par1;
    }

    /**
     * アイテムをNBTから読み出す（スタックサイズをIntegerで保存）
     */
    public static ItemStack readFromNBT(NBTTagCompound par1) {
        Item item = Item.getItemById(par1.getShort("id"));
        int size = par1.getInteger("Count");
        int damage = par1.getShort("Damage");
        if (damage < 0) {
            damage = 0;
        }

        ItemStack itemstack = new ItemStack(item, size, damage);
        if (par1.hasKey("tag", 10)) {
            NBTTagCompound nbt = par1.getCompoundTag("tag");
            itemstack.setTagCompound(nbt);
        }
        return itemstack;
    }

    /**
     * @param stack ItemWritableBook
     * @return ItemWritableBookに書かれている内容
     */
    public static String[] bookToStrings(ItemStack stack) {
        if (stack.getItem() instanceof ItemWritableBook) {
            if (stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound();
                NBTTagList bookPages = nbt.getTagList("pages", 8);
                if (bookPages != null) {
                    String s1 = bookPages.getStringTagAt(0);
                    return s1.split("\n");
                }
            }
        }
        return new String[0];
    }
}