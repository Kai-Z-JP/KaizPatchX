package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMachineBase extends BlockContainer {
	protected BlockMachineBase(Material mat) {
		super(mat);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		this.clickMachine(world, x, y, z, player);
		return true;
	}

	protected boolean clickMachine(World world, int x, int y, int z, EntityPlayer player) {
		if (player.isSneaking())//NGTUtil.isEquippedItem(player, RTMItem.crowbar))
		{
			if (world.isRemote) {
				player.openGui(RTMCore.instance, RTMCore.guiIdSelectTileEntityModel, player.worldObj, x, y, z);
			}
			return true;
		}
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntityMachineBase tile = (TileEntityMachineBase) world.getTileEntity(x, y, z);
		MachineConfig cfg = tile.getModelSet().getConfig();
		return tile.isGettingPower ? cfg.brightness[1] : cfg.brightness[0];
	}
}