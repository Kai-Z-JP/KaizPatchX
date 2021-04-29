package jp.ngt.rtm.rail;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockLargeRailSwitchCore extends BlockLargeRailBase {
    public BlockLargeRailSwitchCore(int par1) {
        super(par1);
        this.setTickRandomly(true);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityLargeRailSwitchCore();
    }

    @Override
    public boolean isCore() {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)//鯖側のみ呼ばれる
    {
		/*if(!world.isRemote)
		{
			TileEntityLargeRailSwitchCore tile = (TileEntityLargeRailSwitchCore)world.getTileEntity(x, y, z);
			tile.onBlockChanged();
		}*/
    }
}