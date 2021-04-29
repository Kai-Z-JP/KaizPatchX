package jp.ngt.rtm.item;

import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBellows extends Item {
    public ItemBellows() {
        super();
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (!world.isRemote) {
            if (world.getBlock(x, y, z) == RTMBlock.brickSlab) {
                if (world.rand.nextInt(5) == 0) {
                    int x1 = x - BlockUtil.facing[side][0];
                    int y1 = y - BlockUtil.facing[side][1];
                    int z1 = z - BlockUtil.facing[side][2];
                    boolean b = this.setLiquid(world, x1, y1, z1, RTMBlock.furnaceFire, 0);
                }
            }
        }
        return true;
    }

    protected boolean setLiquid(World world, int x, int y, int z, Block block, int metadata) {
        int m0 = world.getBlockMetadata(x, y, z);
        int x0 = x - BlockUtil.facing[m0][0];
        int y0 = y - BlockUtil.facing[m0][1];
        int z0 = z - BlockUtil.facing[m0][2];
        if (world.getBlock(x0, y0, z0) == Blocks.air) {
            world.setBlock(x0, y0, z0, block, metadata, 2);
            return true;
        } else if (block instanceof BlockLiquidBase) {
            for (int y1 = 1; y1 < 16; ++y1) {
                if (world.getBlock(x0, y0 + y1 - 1, z0) == block && world.getBlock(x0, y0 + y1, z0) == Blocks.air) {
                    world.setBlock(x0, y0 + y1, z0, block, metadata, 2);
                    return true;
                }
            }
        }
        return false;
    }
}