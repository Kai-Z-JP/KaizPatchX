package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMAchievement;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemHacksaw extends Item {
	public ItemHacksaw() {
		this.maxStackSize = 1;
		this.setMaxDamage(ToolMaterial.IRON.getMaxUses());
		this.setCreativeTab(CreativeTabs.tabTools);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if (!player.canPlayerEdit(x, y, z, side, itemstack)) {
			return false;
		} else {
			Block block = world.getBlock(x, y, z);
			if (block == RTMBlock.steelSlab && world.getBlockMetadata(x, y, z) == 0) {
				if (world.isRemote) {
					return true;
				} else {
					player.entityDropItem(new ItemStack(RTMItem.steel_ingot, 1, 0), 0.5F);
					world.setBlock(x, y, z, Blocks.air);
					itemstack.damageItem(1, player);
					player.addStat(RTMAchievement.getSteel, 1);
					return true;
				}
			} else {
				return false;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}
}