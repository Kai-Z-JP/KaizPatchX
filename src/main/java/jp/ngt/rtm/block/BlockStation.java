package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityStation;
import jp.ngt.rtm.world.station.StationManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockStation extends BlockContainer {
	//@SideOnly(Side.CLIENT)
	//protected IIcon icon_side;

	public BlockStation() {
		super(Material.rock);
	}

	@Override
	public TileEntity createNewTileEntity(World par1, int par2) {
		return new TileEntityStation();
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		if (StationManager.INSTANCE.stationCollection.getStation(x, y, z) == null) {
			return super.canPlaceBlockAt(world, x, y, z);
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (world.isRemote) {
			player.openGui(RTMCore.instance, RTMCore.guiIdStation, world, x, y, z);
		}
		return true;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if (!world.isRemote) {
			StationManager.INSTANCE.stationCollection.add(x, y, z);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);

		if (!world.isRemote) {
			StationManager.INSTANCE.stationCollection.remove(x, y, z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)//0:bottom,1:top,
	{
		return side == 1 ? this.blockIcon : Blocks.stone.getIcon(side, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1) {
		this.blockIcon = par1.registerIcon("rtm:station");
	}
}