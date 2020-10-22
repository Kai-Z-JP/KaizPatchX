package jp.ngt.rtm.gui;

import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.electric.TileEntityTicketVendor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTicketVendor extends Container {
	private final TileEntityTicketVendor vendor;
	private InventoryVendor invVendor = new InventoryVendor(this);

	public ContainerTicketVendor(InventoryPlayer inventory, TileEntityTicketVendor par2) {
		this.vendor = par2;

		int x0 = 66;
		int y0 = 146;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, x0 + j * 18, y0 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventory, i, x0 + i * 18, y0 + 58));//1Slot:18
		}

		for (int i = 0; i < InventoryVendor.Slot_Size; ++i) {
			if (i == 0) {
				x0 = 232;
				y0 = 146;
			} else if (i == 1) {
				x0 = 232;
				y0 = 204;
			} else if (i >= 2 && i < 11) {
				int i1 = i - 2;
				x0 = 8 + (i1 % 3) * 18;
				y0 = 146 + (i1 / 3) * 18;
			} else {
				x0 = 26;
				y0 = 204;
			}
			this.addSlotToContainer(new Slot(this.invVendor, i, x0, y0));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.vendor.getWorldObj().getBlock(this.vendor.xCoord, this.vendor.yCoord, this.vendor.zCoord) != RTMBlock.ticketVendor ? false : player.getDistanceSq((double) this.vendor.xCoord + 0.5D, (double) this.vendor.yCoord + 0.5D, (double) this.vendor.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		if (!player.worldObj.isRemote) {
			for (int i = 0; i < this.invVendor.getSizeInventory(); ++i) {
				ItemStack itemstack = this.invVendor.getStackInSlotOnClosing(i);

				if (itemstack != null) {
					player.dropPlayerItemWithRandomChoice(itemstack, false);
				}
			}
		}
	}
}