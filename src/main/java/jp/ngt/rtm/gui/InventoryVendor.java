package jp.ngt.rtm.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryVendor implements IInventory {
	public static final byte Slot_Size = 12;

	private final ContainerTicketVendor container;
	private final ItemStack[] stackList;

	public InventoryVendor(ContainerTicketVendor par1) {
		this.container = par1;
		this.stackList = new ItemStack[Slot_Size];//money_in:1, card:1, money_out:9, ticket:1
	}

	@Override
	public int getSizeInventory() {
		return this.stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1) {
		return par1 >= this.stackList.length ? null : this.stackList[par1];
	}

	@Override
	public String getInventoryName() {
		return "Vendor";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		return this.getStackInSlot(par1);
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2) {
		ItemStack stack = this.getStackInSlot(par1);
		if (stack != null) {
            if (stack.stackSize <= par2) {
                this.setInventorySlotContents(par1, null);
            } else {
                stack = stack.splitStack(par2);
                if (this.getStackInSlot(par1).stackSize == 0) {
                    this.setInventorySlotContents(par1, null);
                }

            }
            return stack;
        }
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
		return true;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2) {
		return true;
	}
}