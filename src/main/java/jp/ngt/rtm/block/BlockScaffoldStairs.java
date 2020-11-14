package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityScaffold;
import jp.ngt.rtm.block.tileentity.TileEntityScaffoldStairs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.IntStream;

public class BlockScaffoldStairs extends BlockContainer {
	/**
	 * @param par1 階段の元のブロック
	 */
	public BlockScaffoldStairs(Block par1) {
		super(par1.getMaterial());
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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public int getRenderType() {
		return RTMBlock.renderIdScaffoldStairs;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityScaffoldStairs();
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		IntStream.range(0, 16).mapToObj(i -> new ItemStack(par1, 1, i)).forEach(par3List::add);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack) {
		int meta = itemstack.getItemDamage();
		int playerFacing = (MathHelper.floor_double((NGTMath.normalizeAngle(entityliving.rotationYaw + 180.0D) / 90D) + 0.5D) & 3);
		world.setBlock(x, y, z, this, meta, 3);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityScaffold) {
			((TileEntityScaffold) tile).setDir((byte) playerFacing);
		}
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityScaffold) {
			byte dir = ((TileEntityScaffold) tile).getDir();
			byte flag0 = getConnectionType(world, x + 1, y, z, dir);
			byte flag1 = getConnectionType(world, x - 1, y, z, dir);
			byte flag2 = getConnectionType(world, x, y, z + 1, dir);
			byte flag3 = getConnectionType(world, x, y, z - 1, dir);

			if (dir == 0 || dir == 2)//Z
			{
				if (flag1 != 3) {
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0625F, 2.0F, 1.0F);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				}

				if (flag0 != 3) {
					this.setBlockBounds(0.9375F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				}

				IntStream.range(0, 4).forEach(i -> {
					float f0 = i * 0.25F;
					float f1 = (dir == 2) ? f0 : 0.75F - f0;
					this.setBlockBounds(0.0F, 0.0F + f0, f1, 1.0F, 0.25F + f0, 0.25F + f1);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				});
			} else//X
			{
				if (flag3 != 3) {
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0625F);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				}

				if (flag2 != 3) {
					this.setBlockBounds(0.0F, 0.0F, 0.9375F, 1.0F, 2.0F, 1.0F);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				}

				IntStream.range(0, 4).forEach(i -> {
					float f0 = i * 0.25F;
					float f1 = (dir == 1) ? f0 : 0.75F - f0;
					this.setBlockBounds(f1, 0.0F + f0, 0.0F, 0.25F + f1, 0.25F + f0, 1.0F);
					super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
				});
			}

			this.setBlockBoundsForItemRender();
		} else {
			this.setBlockBoundsForItemRender();
			super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int par1) {
		return MapColor.getMapColorForBlockColored(par1).colorValue;//BlockColored
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		if (this == RTMBlock.framework) {
			int meta = world.getBlockMetadata(x, y, z);
			return this.getRenderColor(meta);
		}
		return super.colorMultiplier(world, x, y, z);
	}

	/**
	 * @return なし:0,  足場Z:1, 足場X:2, 階段:3, 立方体:4
	 */
	public static byte getConnectionType(IBlockAccess world, int x, int y, int z, byte dir) {
		Block block0 = world.getBlock(x, y, z);
		Block block1 = world.getBlock(x, y - 1, z);

		if (block0 == RTMBlock.scaffold) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityScaffold) {
				boolean b0 = ((TileEntityScaffold) tile).getDir() == 0;
				return (byte) (b0 ? 1 : 2);
			}
			return 0;
		} else if (block0 == RTMBlock.scaffoldStairs || block1 == RTMBlock.scaffoldStairs) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityScaffoldStairs) {
				if (((TileEntityScaffoldStairs) tile).getDir() == dir) {
					return 3;
				}
			}
			return 0;
		} else if (block0.isOpaqueCube()) {
			return 4;
		} else {
			return 0;
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
		if (!world.isRemote) {
			this.dropBlockAsItem(world, x, y, z, this.getItem(par5));
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return this.getItem(meta);
	}

	private ItemStack getItem(int damage) {
		return new ItemStack(Item.getItemFromBlock(this), 1, damage);
	}
}