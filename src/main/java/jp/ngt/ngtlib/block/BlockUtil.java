package jp.ngt.ngtlib.block;

import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public final class BlockUtil {
    //util.Facing
    public static final int[][] facing = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    public static final int[][] field_01 = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    public static Block[] getConnectedBlock(IBlockAccess world, int x, int y, int z) {
        return new Block[]{world.getBlock(x + 1, y, z), world.getBlock(x - 1, y, z), world.getBlock(x, y + 1, z), world.getBlock(x, y - 1, z), world.getBlock(x, y, z + 1), world.getBlock(x, y, z - 1)};
    }

    public static Object[][] getConnectedBlockAndMetadata(IBlockAccess world, int x, int y, int z) {
        return new Object[][]{{world.getBlock(x + 1, y, z), world.getBlockMetadata(x + 1, y, z)},
                {world.getBlock(x - 1, y, z), world.getBlockMetadata(x - 1, y, z)},
                {world.getBlock(x, y + 1, z), world.getBlockMetadata(x, y + 1, z)},
                {world.getBlock(x, y - 1, z), world.getBlockMetadata(x, y - 1, z)},
                {world.getBlock(x, y, z + 1), world.getBlockMetadata(x, y, z + 1)},
                {world.getBlock(x, y, z - 1), world.getBlockMetadata(x, y, z - 1)}};
    }

    public static boolean[] isConnectedBlock(IBlockAccess world, Block[] blocks, int x, int y, int z) {
        boolean[] b0 = new boolean[6];
        Block[] connected = getConnectedBlock(world, x, y, z);
        for (int i0 = 0; i0 < 6; ++i0) {
            int i2 = 0;
            for (Block block : blocks) {
                i2 += (connected[i0] == block ? 1 : 0);
            }
            b0[i0] = (i2 > 0);
        }
        return b0;
    }

    public static boolean[] isConnectedBlock(IBlockAccess world, Object[][] blocks, int x, int y, int z) {
        boolean[] b0 = new boolean[6];
        for (int i0 = 0; i0 < 6; ++i0) {
            int i2 = 0;
            for (Object[] block : blocks) {
                boolean flag1 = block[0].equals(world.getBlock(x + field_01[i0][0], y + field_01[i0][1], z + field_01[i0][2]));
                boolean flag2 = block[1].equals(-1) || block[1].equals(world.getBlockMetadata(x + field_01[i0][0], y + field_01[i0][1], z + field_01[i0][2]));
                if (flag1 && flag2) {
                    b0[i0] = true;
                    break;
                }
            }
        }
        return b0;
    }

    public static boolean[] isConnectedSolid(IBlockAccess world, int x, int y, int z) {
        return new boolean[]{world.isSideSolid(x + 1, y, z, ForgeDirection.WEST, true),
                world.isSideSolid(x - 1, y, z, ForgeDirection.EAST, true),
                world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN, true),
                world.isSideSolid(x, y - 1, z, ForgeDirection.UP, true),
                world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH, true),
                world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH, true)};
    }

    public static List<int[]> getBlockList(IBlockAccess world, int x, int y, int z, Block block, int range) {
        List<int[]> array = new ArrayList<>();
        int r2 = range * 2;
        for (int i0 = 0; i0 < r2; ++i0) {
            for (int i1 = 0; i1 < r2; ++i1) {
                for (int i2 = 0; i2 < r2; ++i2) {
                    if (world.getBlock(x - range + i0, y - range + i1, z - range + i2) == block && !(i0 == range && i1 == range && i2 == range)) {
                        array.add(new int[]{x - range + i0, y - range + i1, z - range + i2});
                    }
                }
            }
        }
        return array;
    }

    public static int[] rotateBlockPos(byte rotation, int x, int y, int z) {
        if (rotation == 1) {
            return new int[]{-z, y, x};
        } else if (rotation == 2) {
            return new int[]{-x, y, -z};
        } else if (rotation == 3) {
            return new int[]{z, y, -x};
        }
        return new int[]{x, y, z};
    }

    /**
     * @param player
     * @param distance 視線の距離(default:5.0)
     * @param liquid   液体を含める
     * @return 視線の先にあるブロック(null有り)
     */
    public static MovingObjectPosition getMOPFromPlayer(EntityPlayer player, double distance, boolean liquid) {
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
        Vec3 vec31 = vec3.addVector((double) f7 * distance, (double) f6 * distance, (double) f8 * distance);
        return player.worldObj.rayTraceBlocks(vec3, vec31, liquid);
    }
}