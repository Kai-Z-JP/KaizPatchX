package jp.ngt.mcte.item;

import jp.ngt.mcte.MCTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGenerator extends Item {
	public ItemGenerator() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		if (world.isRemote) {
			player.openGui(MCTE.instance, MCTE.guiIdGenerator, world, x, y, z);
		}
		return true;
	}
}