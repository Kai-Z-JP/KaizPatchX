package jp.ngt.ngtlib.world;

import jp.ngt.ngtlib.block.BlockSet;
import net.minecraft.world.IBlockAccess;

public interface IBlockAccessNGT extends IBlockAccess {
    BlockSet getBlockSet(int x, int y, int z);
}