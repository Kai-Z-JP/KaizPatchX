package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityTrainWorkBench;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class ContainerRTMWorkBench extends Container {
	private final InventoryCrafting craftMatrix = new InventoryCrafting(this, 5, 5);
	private final InventoryCrafting invBallast = new InventoryCrafting(this, 5, 1);
	private final InventoryUneditable sample = new InventoryUneditable(this, 1, 1);
	private final InventoryCraftResult craftResult = new InventoryCraftResult();

	private final World worldObj;
	private final TileEntityTrainWorkBench workBench;
	protected EntityPlayer thePlayer;
	private int lastCraftingTime;
	private final boolean isCreativeMode;

	public String modelName;
	public float railHeight = 0.0625F;

	/**
	 * 0:通常, 1:レール用
	 */
	protected int workbenchType;

	public ContainerRTMWorkBench(InventoryPlayer inventory, World world, TileEntityTrainWorkBench par3, boolean par4) {
		this.worldObj = world;
		this.thePlayer = inventory.player;
		this.workBench = par3;
		this.workBench.readItemsFromTile(this.craftMatrix, this.invBallast);
		this.workbenchType = par3.getBlockMetadata();
		this.isCreativeMode = par4;

		this.modelName = ModelPackManager.INSTANCE.getModelSet("ModelRail", "1067mm_Wood").getConfig().getName();

		//完成品スロット
		this.addSlotToContainer(new SlotWorkBench(inventory.player, this.craftMatrix, this.craftResult, 0, 138, 38));

		//見本スロット
		this.addSlotToContainer(new Slot(this.sample, 0, 138, 12));

		//クラフトスロット
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 5, 8 + j * 18, 12 + i * 18));
			}
		}

		//インベントリ(手持ち)
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 164));//1Slot:18
		}

		if (this.workbenchType == 1) {
			//道床スロット
			for (int i = 0; i < 5; ++i) {
				this.addSlotToContainer(new Slot(this.invBallast, i, 8 + i * 18, 104));
			}
		} else if (this.workbenchType == 0) {
			//インベントリ
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < 9; ++j) {
					this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));
				}
			}
		}

		this.onCraftMatrixChanged(this.craftMatrix);

		if (this.workbenchType == 1 && this.isCreativeMode) {
			this.setItemToSampleSlot(ItemRail.getRailItem(ItemRail.getDefaultProperty()));
		}
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4) {
		if (par1 >= 0 && par1 < this.inventorySlots.size()) {
			Slot slot = (Slot) this.inventorySlots.get(par1);
			if (slot != null && slot.inventory == this.sample) {
				return null;//サンプル表示用スロットは除外
			}
		}
		return super.slotClick(par1, par2, par3, par4);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		if (this.workBench.isCrafting()) {
			return;
		}

		ItemStack stack = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
		if (stack != null) {
			if (stack.getItem() == RTMItem.itemLargeRail) {
				if (this.workbenchType == 0) {
					stack = null;
				} else if (this.workbenchType == 1) {
					RailProperty prop = this.getProperty();
					stack = (prop == null) ? null : ItemRail.getRailItem(prop);
				}
			}
		} else if (this.workbenchType == 1 && this.isCreativeMode) {
			RailProperty prop = this.getProperty();
			stack = (prop == null) ? null : ItemRail.getRailItem(prop);
		}

		this.setItemToSampleSlot(stack);
	}

	private void setItemToSampleSlot(ItemStack stack) {
		this.sample.setInventorySlotContents(0, stack);
		this.detectAndSendChanges();
	}

	/**
	 * Guiでクラフト開始ボタンを押すと呼ばれる
	 */
	public void startCrafting() {
		this.consumeItemsInCraftMatrix();
	}

	/**
	 * アイテム消費<br>
	 * 本来はSlotWorkBench.onPickupFromSlot()で行う
	 */
	private void consumeItemsInCraftMatrix() {
		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
			this.decrSlotItem(this.craftMatrix, i);
		}

		for (int i = 0; i < this.invBallast.getSizeInventory(); ++i) {
			this.decrSlotItem(this.invBallast, i);
		}
	}

	private void decrSlotItem(InventoryCrafting inventory, int slotNum) {
		ItemStack itemInSlot = inventory.getStackInSlot(slotNum);
		if (itemInSlot == null) {
			return;
		}

		inventory.decrStackSize(slotNum, 1);

		if (itemInSlot.getItem().hasContainerItem(itemInSlot)) {
			ItemStack itemstack2 = itemInSlot.getItem().getContainerItem(itemInSlot);

			if (itemstack2 != null && itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage()) {
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this.thePlayer, itemstack2));
				return;
			}

			if (!itemInSlot.getItem().doesContainerItemLeaveCraftingGrid(itemInSlot) || !this.thePlayer.inventory.addItemStackToInventory(itemstack2)) {
				if (inventory.getStackInSlot(slotNum) == null) {
					inventory.setInventorySlotContents(slotNum, itemstack2);
				} else {
					this.thePlayer.dropPlayerItemWithRandomChoice(itemstack2, false);
				}
			}
		}
	}

	private RailProperty getProperty() {
		ItemStack stack = this.invBallast.getStackInSlot(0);
		Block block = (stack == null) ? Blocks.air : Block.getBlockFromItem(stack.getItem());
		int damage = (stack == null) ? 0 : stack.getItemDamage();

		if (!this.thePlayer.capabilities.isCreativeMode) {
			for (int i = 0; i < this.invBallast.getSizeInventory(); ++i) {
				ItemStack stack2 = this.invBallast.getStackInSlot(i);
				if (stack2 == null) {
					block = Blocks.air;
					break;
				}

				Block block2 = Block.getBlockFromItem(stack2.getItem());
				if (block2 != block || stack2.getItemDamage() != damage) {
					block = Blocks.air;
					break;
				}
			}
		}

		String name = this.modelName;
		if (block != null && name != null) {
			return new RailProperty(name, block, damage, this.railHeight);
		}
		return null;
	}

	@Override
	public void addCraftingToCrafters(ICrafting crafting) {
		super.addCraftingToCrafters(crafting);
		crafting.sendProgressBarUpdate(this, 0, this.workBench.getCraftingTime());
	}

	@Override
	public void detectAndSendChanges() {
		if (this.workBench.getCraftingTime() == TileEntityTrainWorkBench.Max_CraftingTime && this.getSampeItem() != null) {
			this.craftResult.setInventorySlotContents(0, this.getSampeItem().copy());
			if (this.workbenchType == 0 || !this.isCreativeMode) {
				this.sample.setInventorySlotContents(0, null);
			}
		}

		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting) this.crafters.get(i);

			if (this.lastCraftingTime != this.workBench.getCraftingTime()) {
				icrafting.sendProgressBarUpdate(this, 0, this.workBench.getCraftingTime());
			}
		}

		this.lastCraftingTime = this.workBench.getCraftingTime();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		if (par1 == 0) {
			this.workBench.setCraftingTime(par2);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		this.workBench.writeItemsToTile(this.craftMatrix, this.invBallast);
		this.workBench.markDirty();

		if (!player.worldObj.isRemote && this.getResultItem() != null) {
			player.entityDropItem(this.getResultItem(), 0.5F);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.worldObj.getBlock(this.workBench.xCoord, this.workBench.yCoord, this.workBench.zCoord) == RTMBlock.trainWorkBench && player.getDistanceSq((double) this.workBench.xCoord + 0.5D, (double) this.workBench.yCoord + 0.5D, (double) this.workBench.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			//0 完成品:0, 見本:1, クラフト:2~26, プレーヤ:27~35 + 36~62
			//1 完成品:0, 見本:1, クラフト:2~26, プレーヤ:27~35, 道床:36~40
			if (slotIndex == 0) {
				int index = (this.workbenchType == 1) ? 35 : 62;
				if (!this.mergeItemStack(itemstack1, 27, index, true)) {
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (this.workbenchType == 0 && slotIndex >= 36 && slotIndex < 63)//9*3
			{
				if (!this.mergeItemStack(itemstack1, 27, 35, false))//9*1
				{
					return null;
				}
			} else if (this.workbenchType == 0 && slotIndex >= 27 && slotIndex < 36)//9*1
			{
				if (!this.mergeItemStack(itemstack1, 36, 62, false))//9*3
				{
					return null;
				}
			} else if (!this.mergeItemStack(itemstack1, 27, 35, false))//9*1
			{
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean func_94530_a(ItemStack stack, Slot slot) {
		return slot.inventory != this.craftResult && super.func_94530_a(stack, slot);
	}

	public ItemStack getSampeItem() {
		return this.sample.getStackInSlot(0);
	}

	public ItemStack getResultItem() {
		return this.craftResult.getStackInSlot(0);
	}

	public void setRailProp(String name, float h) {
		this.modelName = name;
		this.railHeight = h;
		this.onCraftMatrixChanged(this.craftMatrix);
	}
}