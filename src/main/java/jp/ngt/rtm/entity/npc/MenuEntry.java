package jp.ngt.rtm.entity.npc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MenuEntry {
	public final ItemStack item;
	public final int price;
	public final int maxCount;

	public MenuEntry(ItemStack par1, int par2) {
		this.item = par1;
		this.price = par2;
		this.maxCount = 64 / par1.stackSize;
	}

	@Override
	public int hashCode() {
		return this.item.getItem().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ItemStack) {
			return ItemStack.areItemStacksEqual(this.item, (ItemStack) object);
		}
		return false;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagCompound itemNBT = new NBTTagCompound();
		this.item.writeToNBT(itemNBT);
		nbt.setTag("item", itemNBT);
		nbt.setInteger("price", this.price);
		return nbt;
	}

	public static MenuEntry readFromNBT(NBTTagCompound nbt) {
		NBTTagCompound itemNBT = (NBTTagCompound) nbt.getTag("item");
		ItemStack item = ItemStack.loadItemStackFromNBT(itemNBT);
		if (item != null) {
			int price = nbt.getInteger("price");
			return new MenuEntry(item, price);
		}
		return null;
	}
}
