package jp.ngt.rtm.electric;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemInstalledObject.IstlObjType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockConnector extends BlockElectricalWiring {
	public BlockConnector() {
		super(Material.rock);
		this.setLightOpacity(0);
		this.setBlockBounds(0);
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
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityConnector();
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int par2, int par3, int par4) {
		int meta = blockAccess.getBlockMetadata(par2, par3, par4);
		this.setBlockBounds(meta % 6);
	}

	protected void setBlockBounds(int par1) {
		float range = 0.25F;
		float minX = 0.5F - range;
		float minY = 0.5F - range;
		float minZ = 0.5F - range;
		float maxX = 0.5F + range;
		float maxY = 0.5F + range;
		float maxZ = 0.5F + range;
		switch (par1) {
			case 0:
				maxY = 1.0F;
				break;
			case 1:
				minY = 0.0F;
				break;
			case 2:
				maxZ = 1.0F;
				break;
			case 3:
				minZ = 0.0F;
				break;
			case 4:
				maxX = 1.0F;
				break;
			case 5:
				minX = 0.0F;
				break;
		}
		this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(par2, par3, par4);
			this.dropBlockAsItem(world, par2, par3, par4, this.getItem(meta));
		}
	}

	@Override
	protected ItemStack getItem(int damage) {
		damage = damage < 6 ? IstlObjType.CONNECTOR_IN.id : IstlObjType.CONNECTOR_OUT.id;
		return new ItemStack(RTMItem.installedObject, 1, damage);
	}

	@Override
	public boolean canConnect(World world, int x, int y, int z) {
		return true;
	}
}