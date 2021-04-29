package jp.ngt.ngtlib.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

@SideOnly(Side.CLIENT)
public class NGTBlockAccess implements IBlockAccessNGT {
    private final World world;
    private final NGTObject blockObject;

    public NGTBlockAccess(World par1, NGTObject par2) {
        this.world = par1;
        this.blockObject = par2;
    }

    @Override
    public BlockSet getBlockSet(int x, int y, int z) {
        return this.blockObject.getBlockSet(x, y, z);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return this.getBlockSet(x, y, z).block;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        BlockSet set = this.getBlockSet(x, y, z);
        if (set.block.hasTileEntity(set.metadata)) {
            TileEntity tile = set.block.createTileEntity(this.world, set.metadata);
            if (set.nbt != null) {
                tile.readFromNBT(set.nbt);
            }
            return tile;
        }
        return null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int _default) {
        int i1 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int j1 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        if (j1 < _default) {
            j1 = _default;
        }

        return i1 << 20 | j1 << 4;
    }

    private int getSkyBlockTypeBrightness(EnumSkyBlock skyBlock, int x, int y, int z) {
        if (y < 0) {
            y = 0;
        }

        if (y >= this.blockObject.ySize) {
            return skyBlock.defaultLightValue;
        } else if (x >= 0 && z >= 0 && x < this.blockObject.xSize && z < this.blockObject.zSize) {
            int l;
            int i1;

            if (this.getBlock(x, y, z).getUseNeighborBrightness()) {
                l = this.getSpecialBlockBrightness(skyBlock, x, y + 1, z);
                i1 = this.getSpecialBlockBrightness(skyBlock, x + 1, y, z);
                int j1 = this.getSpecialBlockBrightness(skyBlock, x - 1, y, z);
                int k1 = this.getSpecialBlockBrightness(skyBlock, x, y, z + 1);
                int l1 = this.getSpecialBlockBrightness(skyBlock, x, y, z - 1);

                if (i1 > l) {
                    l = i1;
                }

                if (j1 > l) {
                    l = j1;
                }

                if (k1 > l) {
                    l = k1;
                }

                if (l1 > l) {
                    l = l1;
                }

                return l;
            }
        }

        return skyBlock.defaultLightValue;
    }

    private int getSpecialBlockBrightness(EnumSkyBlock skyBlock, int x, int y, int z) {
        return this.canBlockSeeTheSky(x, y, z) ? skyBlock.defaultLightValue : 0;
    }

    private boolean canBlockSeeTheSky(int x, int y, int z) {
        int y0 = y + 1;
        while (y0 < this.blockObject.ySize) {
            if (this.getBlock(x, y0, z) != Blocks.air) {
                return false;
            }
            ++y0;
        }
        return true;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        BlockSet set = this.getBlockSet(x, y, z);
        if (set.block == Blocks.air) {
            return 15;//明るさ変わらず
        }
        return set.metadata;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
        return 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return this.getBlockSet(x, y, z).block == Blocks.air;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return BiomeGenBase.ocean;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return this.getBlockSet(x, y, z).block.isSideSolid(this, x, y, z, side);
    }
}