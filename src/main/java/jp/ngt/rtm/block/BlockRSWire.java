package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class BlockRSWire extends Block {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockRSWire() {
		super(Material.circuits);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 0;
	}

	@Override
	public int tickRate(World world) {
		return 4;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta < 6 && this.isGettingPower(world, x, y, z)) {
				world.setBlockMetadataWithNotify(x, y, z, meta + 6, 2);
				this.notifyRSWireChanged(world, x, y, z, true);
			}
		}
	}

    /*private void notifyNeighborChange(World p_150172_1_, int p_150172_2_, int p_150172_3_, int p_150172_4_)
    {
        if (p_150172_1_.getBlock(p_150172_2_, p_150172_3_, p_150172_4_) == this)
        {
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_, p_150172_3_, p_150172_4_, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_ - 1, p_150172_3_, p_150172_4_, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_ + 1, p_150172_3_, p_150172_4_, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_, p_150172_3_, p_150172_4_ - 1, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_, p_150172_3_, p_150172_4_ + 1, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_, p_150172_3_ - 1, p_150172_4_, this);
            p_150172_1_.notifyBlocksOfNeighborChange(p_150172_2_, p_150172_3_ + 1, p_150172_4_, this);
        }
    }*/

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if (!world.isRemote) {
			this.notifyRSWireChanged(world, x, y, z, false);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
		super.breakBlock(world, x, y, z, block, p_149749_6_);

		if (!world.isRemote) {
			this.notifyRSWireChanged(world, x, y, z, false);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			boolean gettingPower = this.isGettingPower(world, x, y, z);
			if (meta < 6) {
				if (gettingPower) {
					world.setBlockMetadataWithNotify(x, y, z, meta + 6, 2);
					this.notifyRSWireChanged(world, x, y, z, true);
				}
			} else {
				if (gettingPower) {
					try {
						if (!world.isBlockTickScheduledThisTick(x, y, z, this)) {
							world.setBlockMetadataWithNotify(x, y, z, meta - 6, 2);
							this.notifyRSWireChanged(world, x, y, z, false);
							world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
						}
					} catch (Throwable throwable) {
						throwable.printStackTrace();
						world.setBlockToAir(x, y, z);
					}
				} else {
					world.setBlockMetadataWithNotify(x, y, z, meta - 6, 2);
				}
			}
		}
	}

	private void notifyRSWireChanged(World world, int x, int y, int z, boolean powered) {
		for (int i = 0; i < 6; ++i) {
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			int x0 = x + direction.offsetX;
			int y0 = y + direction.offsetY;
			int z0 = z + direction.offsetZ;
			Block block = world.getBlock(x0, y0, z0);
			if (block == this) {
				int meta0 = world.getBlockMetadata(x0, y0, z0);
				boolean b0 = this.isPowered(meta0);
				if (powered && !b0) {
					world.setBlockMetadataWithNotify(x0, y0, z0, meta0 + 6, 2);
					this.notifyRSWireChanged(world, x0, y0, z0, true);
				} else if (!powered && b0) {
					world.setBlockMetadataWithNotify(x0, y0, z0, meta0 - 6, 2);
					this.notifyRSWireChanged(world, x0, y0, z0, false);
				}
			} else if (block == Blocks.redstone_wire) {
				boolean b0 = world.getBlockMetadata(x0, y0, z0) == 0;
				if (b0 && powered) {
					world.notifyBlockOfNeighborChange(x0, y0, z0, this);
				} else if (!b0 && !powered) {
					world.notifyBlockOfNeighborChange(x0, y0, z0, this);
				}
			} else {
				world.notifyBlockOfNeighborChange(x0, y0, z0, this);//Block1つのみ
			}
		}
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int p5) {
		return world.getBlockMetadata(x, y, z) < 6 ? 0 : 15;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int p5) {
		return this.isProvidingWeakPower(world, x, y, z, p5);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	private boolean isGettingPower(World world, int x, int y, int z) {
		if (world.getBlockPowerInput(x, y, z) > 0) {
			return true;
		}

		if (world.getStrongestIndirectPower(x, y, z) > 0) {
			return true;
		}

		/*for(int i = 0; i < 6; ++i)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			int x0 = x + direction.offsetX;
			int y0 = y + direction.offsetY;
			int z0 = z + direction.offsetZ;
			if(world.getIndirectPowerLevelTo(x0, y0, z0, i) > 0)
			{
				return true;
			}
		}*/
		return false;
	}

	private boolean isPowered(int metadata) {
		return metadata >= 6;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return (par2 < 6) ? this.icons[0] : this.icons[1];
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.icons = new IIcon[2];
		this.icons[0] = par1IconRegister.registerIcon("rtm:rsWire_off");
		this.icons[1] = par1IconRegister.registerIcon("rtm:rsWire_on");
	}
}