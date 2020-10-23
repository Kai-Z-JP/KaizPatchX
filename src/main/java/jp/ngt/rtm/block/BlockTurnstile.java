package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityTurnstile;
import jp.ngt.rtm.item.ItemTicket;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class BlockTurnstile extends BlockMachineBase {
	public BlockTurnstile() {
		super(Material.rock);
		this.setLightOpacity(0);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityTurnstile();
	}

	@Override
	public Item getItemDropped(int par1, Random rand, int par3) {
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
		if (!world.isRemote) {
			this.dropBlockAsItem(world, par2, par3, par4, new ItemStack(RTMItem.installedObject, 1, 12));
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int l = world.getBlockMetadata(x, y, z);
		return isOpen(l) ? null : (l != 2 && l != 0 ? AxisAlignedBB.getBoundingBox((double) x + 0.375D, y, z, (double) x + 0.625D, (float) y + 1.5F, z + 1) : AxisAlignedBB.getBoundingBox(x, y, (float) z + 0.375F, x + 1, (float) y + 1.5F, (float) z + 0.625F));
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		if (side == ForgeDirection.UP || side == ForgeDirection.DOWN) {
			return false;
		} else {
			int l = world.getBlockMetadata(x, y, z) & 3;
			if (l != 2 && l != 0) {
				return side == ForgeDirection.NORTH || side == ForgeDirection.SOUTH;
			} else {
				return side == ForgeDirection.EAST || side == ForgeDirection.WEST;
			}
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int l = world.getBlockMetadata(x, y, z) & 3;
		if (l != 2 && l != 0) {
			this.setBlockBounds(0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F);
		} else {
			this.setBlockBounds(0.0F, 0.0F, 0.375F, 1.0F, 1.0F, 0.625F);
		}
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
		return isOpen(world.getBlockMetadata(x, y, z));
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (!this.clickMachine(world, x, y, z, player)) {
			ItemStack itemStack = player.getCurrentEquippedItem();
			if (itemStack != null && itemStack.getItem() instanceof ItemTicket) {
				this.openGate(world, x, y, z, player);
				if (((ItemTicket) itemStack.getItem()).ticketType != 2) {
					ItemStack itemStack2 = ItemTicket.consumeTicket(itemStack);
					if (!world.isRemote && itemStack2 != null) {
						this.dropBlockAsItem(world, x, y + 1, z, itemStack2);
					}
				}
			}
		}
		return true;
	}

	public void openGate(World world, int x, int y, int z, EntityPlayer player) {
		int meta = world.getBlockMetadata(x, y, z);
		TileEntityTurnstile tile = (TileEntityTurnstile) world.getTileEntity(x, y, z);
		if (!isOpen(meta) && !tile.canThrough()) {
			world.setBlockMetadataWithNotify(x, y, z, meta + 4, 2);//open
			tile.setCount(30);
			tile.onActivate();
		}
	}

	public static boolean isOpen(int par1) {
		return (par1 & 4) != 0;
	}
}