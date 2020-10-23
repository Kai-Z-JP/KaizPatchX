package jp.ngt.rtm.item;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemPaddle extends Item {
	public ItemPaddle() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(ToolMaterial.IRON.getMaxUses());
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		MovingObjectPosition target = this.getMovingObjectPositionFromPlayer(world, player, false);
		if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
			return itemstack;
		} else {
			int x = target.blockX;
			int y = target.blockY;
			int z = target.blockZ;

			switch (target.sideHit) {
				case 0:
					--y;
					break;
				case 1:
					++y;
					break;
				case 2:
					--z;
					break;
				case 3:
					++z;
					break;
				case 4:
					--x;
					break;
				case 5:
					++x;
					break;
			}

			if (world.getBlock(x, y, z) == RTMBlock.liquefiedPigIron) {
				if (!world.isRemote) {
					if (this.onStirPigIron(world, player, x, y, z)) {
						itemstack.damageItem(1, player);
					}
				}
			}
		}
		return itemstack.copy();//サバイバルで右クリック動作を強制させる
	}

	private boolean onStirPigIron(World world, EntityPlayer player, int x, int y, int z) {
		if (!world.canBlockSeeTheSky(x, y, z)) {
			boolean flag0 = false;//wall
			boolean flag1 = false;//fire
			boolean flag2 = false;
			boolean flag3 = false;
			boolean flag4 = false;
			boolean flag5 = false;

			int maxX = -1;
			int minX = -1;
			int maxZ = -1;
			int minZ = -1;

			for (int i = 0; i < 16; ++i) {
				if (maxX <= 0 && world.getBlock(x + i, y + 1, z) == RTMBlock.fireBrick) {
					maxX = i;
				}

				if (minX <= 0 && world.getBlock(x - i, y + 1, z) == RTMBlock.fireBrick) {
					minX = i;
				}

				if (maxZ <= 0 && world.getBlock(x, y + 1, z + i) == RTMBlock.fireBrick) {
					maxZ = i;
				}

				if (minZ <= 0 && world.getBlock(x, y + 1, z - i) == RTMBlock.fireBrick) {
					minZ = i;
				}

				flag0 = maxX > 0 && minX > 0 && maxZ > 0 && minZ > 0;
				flag1 = flag1 || world.getBlock(x + i, y, z) == Blocks.fire || world.getBlock(x - i, y, z) == Blocks.fire || world.getBlock(x, y, z + i) == Blocks.fire || world.getBlock(x, y, z - i) == Blocks.fire;
			}

			for (int i = 0; i < 16; ++i) {
				if (i < maxX) {
					flag2 = flag2 || world.canBlockSeeTheSky(x + i, y + 1, z);
				}

				if (i < minX) {
					flag3 = flag3 || world.canBlockSeeTheSky(x - i, y + 1, z);
				}

				if (i < maxZ) {
					flag4 = flag4 || world.canBlockSeeTheSky(x, y + 1, z + i);
				}

				if (i < minZ) {
					flag5 = flag5 || world.canBlockSeeTheSky(x, y + 1, z - i);
				}
			}

			if (flag0 && flag1 && (flag2 || flag3 || flag4 || flag5)) {
				int meta = world.getBlockMetadata(x, y, z);
				int difficulty = 8;
				if (world.rand.nextInt(difficulty * (meta + 1)) == 0) {
					world.setBlock(x, y, z, RTMBlock.liquefiedSteel, meta, 2);
					NGTLog.sendChatMessage(player, "message.paddle.get_steel");
					return true;
				}
				return false;
			}
		}
		NGTLog.sendChatMessage(player, "message.paddle.incorrect_design");
		return false;
	}
}