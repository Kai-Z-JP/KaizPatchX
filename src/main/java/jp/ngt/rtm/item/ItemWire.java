package jp.ngt.rtm.item;

import jp.ngt.rtm.modelpack.cfg.WireConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWire extends ItemWithModel {
	public ItemWire() {
		super();
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		return false;
	}

	@Override
	protected String getModelType(ItemStack itemStack) {
		return WireConfig.TYPE;
	}

	@Override
	protected String getDefaultModelName(ItemStack itemStack) {
		return "BasicWireBlack";
	}
}