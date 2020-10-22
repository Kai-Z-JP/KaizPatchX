package jp.ngt.rtm.item;

import jp.ngt.ngtlib.item.ItemMultiIcon;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Map;

public class ItemMirror extends ItemMultiIcon {
	public ItemMirror(Map<Integer, String> par1) {
		super(par1);
	}

	@Override
	public String getUnlocalizedName(ItemStack par1) {
		int meta = par1.getItemDamage();
		switch (meta) {
			case 0:
				return super.getUnlocalizedName() + ".plate";
			default:
				return super.getUnlocalizedName() + ".block." + (meta - 20);
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (!world.isRemote) {
			int meta = itemStack.getItemDamage();
			int x = par4;
			int y = par5;
			int z = par6;
			Block block = null;

			if (par7 == 0)//up
			{
				--par5;
			} else if (par7 == 1)//down
			{
				++par5;
			} else if (par7 == 2)//south
			{
				--par6;
			} else if (par7 == 3)//north
			{
				++par6;
			} else if (par7 == 4)//east
			{
				--par4;
			} else if (par7 == 5)//west
			{
				++par4;
			}

			if (!world.isAirBlock(par4, par5, par6)) {
				return true;
			}

			if (!player.canPlayerEdit(par4, par5, par6, par7, itemStack) || !world.isAirBlock(par4, par5, par6)) {
				return true;
			}

			if (meta == 0) {
				world.setBlock(par4, par5, par6, RTMBlock.mirror, par7, 3);
				block = RTMBlock.mirror;
			} else {
				world.setBlock(par4, par5, par6, RTMBlock.mirrorCube, meta - 20, 3);
				block = RTMBlock.mirror;
			}

			if (block != null) {
				world.playSoundEffect((double) par4 + 0.5D, (double) par5 + 0.5D, (double) par6 + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
				--itemStack.stackSize;
			}
		}

		return true;
	}
}