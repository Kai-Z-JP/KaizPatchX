package jp.ngt.ngtlib.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

public class ItemBlockCustom extends ItemBlockWithMetadata {
	public ItemBlockCustom(Block block) {
		super(block, block);
	}

	@Override
	public String getUnlocalizedName(ItemStack par1) {
		return super.getUnlocalizedName() + "." + par1.getItemDamage();
	}
}