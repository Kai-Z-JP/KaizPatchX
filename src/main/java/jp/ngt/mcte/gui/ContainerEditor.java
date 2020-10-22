package jp.ngt.mcte.gui;

import jp.ngt.mcte.editor.EntityEditor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEditor extends Container {
	private final EntityEditor editor;

	public ContainerEditor(EntityEditor par1) {
		this.editor = par1;
		this.editor.openInventory();

		this.addSlotToContainer(new Slot(this.editor, 0, 72, 152));
		this.addSlotToContainer(new Slot(this.editor, 1, 72, 172));

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(this.editor.getPlayer().inventory, i, 8 + i * 20, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return entityPlayer.getDistanceSqToEntity(this.editor) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(par2);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (par2 < this.editor.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.editor.getSizeInventory(), this.inventorySlots.size(), true)) {
					return null;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, this.editor.getSizeInventory(), false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);

		if (!par1EntityPlayer.worldObj.isRemote) {
			for (int i = 0; i < this.editor.getSizeInventory(); ++i) {
				ItemStack itemstack = this.editor.getStackInSlotOnClosing(i);
				if (itemstack != null) {
					par1EntityPlayer.dropPlayerItemWithRandomChoice(itemstack, false);
				}
			}
			this.editor.setCloneBox(0, 0, 0, 0);
		}
	}
}