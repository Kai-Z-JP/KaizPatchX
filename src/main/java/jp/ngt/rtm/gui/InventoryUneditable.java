package jp.ngt.rtm.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUneditable implements IInventory {
	private ItemStack[] stackList;
	private int inventoryWidth;

	public InventoryUneditable(Container container, int size) {
		this.stackList = new ItemStack[size];
		this.inventoryWidth = 1;
	}

	public InventoryUneditable(Container container, int width, int height) {
		int k = width * height;
		this.stackList = new ItemStack[k];
		this.inventoryWidth = width;
	}

	@Override
	public int getSizeInventory() {
		return this.stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1) {
		return par1 >= this.getSizeInventory() ? null : this.stackList[par1];
	}

	@Override
	public String getInventoryName() {
		return "container.uneditable";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int size) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index < this.stackList.length) {
			this.stackList[index] = stack;
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}
}