package jp.ngt.rtm.item;

import jp.ngt.rtm.RTMAchievement;
import jp.ngt.rtm.block.BlockConverter;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemCrowbar extends ItemSword {
	public ItemCrowbar() {
		super(ToolMaterial.IRON);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (!world.isRemote) {
			if (world.getBlock(par4, par5, par6) == Blocks.cobblestone) {
				byte b0 = BlockConverter.shouldCreateConverter(world, par4, par5, par6);
				if (b0 >= 0) {
					BlockConverter.createConverter(world, par4, par5, par6, b0, false);
					player.addStat(RTMAchievement.buildConverter, 1);
					return true;
				}
			} else {
				for (int i = 0; i < 64; ++i) {
					for (int j = 0; j < 64; ++j) {
                        int x = par4 + i - 32;
                        int z = par6 + j - 32;
                        TileEntity tile0 = world.getTileEntity(x, par5, z);
                        if (tile0 instanceof TileEntityLargeRailBase && ((TileEntityLargeRailBase) tile0).getRailCore() == null) {
                            world.setBlockToAir(x, par5, z);
                        }
                    }
				}
			}
		}
		return true;
	}
}