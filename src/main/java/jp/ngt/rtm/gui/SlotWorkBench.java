package jp.ngt.rtm.gui;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;

public class SlotWorkBench extends Slot {
	private final IInventory craftMatrix;
	private EntityPlayer thePlayer;
	private int amountCrafted;

	public SlotWorkBench(EntityPlayer player, IInventory inventory1, IInventory inventory2, int p_i1823_4_, int p_i1823_5_, int p_i1823_6_) {
		super(inventory2, p_i1823_4_, p_i1823_5_, p_i1823_6_);
		this.thePlayer = player;
		this.craftMatrix = inventory1;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int p_75209_1_) {
		if (this.getHasStack()) {
			this.amountCrafted += Math.min(p_75209_1_, this.getStack().stackSize);
		}

		return super.decrStackSize(p_75209_1_);
	}

	@Override
	protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {
		this.amountCrafted += p_75210_2_;
		this.onCrafting(p_75210_1_);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
	 */
	@Override
	protected void onCrafting(ItemStack itemStack) {
		itemStack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted);
		this.amountCrafted = 0;

		if (itemStack.getItem() == Item.getItemFromBlock(Blocks.crafting_table)) {
			this.thePlayer.addStat(AchievementList.buildWorkBench, 1);
		}

		if (itemStack.getItem() instanceof ItemPickaxe) {
			this.thePlayer.addStat(AchievementList.buildPickaxe, 1);
		}

		if (itemStack.getItem() == Item.getItemFromBlock(Blocks.furnace)) {
			this.thePlayer.addStat(AchievementList.buildFurnace, 1);
		}

		if (itemStack.getItem() instanceof ItemHoe) {
			this.thePlayer.addStat(AchievementList.buildHoe, 1);
		}

		if (itemStack.getItem() == Items.bread) {
			this.thePlayer.addStat(AchievementList.makeBread, 1);
		}

		if (itemStack.getItem() == Items.cake) {
			this.thePlayer.addStat(AchievementList.bakeCake, 1);
		}

		if (itemStack.getItem() instanceof ItemPickaxe && ((ItemPickaxe) itemStack.getItem()).func_150913_i() != Item.ToolMaterial.WOOD) {
			this.thePlayer.addStat(AchievementList.buildBetterPickaxe, 1);
		}

		if (itemStack.getItem() instanceof ItemSword) {
			this.thePlayer.addStat(AchievementList.buildSword, 1);
		}

		if (itemStack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table)) {
			this.thePlayer.addStat(AchievementList.enchantments, 1);
		}

		if (itemStack.getItem() == Item.getItemFromBlock(Blocks.bookshelf)) {
			this.thePlayer.addStat(AchievementList.bookcase, 1);
		}
	}

	/**
	 * 完成品取り出し
	 */
	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
		FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStack, craftMatrix);
		this.onCrafting(itemStack);

		//本来はここでアイテム消費
	}
}