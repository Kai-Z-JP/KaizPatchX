package jp.ngt.ngtlib.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockCustomColor extends ItemBlock {
    public ItemBlockCustomColor(Block block) {
        super(block);
    }

    @Override
    public int getColorFromItemStack(ItemStack p_82790_1_, int p_82790_2_) {
        return this.field_150939_a.getRenderColor(p_82790_1_.getItemDamage());
    }
}