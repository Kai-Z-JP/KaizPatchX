package jp.ngt.rtm.rail;

import jp.ngt.rtm.RTMBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLargeRailSlopeBase extends BlockLargeRailBase {
	public BlockLargeRailSlopeBase(int par1) {
		super(par1);
		this.setBlockBoundsRail(0);
	}

	@Override
	public int getRenderType() {
		return RTMBlock.renderIdBlockRail;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityLargeRailSlopeBase();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		int l = par1World.getBlockMetadata(par2, par3, par4) & 15;
		float f = 0.0625F;
		return AxisAlignedBB.getBoundingBox((double) par2 + this.minX, (double) par3 + this.minY, (double) par4 + this.minZ, (double) par2 + this.maxX, (float) par3 + (float) l * f, (double) par4 + this.maxZ);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		this.setBlockBoundsRail(blockAccess.getBlockMetadata(x, y, z) & 15);
	}

	protected void setBlockBoundsRail(int par1) {
		float f = (float) (1 + par1) * 0.0625F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
	}

	@Override
	public boolean isCore() {
		return false;
	}
}