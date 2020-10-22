package jp.ngt.rtm.rail;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockLargeRailSwitchBase extends BlockLargeRailBase {
	public BlockLargeRailSwitchBase(int par1) {
		super(par1);
		this.setTickRandomly(true);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityLargeRailSwitchBase();
	}

	@Override
	public boolean isCore() {
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)//鯖側のみ呼ばれる
	{
		/*if(!world.isRemote)
		{
			TileEntityLargeRailSwitchBase tile = (TileEntityLargeRailSwitchBase)world.getTileEntity(x, y, z);
			TileEntityLargeRailCore core = tile.getRailCore();
			if(core instanceof TileEntityLargeRailSwitchCore)
			{
				((TileEntityLargeRailSwitchCore)core).onBlockChanged();
			}
		}*/
	}
}