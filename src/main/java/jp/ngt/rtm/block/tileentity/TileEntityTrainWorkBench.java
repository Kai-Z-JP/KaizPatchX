package jp.ngt.rtm.block.tileentity;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.gui.ContainerRTMWorkBench;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTrainWorkBench extends TileEntity //implements IInventory
{
	private ItemStack[] craftSlots = new ItemStack[30];

	public static final int Max_CraftingTime = 64;
	private int craftingTime = 0;
	private boolean isCrafting = false;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.craftSlots = new ItemStack[30];
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbt1 = nbttaglist.getCompoundTagAt(i);
			int j = nbt1.getByte("Slot") & 255;
			if (j >= 0 && j < this.craftSlots.length) {
				this.craftSlots[j] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList tagList = new NBTTagList();
		for (int i = 0; i < this.craftSlots.length; ++i) {
			if (this.craftSlots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("Slot", (byte) i);
				this.craftSlots[i].writeToNBT(nbt1);
				tagList.appendTag(nbt1);
			}
		}
		nbt.setTag("Items", tagList);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (this.isCrafting) {
			if (this.craftingTime < Max_CraftingTime) {
				++this.craftingTime;
			} else {
				this.craftingTime = 0;
				this.isCrafting = false;
			}
		}
	}

	public void readItemsFromTile(IInventory inventory, IInventory inv2) {
		for (int i = 0; i < 25; ++i) {
			inventory.setInventorySlotContents(i, this.craftSlots[i]);
		}

		for (int i = 25; i < 30; ++i) {
			inv2.setInventorySlotContents(i - 25, this.craftSlots[i]);
		}
	}

	public void writeItemsToTile(IInventory inventory, IInventory inv2) {
		for (int i = 0; i < this.craftSlots.length; ++i) {
			this.craftSlots[i] = inventory.getStackInSlot(i);
		}

		for (int i = 25; i < 30; ++i) {
			this.craftSlots[i] = inv2.getStackInSlot(i - 25);
		}

		this.sendPacket();
	}

	public int getCraftingTime() {
		return this.craftingTime;
	}

	public void setCraftingTime(int par1) {
		this.craftingTime = par1;
	}

	public void startCrafting(EntityPlayer player, boolean sendPacket) {
		this.craftingTime = 0;
		this.isCrafting = true;

		ContainerRTMWorkBench container = (ContainerRTMWorkBench) player.openContainer;
		container.startCrafting();

		if (sendPacket)//isServer
		{
			String s = "StartCrafting";
			RTMCore.NETWORK_WRAPPER.sendToServer(new PacketNotice(PacketNotice.Side_SERVER, s, this));
		}
	}

	public boolean isCrafting() {
		return this.isCrafting;
	}

	private void sendPacket() {
		NGTUtil.sendPacketToClient(this);
	}

	@Override
	public Packet getDescriptionPacket() {
		this.sendPacket();
		return null;
	}
}