package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityRailroadSign;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRailroadSign extends BlockContainer {
	public BlockRailroadSign() {
		super(Material.circuits);
		this.setBlockBounds(0.0625F * 7.0F, 0.0F, 0.0625F * 7.0F, 0.0625F * 9.0F, 1.5F, 0.0625F * 9.0F);
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
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityRailroadSign();
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		if (world.getBlock(x, y + 1, z) != Blocks.air) {
			this.setBlockBounds(0.0625F * 7.0F, -0.5F, 0.0625F * 7.0F, 0.0625F * 9.0F, 1.0F, 0.0625F * 9.0F);
		} else {
			this.setBlockBounds(0.0625F * 7.0F, 0.0F, 0.0625F * 7.0F, 0.0625F * 9.0F, 1.5F, 0.0625F * 9.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (world.isRemote) {
			player.openGui(RTMCore.instance, RTMCore.instance.guiIdSelectTexture, world, x, y, z);
		}
		return true;
	}

	@Override
	public Item getItemDropped(int par1, Random random, int par3) {
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
		if (!world.isRemote) {
			this.dropBlockAsItem(world, par2, par3, par4, new ItemStack(RTMItem.itemRailroadSign, 1, 0));
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(RTMItem.itemRailroadSign, 1, 0);
	}
}