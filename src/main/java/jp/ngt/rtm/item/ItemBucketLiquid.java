package jp.ngt.rtm.item;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;

import java.util.List;

public class ItemBucketLiquid extends Item {
	private static final Block[] blockList = {RTMBlock.liquefiedPigIron, RTMBlock.liquefiedSteel};

	public ItemBucketLiquid() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(15);
		this.setHasSubtypes(true);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, false);//false
		if (mop == null) {
			return itemstack;
		} else {
			if (!itemstack.hasTagCompound()) {
				return itemstack;
			}

			ItemStack itemBlock = ItemStack.loadItemStackFromNBT(itemstack.getTagCompound());
			if (itemBlock == null) {
				return itemstack;
			}

			Block block = Block.getBlockFromItem(itemBlock.getItem());
			if (block == null) {
				return itemstack;
			}

			return this.useBucket(world, player, mop, block, itemstack.getItemDamage() & 15, itemstack, player.capabilities.isCreativeMode);
		}
	}

	/**
	 * FillBucketEvent, もしくは右クリックしたとき呼ばれる
	 *
	 * @param world
	 * @param block    バケツ内のブロック, 空ならBlockAir
	 * @param metadata
	 * @param item     液体入りバケツ or null
	 */
	protected ItemStack useBucket(World world, EntityPlayer player, MovingObjectPosition target, Block block, int metadata, ItemStack item, boolean isCreative) {
		if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			int x = target.blockX;
			int y = target.blockY;
			int z = target.blockZ;

			if (block != Blocks.air) {
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
			}

			if (block == Blocks.air) {
				if (!player.canPlayerEdit(x, y, z, target.sideHit, item)) {
					return item;
				}

				Block block0 = world.getBlock(x, y, z);
				if (block0 instanceof BlockLiquidBase) {
					int meta = world.getBlockMetadata(x, y, z);
					ItemStack itemstack = new ItemStack(RTMItem.bucketLiquid, 1, meta);
					NBTTagCompound nbt = new NBTTagCompound();
					ItemStack itemBlock = new ItemStack(block0);
					itemBlock.writeToNBT(nbt);
					itemstack.setTagCompound(nbt);
					world.setBlock(x, y, z, Blocks.air, 0, 2);
					return itemstack;
				}
			} else {
				if (player.canPlayerEdit(x, y, z, target.sideHit, item)) {
					if (world.setBlock(x, y, z, block, metadata, 3)) {
						return isCreative ? item : new ItemStack(Items.bucket);
					}
				}
			}
		}
		return item;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs tab, List list) {
		for (Block block : blockList) {
			ItemStack itemstack = new ItemStack(RTMItem.bucketLiquid, 1, 15);
			NBTTagCompound nbt = new NBTTagCompound();
			ItemStack itemBlock = new ItemStack(block);
			itemBlock.writeToNBT(nbt);
			itemstack.setTagCompound(nbt);
			list.add(itemstack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		if (itemStack.hasTagCompound()) {
			ItemStack itemBlock = ItemStack.loadItemStackFromNBT(itemStack.getTagCompound());
			if (itemBlock != null) {
				Block block = Block.getBlockFromItem(itemBlock.getItem());
				if (block != null) {
					String name = block.getLocalizedName();
					list.add(EnumChatFormatting.GRAY + String.valueOf(name));
					list.add(EnumChatFormatting.GRAY + String.valueOf(itemStack.getItemDamage() + 1) + "/16");
				}
			}
		}
	}

	@SubscribeEvent
	public void onFillBucket(FillBucketEvent event) {
		if (!event.world.isRemote) {
			ItemStack itemstack = this.useBucket(event.world, event.entityPlayer, event.target, Blocks.air, 0, null, false);
			if (itemstack != null) {
				event.result = itemstack;
				event.setResult(Event.Result.ALLOW);
			}
		}
	}
}