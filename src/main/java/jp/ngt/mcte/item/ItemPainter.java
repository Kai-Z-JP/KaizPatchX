package jp.ngt.mcte.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import java.util.List;

public class ItemPainter extends Item {
	public ItemPainter() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		if (!world.isRemote) {
			if (player.isSneaking())//ブロックの詳細をチャットに表示
			{
				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);
				ItemStack stack = new ItemStack(block, 1, meta);
				NGTLog.sendChatMessage(player, stack.getDisplayName() + "(" + Block.blockRegistry.getNameForObject(block) + ", " + meta + ")");
			} else {
				this.onUsingTick(itemStack, player, 0);
			}
		}
		return true;
	}

	@Override
	public void onUsingTick(ItemStack itemStack, EntityPlayer player, int count) {
		if (!player.worldObj.isRemote) {
			PainterSetting setting = PainterSetting.getPainterSettingFromItem(itemStack);
			MovingObjectPosition target = this.getTarget(player, setting.drawMode == 0, false);
			if (target != null && setting != null) {
				this.placeBlocks(setting, player.worldObj, target.blockX, target.blockY, target.blockZ);
			}
		}
	}

	private void placeBlocks(PainterSetting setting, World world, int x, int y, int z) {
		int size = setting.size - 1;
		for (int i = -size; i <= size; ++i) {
			for (int j = -size; j <= size; ++j) {
				for (int k = -size; k <= size; ++k) {
					int x0 = x + i;
					int y0 = y + j;
					int z0 = z + k;
					if ((i * i + j * j + k * k) <= size * size) {
						Block targetBlock = world.getBlock(x0, y0, z0);
						int targetMetadata = world.getBlockMetadata(x0, y0, z0);

						boolean flag1 = setting.fill && targetBlock == Blocks.air;
						boolean flag2 = setting.rewrite && targetBlock != Blocks.bedrock && (setting.rewriteBlock.block == Blocks.air || (setting.rewriteBlock.block == targetBlock && setting.rewriteBlock.metadata == targetMetadata));
						if (flag1 || flag2) {
							world.setBlock(x0, y0, z0, setting.fillBlock.block, setting.fillBlock.metadata, 2);
						}
					}
				}
			}
		}
	}

	private MovingObjectPosition getTarget(EntityPlayer player, boolean par2, boolean selectSide) {
		if (par2) {
			MovingObjectPosition target = BlockUtil.getMOPFromPlayer(player, 128.0D, true);
			if (target != null && target.typeOfHit == MovingObjectType.BLOCK) {
				if (selectSide) {
					if (target.sideHit == 0) {
						--target.blockY;
					} else if (target.sideHit == 1) {
						++target.blockY;
					} else if (target.sideHit == 2) {
						--target.blockZ;
					} else if (target.sideHit == 3) {
						++target.blockZ;
					} else if (target.sideHit == 4) {
						--target.blockX;
					} else if (target.sideHit == 5) {
						++target.blockX;
					}
				}
				return target;
			}
		} else {
			float f = 1.0F;
			float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
			float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
			double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
			double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) f + 1.62D - (double) player.yOffset;
			double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
			Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
			float f3 = MathHelper.cos(-f2 * 0.017453292F - NGTMath.PI);
			float f4 = MathHelper.sin(-f2 * 0.017453292F - NGTMath.PI);
			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
			float f6 = MathHelper.sin(-f1 * 0.017453292F);
			float f7 = f4 * f5;
			float f8 = f3 * f5;
			double distance = 8.0D;
			Vec3 vec31 = vec3.addVector((double) f7 * distance, (double) f6 * distance, (double) f8 * distance);

			int x = MathHelper.floor_double(vec31.xCoord);
			int y = MathHelper.floor_double(vec31.yCoord);
			int z = MathHelper.floor_double(vec31.zCoord);
			if (y > 0) {
				return new MovingObjectPosition(x, y, z, 0, vec31, true);
			}
		}
		return null;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1) {
		return EnumAction.none;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("usage.painter"));
	}
}