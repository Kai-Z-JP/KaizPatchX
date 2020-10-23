package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityPaint;
import jp.ngt.rtm.item.PaintProperty.EnumPaintType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.List;

public class ItemPaintTool extends Item {
	public ItemPaintTool() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if (player.isSneaking()) {
			if (world.isRemote) {
				player.openGui(RTMCore.instance, RTMCore.guiIdPaintTool, world, player.getEntityId(), 0, 0);
			}
		} else {
			player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
		}
		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int dir, float fx, float fy, float fz) {
		return false;
	}

	/**
	 * ServerOnly
	 */
	public void usePaintTool(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int dir, float fx, float fy, float fz) {
		PaintProperty prop = PaintProperty.getProperty(itemStack);
		int[] ia = BlockUtil.facing[dir];
		int bx = (int) (fx * 16.0F);
		int by = (int) (fy * 16.0F);
		int bz = (int) (fz * 16.0F);
		int maxX = (bx + prop.radius) * (1 - ia[0]);
		int maxY = (by + prop.radius) * (1 - ia[1]);
		int maxZ = (bz + prop.radius) * (1 - ia[2]);
		int minX = (bx - prop.radius) * (1 - ia[0]);
		int minY = (by - prop.radius) * (1 - ia[1]);
		int minZ = (bz - prop.radius) * (1 - ia[2]);

		int i = minX >= 0 ? (minX >> 4) : (-(Math.abs(minX - 1) >> 4) - 1);
		for (; i <= maxX >> 4; ++i) {
			int j = minY >= 0 ? (minY >> 4) : (-(Math.abs(minY - 1) >> 4) - 1);
			for (; j <= maxY >> 4; ++j) {
				int k = minZ >= 0 ? (minZ >> 4) : (-(Math.abs(minZ - 1) >> 4) - 1);
				for (; k <= maxZ >> 4; ++k) {
					this.paint(world, x + i, y + j, z + k, dir, bx - (i << 4), by - (j << 4), bz - (k << 4), prop);
				}
			}
		}

		/*if(!player.capabilities.isCreativeMode)
		{
			if(itemStack.isItemStackDamageable())
            {
				itemStack.setItemDamage(itemStack.getItemDamage() + 1);
            }
		}*/
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		//EntityRenderer.getMouseOver()
		double blockReachDistance = 7.0;//PlayerControllerMP.getBlockReachDistance()
		MovingObjectPosition mop = BlockUtil.getMOPFromPlayer(player, blockReachDistance, false);
		if (mop != null) {
			float fx = (float) mop.hitVec.xCoord - (float) mop.blockX;
			float fy = (float) mop.hitVec.yCoord - (float) mop.blockY;
			float fz = (float) mop.hitVec.zCoord - (float) mop.blockZ;
			this.usePaintTool(stack, player, player.worldObj, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, fx, fy, fz);
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int count) {
	}

	private void paint(World world, int x, int y, int z, int dir, int paintX, int paintY, int paintZ, PaintProperty prop) {
		if (!world.getBlock(x, y, z).isOpaqueCube()) {
			return;
		}

		int[] ia = BlockUtil.facing[dir];
		x += ia[0];
		y += ia[1];
		z += ia[2];
		Block block = world.getBlock(x, y, z);
		if (block.isAir(world, x, y, z)) {
			world.setBlock(x, y, z, RTMBlock.paint, 0, 3);
		} else if (block != RTMBlock.paint) {
			return;
		}

		int p1 = -1;
		int p2 = -1;
		switch (dir) {
			case 0:
			case 1:
				p1 = paintX;
				p2 = paintZ;
				break;
			case 2:
			case 3:
				p1 = paintX;
				p2 = paintY;
				break;
			case 4:
			case 5:
				p1 = paintY;
				p2 = paintZ;
				break;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityPaint) {
			TileEntityPaint paint = (TileEntityPaint) tile;
			for (int i = 0; i < 16; ++i)//前の位置記憶して間を補完
			{
				for (int j = 0; j < 16; ++j) {
					EnumPaintType type = EnumPaintType.values()[prop.type];
					boolean flag = false;
					int alpha = prop.alpha;
					int r = prop.radius - 1;
					if (type == EnumPaintType.pen_circle || type == EnumPaintType.brush || type == EnumPaintType.eraser_circle) {
						int disQ = (i - p1) * (i - p1) + (j - p2) * (j - p2);
						int rad2 = r * r;
						flag = disQ <= rad2;

						if (type == EnumPaintType.brush && rad2 > 0) {
							alpha -= (alpha * disQ / rad2);
						}
					} else if (type == EnumPaintType.pen_square || type == EnumPaintType.eraser_square) {
						flag = Math.abs(i - p1) <= r && Math.abs(j - p2) <= r;
					}

					if (flag) {
						if (type == EnumPaintType.eraser_circle || type == EnumPaintType.eraser_square) {
							paint.clearColor(i, j, dir);
						} else {
							paint.setColor(prop.color, alpha, i, j, dir);
						}
					}
				}
			}
			paint.markDirty();
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.none;
	}

	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		//list.add(EnumChatFormatting.GRAY + "C:" + String.valueOf(0));
	}
}