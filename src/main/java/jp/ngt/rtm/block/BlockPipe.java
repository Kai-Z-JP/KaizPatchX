package jp.ngt.rtm.block;

import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.RTMMaterial;
import jp.ngt.rtm.block.tileentity.TileEntityPipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockPipe extends BlockContainer {
    public BlockPipe() {
        super(RTMMaterial.fireproof);
        this.setLightOpacity(0);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
        this.setStepSound(RTMBlock.soundTypeMetal2);
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
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityPipe();
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return null;
    }

    @Override
    public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!par1World.isRemote) {
            this.dropBlockAsItem(par1World, par2, par3, par4, this.getItem(par5));
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        TileEntityPipe tile = (TileEntityPipe) world.getTileEntity(x, y, z);
        tile.refresh();
    }

    public List<BlockSet> setLiquid(World world, int x, int y, int z, int fromX, int fromY, int fromZ, List<BlockSet> list, int count) {
        if (count > 255) {
            return list;
        }

        int x0;
        int y0;
        int z0;
        //int meta = world.getBlockMetadata(x, y, z);
        //int min = 0;
        //int max = 6;

		/*if(meta % 2 == 0)
		{
			TileEntityPipe tile = (TileEntityPipe)world.getTileEntity(x, y, z);
			byte dir = tile.getDirection();
			switch(dir)
			{
			case 0: min = 4; break;
			case 1: max = 2; break;
			case 2: min = 2; max = 4; break;
			}
		}*/

        TileEntityPipe tile = (TileEntityPipe) world.getTileEntity(x, y, z);
        for (int i = 0; i < 6; ++i) {
            x0 = x + BlockUtil.facing[i][0];
            y0 = y + BlockUtil.facing[i][1];
            z0 = z + BlockUtil.facing[i][2];
            if (!(x0 == fromX && y0 == fromY && z0 == fromZ)) {
                if (tile.connection[i] == 3)//block == RTMBlock.slot)
                {
                    Block block = world.getBlock(x0, y0, z0);
                    int m0 = world.getBlockMetadata(x0, y0, z0);
                    BlockSet bs = new BlockSet(x0, y0, z0, block, m0);
                    if (!list.contains(bs)) {
                        list.add(bs);
                    }
                } else if (tile.connection[i] == 2)//block == RTMBlock.pipe)
                {
                    this.setLiquid(world, x0, y0, z0, x, y, z, list, ++count);
                }
            }
        }
        return list;
    }

    @Override
    public String getHarvestTool(int metadata)//Material != rockのとき必須？
    {
        return "pickaxe";
    }

    @Override
    public int getHarvestLevel(int metadata) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getItem(meta);
    }

    private ItemStack getItem(int damage) {
        return new ItemStack(RTMItem.itemPipe, 1, damage);
    }
}