package jp.ngt.rtm.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ContainerItemContainer extends Container {
	private final IInventory inventory;
	private final int numRows;
	private int field_94535_f = -1;
	private int field_94536_g;
	private final Set field_94537_h = new HashSet();
	private final int maxContainerSlotNumber;

	public ContainerItemContainer(InventoryPlayer invPlayer, IInventory invMain) {
		this.inventory = invMain;
		invMain.openInventory();
		this.numRows = this.inventory.getSizeInventory() / 9;
		int i = (this.numRows - 4) * 18;
		int j;
		int k;

		for (j = 0; j < this.numRows; ++j) {
			for (k = 0; k < 9; ++k) {
				this.addSlotToContainer(new Slot(invMain, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for (j = 0; j < 3; ++j) {
			for (k = 0; k < 9; ++k) {
				this.addSlotToContainer(new Slot(invPlayer, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
			}
		}

		for (j = 0; j < 9; ++j) {
			this.addSlotToContainer(new Slot(invPlayer, j, 8 + j * 18, 161 + i));
		}

		this.maxContainerSlotNumber = this.inventory.getSizeInventory() - 1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return this.inventory.isUseableByPlayer(var1);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(par2);
		if (slot != null) {
			if (slot.getHasStack()) {
				ItemStack itemstack1 = slot.getStack();
				itemstack = itemstack1.copy();
				if (par2 < this.inventory.getSizeInventory()) {
					if (!this.mergeItemStack(itemstack1, this.inventory.getSizeInventory(), this.inventorySlots.size(), true)) {
						return null;
					}
				} else if (!this.mergeItemStack(itemstack1, 0, this.inventory.getSizeInventory(), false)) {
					return null;
				}

				if (itemstack1.stackSize == 0) {
					slot.putStack(null);
				} else {
					slot.onSlotChanged();
				}
			} else {
			}
		}
		return itemstack;
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);
		this.inventory.closeInventory();
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer) {
		ItemStack itemstack = null;
		InventoryPlayer inventoryplayer = par4EntityPlayer.inventory;
		int i1;
		ItemStack itemstack3;

		if (par3 == 5) {
			int l = this.field_94536_g;
			this.field_94536_g = func_94532_c(par2);

			if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g) {
				this.func_94533_d();
			} else if (inventoryplayer.getItemStack() == null) {
				this.func_94533_d();
			} else if (this.field_94536_g == 0) {
				this.field_94535_f = func_94529_b(par2);

				if (func_94528_d(this.field_94535_f)) {
					this.field_94536_g = 1;
					this.field_94537_h.clear();
				} else {
					this.func_94533_d();
				}
			} else if (this.field_94536_g == 1) {
				Slot slot = (Slot) this.inventorySlots.get(par1);

				if (slot != null && func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size() && this.canDragIntoSlot(slot)) {
					this.field_94537_h.add(slot);
				}
			} else if (this.field_94536_g == 2) {
				if (!this.field_94537_h.isEmpty()) {
					itemstack3 = inventoryplayer.getItemStack().copy();
					i1 = inventoryplayer.getItemStack().stackSize;
					Iterator iterator = this.field_94537_h.iterator();

					while (iterator.hasNext()) {
						Slot slot1 = (Slot) iterator.next();

						if (slot1 != null && func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size() && this.canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);

							if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
								itemstack1.stackSize = itemstack1.getMaxStackSize();
							}

							if (itemstack1.stackSize > slot1.getSlotStackLimit()) {
								itemstack1.stackSize = slot1.getSlotStackLimit();
							}

							i1 -= itemstack1.stackSize - j1;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.stackSize = i1;

					if (itemstack3.stackSize <= 0) {
						itemstack3 = null;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				this.func_94533_d();
			} else {
				this.func_94533_d();
			}
		} else if (this.field_94536_g != 0) {
			this.func_94533_d();
		} else {
			Slot slot2;
			int l1;
			ItemStack itemstack5;

			if ((par3 == 0 || par3 == 1) && (par2 == 0 || par2 == 1)) {
				if (par1 == -999) {
					if (inventoryplayer.getItemStack() != null && par1 == -999) {
						if (par2 == 0) {
							par4EntityPlayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
							inventoryplayer.setItemStack(null);
						}

						if (par2 == 1) {
							par4EntityPlayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

							if (inventoryplayer.getItemStack().stackSize == 0) {
								inventoryplayer.setItemStack(null);
							}
						}
					}
				} else if (par3 == 1) {
					if (par1 < 0) {
						return null;
					}

					slot2 = (Slot) this.inventorySlots.get(par1);

					if (slot2 != null && slot2.canTakeStack(par4EntityPlayer)) {
						itemstack3 = this.transferStackInSlot(par4EntityPlayer, par1);

						if (itemstack3 != null) {
							Item item = itemstack3.getItem();
							itemstack = itemstack3.copy();

							if (slot2.getStack() != null && slot2.getStack().getItem() == item) {
								this.retrySlotClick(par1, par2, true, par4EntityPlayer);
							}
						}
					}
				} else {
					if (par1 < 0) {
						return null;
					}

					slot2 = (Slot) this.inventorySlots.get(par1);

					if (slot2 != null) {
						itemstack3 = slot2.getStack();
						ItemStack itemstack4 = inventoryplayer.getItemStack();

						if (itemstack3 != null) {
							itemstack = itemstack3.copy();
						}

						if (itemstack3 == null) {
							if (itemstack4 != null && slot2.isItemValid(itemstack4)) {
								l1 = par2 == 0 ? itemstack4.stackSize : 1;

								if (l1 > slot2.getSlotStackLimit()) {
									l1 = slot2.getSlotStackLimit();
								}

								if (itemstack4.stackSize >= l1) {
									slot2.putStack(itemstack4.splitStack(l1));
								}

								if (itemstack4.stackSize == 0) {
									inventoryplayer.setItemStack(null);
								}
							}
						} else if (slot2.canTakeStack(par4EntityPlayer)) {
							if (itemstack4 == null) {
								l1 = par2 == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
								itemstack5 = slot2.decrStackSize(l1);
								inventoryplayer.setItemStack(itemstack5);

								if (itemstack3.stackSize == 0) {
									slot2.putStack(null);
								}

								slot2.onPickupFromSlot(par4EntityPlayer, inventoryplayer.getItemStack());
							} else if (slot2.isItemValid(itemstack4)) {
								//スロット内と保持してるアイテムを合体
								if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4)) {
									l1 = par2 == 0 ? itemstack4.stackSize : 1;

									if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize) {
										l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
									}

									if (slot2.slotNumber > this.maxContainerSlotNumber) {
										if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize) {
											l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
										}
									}

									itemstack4.splitStack(l1);

									if (itemstack4.stackSize == 0) {
										inventoryplayer.setItemStack(null);
									}

									itemstack3.stackSize += l1;
								} else if (itemstack4.stackSize <= slot2.getSlotStackLimit()) {
									slot2.putStack(itemstack4);
									inventoryplayer.setItemStack(itemstack3);
								}
							} else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4)) {
								l1 = itemstack3.stackSize;

								if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize()) {
									itemstack4.stackSize += l1;
									itemstack3 = slot2.decrStackSize(l1);

									if (itemstack3.stackSize == 0) {
										slot2.putStack(null);
									}

									slot2.onPickupFromSlot(par4EntityPlayer, inventoryplayer.getItemStack());
								}
							}
						}

						slot2.onSlotChanged();
					}
				}
			} else if (par3 == 2 && par2 >= 0 && par2 < 9) {
				slot2 = (Slot) this.inventorySlots.get(par1);

				if (slot2.canTakeStack(par4EntityPlayer)) {
					itemstack3 = inventoryplayer.getStackInSlot(par2);
					boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
					l1 = -1;

					if (!flag) {
						l1 = inventoryplayer.getFirstEmptyStack();
						flag |= l1 > -1;
					}

					if (slot2.getHasStack() && flag) {
						itemstack5 = slot2.getStack();
						inventoryplayer.setInventorySlotContents(par2, itemstack5.copy());

						if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null) {
							if (l1 > -1) {
								inventoryplayer.addItemStackToInventory(itemstack3);
								slot2.decrStackSize(itemstack5.stackSize);
								slot2.putStack(null);
								slot2.onPickupFromSlot(par4EntityPlayer, itemstack5);
							}
						} else {
							slot2.decrStackSize(itemstack5.stackSize);
							slot2.putStack(itemstack3);
							slot2.onPickupFromSlot(par4EntityPlayer, itemstack5);
						}
					} else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3)) {
						inventoryplayer.setInventorySlotContents(par2, null);
						slot2.putStack(itemstack3);
					}
				}
			} else if (par3 == 3 && par4EntityPlayer.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && par1 >= 0) {
				slot2 = (Slot) this.inventorySlots.get(par1);

				if (slot2 != null && slot2.getHasStack()) {
					itemstack3 = slot2.getStack().copy();
					itemstack3.stackSize = itemstack3.getMaxStackSize();
					inventoryplayer.setItemStack(itemstack3);
				}
			} else if (par3 == 4 && inventoryplayer.getItemStack() == null && par1 >= 0) {
				slot2 = (Slot) this.inventorySlots.get(par1);

				if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(par4EntityPlayer)) {
					itemstack3 = slot2.decrStackSize(par2 == 0 ? 1 : slot2.getStack().stackSize);
					slot2.onPickupFromSlot(par4EntityPlayer, itemstack3);
					par4EntityPlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
				}
			} else if (par3 == 6 && par1 >= 0) {
				slot2 = (Slot) this.inventorySlots.get(par1);
				itemstack3 = inventoryplayer.getItemStack();

				if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(par4EntityPlayer))) {
					i1 = par2 == 0 ? 0 : this.inventorySlots.size() - 1;
					l1 = par2 == 0 ? 1 : -1;

					for (int i2 = 0; i2 < 2; ++i2) {
						for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1) {
							Slot slot3 = (Slot) this.inventorySlots.get(j2);

							if (slot3.getHasStack() && func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(par4EntityPlayer) && this.func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize())) {
								int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
								ItemStack itemstack2 = slot3.decrStackSize(k1);
								itemstack3.stackSize += k1;

								if (itemstack2.stackSize <= 0) {
									slot3.putStack(null);
								}

								slot3.onPickupFromSlot(par4EntityPlayer, itemstack2);
							}
						}
					}
				}

				this.detectAndSendChanges();
			}
		}

		return itemstack;
	}

	public static boolean func_94527_a(Slot par0Slot, ItemStack par1ItemStack, boolean par2) {
		boolean flag1 = par0Slot == null || !par0Slot.getHasStack();

		if (par0Slot != null && par0Slot.getHasStack() && par1ItemStack != null && par1ItemStack.isItemEqual(par0Slot.getStack()) && ItemStack.areItemStackTagsEqual(par0Slot.getStack(), par1ItemStack)) {
			int i = par2 ? 0 : par1ItemStack.stackSize;
			if (par0Slot.slotNumber <= 53) {
				flag1 |= par0Slot.getStack().stackSize + i <= par0Slot.getSlotStackLimit();
			} else {
				flag1 |= par0Slot.getStack().stackSize + i <= par1ItemStack.getMaxStackSize();
			}
		}

		return flag1;
	}

	@Override
	protected boolean mergeItemStack(ItemStack par1ItemStack, int par2, int par3, boolean par4) {
		boolean flag1 = false;
		int k = par2;

		if (par4) {
			k = par3 - 1;
		}

		Slot slot;
		ItemStack itemstack1;

		if (par1ItemStack.isStackable()) {
			while (par1ItemStack.stackSize > 0 && (!par4 && k < par3 || par4 && k >= par2)) {
				slot = (Slot) this.inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (itemstack1 != null && itemstack1.getItem() == par1ItemStack.getItem() && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1)) {
					int l = itemstack1.stackSize + par1ItemStack.stackSize;
					//コンテナのスロットか他のスロットか
					int maxSize = slot.slotNumber <= this.maxContainerSlotNumber ? slot.getSlotStackLimit() : par1ItemStack.getMaxStackSize();

					if (l <= maxSize) {
						par1ItemStack.stackSize = 0;
						itemstack1.stackSize = l;
						slot.onSlotChanged();
						flag1 = true;
					} else if (itemstack1.stackSize < maxSize) {
						par1ItemStack.stackSize -= maxSize - itemstack1.stackSize;
						itemstack1.stackSize = maxSize;
						slot.onSlotChanged();
						flag1 = true;
					}
				}

				if (par4) {
					--k;
				} else {
					++k;
				}
			}
		}

		if (par1ItemStack.stackSize > 0) {
			if (par4) {
				k = par3 - 1;
			} else {
				k = par2;
			}

			while (!par4 && k < par3 || par4 && k >= par2) {
				slot = (Slot) this.inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (itemstack1 == null) {
					slot.putStack(par1ItemStack.copy());
					slot.onSlotChanged();
					par1ItemStack.stackSize = 0;
					flag1 = true;
					break;
				}

				if (par4) {
					--k;
				} else {
					++k;
				}
			}
		}

		return flag1;
	}
}