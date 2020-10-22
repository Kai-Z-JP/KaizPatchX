package jp.ngt.rtm.electric;

import jp.ngt.rtm.block.tileentity.TileEntityMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class TileEntityTicketVendor extends TileEntityMachineBase implements IInventory {
	@Override
	public MachineType getMachinleType() {
		return MachineType.Vendor;
	}

	/**********************************************************************************/

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int par1) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2) {
		;
	}

	@Override
	public String getInventoryName() {
		return "TicketVendor";
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
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory() {
		;
	}

	@Override
	public void closeInventory() {
		;
	}

	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2) {
		return false;
	}

	@Override
	protected String getDefaultName() {
		return "Turnstile01";
	}
}