package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemSignal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSignal extends BlockContainer implements IBlockConnective {
	public BlockSignal() {
		super(Material.rock);
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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public int getRenderType() {
		return RTMBlock.renderIdLinePole;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntitySignal();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (world.isRemote) {
			player.openGui(RTMCore.instance, RTMCore.guiIdSelectTileEntityModel, player.worldObj, x, y, z);
		}
		return true;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (!world.isRemote) {
			Block block = this.getRenderBlock(world, x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			world.setBlock(x, y, z, block, meta, 3);
			if (!player.capabilities.isCreativeMode) {
				this.dropBlockAsItem(world, x, y, z, new ItemStack(RTMItem.itemSignal, 1, 0));
			}
		}
		return true;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		ItemStack itemStack = new ItemStack(RTMItem.itemSignal, 1, 0);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntitySignal) {
			((ItemSignal) RTMItem.itemSignal).setModelName(itemStack, ((TileEntitySignal) tileEntity).getModelName());
			((ItemSignal) RTMItem.itemSignal).setModelState(itemStack, ((TileEntitySignal) tileEntity).getResourceState());
		}
		return itemStack;
	}

	@Override
	public boolean canConnect(World world, int x, int y, int z) {
		return true;
	}

	public Block getRenderBlock(IBlockAccess world, int x, int y, int z) {
		TileEntitySignal tile = (TileEntitySignal) world.getTileEntity(x, y, z);
		return tile.getRenderBlock();
	}
}