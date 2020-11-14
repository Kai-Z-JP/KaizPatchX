package jp.ngt.rtm.entity.npc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Arrays;
import java.util.stream.IntStream;

public class InventoryNPC implements IInventory {
    private final EntityNPC npc;
    public ItemStack[] mainInventory = new ItemStack[9 * 3];
    public ItemStack[] armorInventory = new ItemStack[4];
    public boolean isOpening;

    public InventoryNPC(EntityNPC par1) {
        this.npc = par1;
    }

	public NBTTagList writeToNBT(NBTTagList nbtList) {
        IntStream.range(0, this.mainInventory.length).filter(i -> this.mainInventory[i] != null).forEach(i -> {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByte("Slot", (byte) i);
            this.mainInventory[i].writeToNBT(nbt);
            nbtList.appendTag(nbt);
        });

        IntStream.range(0, this.armorInventory.length).filter(i -> this.armorInventory[i] != null).forEach(i -> {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByte("Slot", (byte) (i + 100));
            this.armorInventory[i].writeToNBT(nbt);
            nbtList.appendTag(nbt);
        });

        return nbtList;
    }

	public void readFromNBT(NBTTagList nbtList) {
        IntStream.range(0, nbtList.tagCount()).mapToObj(nbtList::getCompoundTagAt).forEach(nbt -> {
            int j = nbt.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbt);
            if (itemstack != null) {
                if (j < this.mainInventory.length) {
                    this.mainInventory[j] = itemstack;
                }

                if (j >= 100 && j < this.armorInventory.length + 100) {
                    this.armorInventory[j - 100] = itemstack;
                }
            }
        });
	}

	@Override
	public int getSizeInventory() {
		return this.mainInventory.length + 4;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index >= this.mainInventory.length) {
			return this.armorInventory[index - this.mainInventory.length];
		}
		return this.mainInventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int size) {
		ItemStack[] aitemstack = this.mainInventory;

		if (index >= this.mainInventory.length) {
			aitemstack = this.armorInventory;
			index -= this.mainInventory.length;
		}

		if (aitemstack[index] != null) {
			ItemStack itemstack;

            if (aitemstack[index].stackSize <= size) {
                itemstack = aitemstack[index];
                aitemstack[index] = null;
            } else {
                itemstack = aitemstack[index].splitStack(size);

                if (aitemstack[index].stackSize == 0) {
                    aitemstack[index] = null;
                }

            }
            return itemstack;
        } else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		ItemStack[] aitemstack = this.mainInventory;

		if (index >= this.mainInventory.length) {
			aitemstack = this.armorInventory;
			index -= this.mainInventory.length;
		}

		if (aitemstack[index] != null) {
			ItemStack itemstack = aitemstack[index];
			aitemstack[index] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		ItemStack[] aitemstack = this.mainInventory;
		if (index >= aitemstack.length) {
			index -= aitemstack.length;
			aitemstack = this.armorInventory;
		}
		aitemstack[index] = item;
	}

	@Override
	public String getInventoryName() {
		return "inventoryNPC";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		this.npc.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory() {
		this.isOpening = true;
	}

	@Override
	public void closeInventory() {
		this.isOpening = false;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack item) {
		return true;
	}

	public void dropAllItems() {
        IntStream.range(0, this.mainInventory.length).filter(i -> this.mainInventory[i] != null).forEach(i -> {
            this.npc.entityDropItem(this.mainInventory[i], 0.5F);
            this.mainInventory[i] = null;
        });

        IntStream.range(0, this.armorInventory.length).filter(i -> this.armorInventory[i] != null).forEach(i -> {
            this.npc.entityDropItem(this.armorInventory[i], 0.5F);
            this.armorInventory[i] = null;
        });
    }

	public int getTotalArmorValue() {
        return Arrays.stream(this.armorInventory).filter(itemStack -> itemStack != null && itemStack.getItem() instanceof ItemArmor).mapToInt(itemStack -> ((ItemArmor) itemStack.getItem()).damageReduceAmount).sum();
    }

	public void damageArmor(EntityLivingBase entity, float damage) {
		damage /= 4.0F;
		if (damage < 1.0F) {
			damage = 1.0F;
		}

		for (int i = 0; i < this.armorInventory.length; ++i) {
			if (this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
				this.armorInventory[i].damageItem((int) damage, entity);
				if (this.armorInventory[i].stackSize == 0) {
					this.armorInventory[i] = null;
				}
			}
		}
	}

	public int hasItem(Class<? extends Item> clazz) {
		for (int i = 0; i < this.getSizeInventory(); ++i) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack != null && stack.getItem().getClass() == clazz) {
				return i;
			}
		}
		return -1;
	}
}