package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.BlockMachineBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTicketVendor extends BlockMachineBase {
	public BlockTicketVendor() {
		super(Material.rock);
		this.setLightOpacity(0);
	}

	@Override
	public TileEntity createNewTileEntity(World par1World, int par2) {
		return new TileEntityTicketVendor();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
			;
		}

		if (!world.isRemote) {
			player.openGui(RTMCore.instance, RTMCore.instance.guiIdTicketVendor, world, x, y, z);
		}
		return true;
	}

	@Override
	public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
		if (!par1World.isRemote) {
			this.dropBlockAsItem(par1World, par2, par3, par4, new ItemStack(RTMItem.installedObject, 1, 5));
		}
	}
}