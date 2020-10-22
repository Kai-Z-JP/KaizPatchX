package jp.ngt.mcte.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.item.ItemMiniature;
import jp.ngt.mcte.item.ItemMiniature.MiniatureMode;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class BlockMiniature extends BlockContainer {
	public BlockMiniature() {
		super(Material.rock);
		this.setLightOpacity(0);
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
	public int getMobilityFlag() {
		return 0;//ピストンで押せる(大嘘)
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityMiniature();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		ItemStack stack = player.inventory.getCurrentItem();
		if (stack == null || stack.getItem() != MCTE.itemMiniature) {
			if (!world.isRemote) {
				TileEntityMiniature tile = this.getMiniatureTileEntity(world, x, y, z);
				tile.setRotation((float) NGTMath.normalizeAngle(tile.getRotation() + MCTE.rotationInterval), true);
			}
			return true;
		}
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
		;
	}

	private ItemStack getMiniatureItem(World world, int x, int y, int z) {
		TileEntityMiniature tile = this.getMiniatureTileEntity(world, x, y, z);
		NGTObject ngto = tile.blocksObject;
		float scale = tile.scale;
		float mx = tile.offsetX;
		float my = tile.offsetY;
		float mz = tile.offsetZ;
		MiniatureMode mode = tile.mode;
		MiniatureBlockState state = tile.getMBState();
		return ItemMiniature.createMiniatureItem(ngto, scale, mx, my, mz, mode, state);
	}

	private TileEntityMiniature getMiniatureTileEntity(IBlockAccess world, int x, int y, int z) {
		return (TileEntityMiniature) world.getTileEntity(x, y, z);
	}

	//隣からRS入力
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!world.isRemote) {
			TileEntityMiniature te = this.getMiniatureTileEntity(world, x, y, z);
			te.port.onNeighborBlockChange(te);
		}
	}

	/***********************************************************************************************/

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().hardness;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB pbb, List list, Entity p_149743_7_) {
		List<AxisAlignedBB> aabbList = this.getMiniatureTileEntity(world, x, y, z).getCollisionBoxes(x, y, z);
		if (aabbList.isEmpty()) {
			AxisAlignedBB aabb = this.getCollisionBoundingBoxFromPool(world, x, y, z);

			if (aabb != null && pbb.intersectsWith(aabb)) {
				list.add(aabb);
			}
		} else {
			for (AxisAlignedBB aabb : aabbList) {
				if (aabb != null && pbb.intersectsWith(aabb)) {
					list.add(aabb);
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		TileEntityMiniature tile = this.getMiniatureTileEntity(world, x, y, z);
		return tile == null ? super.getCollisionBoundingBoxFromPool(world, x, y, z) : tile.getSelectBox(x, y, z);
		//return AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		return this.getCollisionBoundingBoxFromPool(world, x, y, z);
		//return AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		//return this.getMiniatureTileEntity(world, x, y, z).state.redstonePower;
		return this.isProvidingStrongPower(world, x, y, z, side);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		TileEntityMiniature te = this.getMiniatureTileEntity(world, x, y, z);
		int power = te.port.isProvidingPower(te, side);
		return power > 0 ? power : this.getMiniatureTileEntity(world, x, y, z).getMBState().redstonePower;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return false;//隣接ブロックからのの信号を使うか
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().lightValue;
	}

	/**
	 * Entityの中心座標がブロック内にないと意味なし->living.onLadder()
	 */
	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().isLadder();
	}

	@Override
	public boolean isBurning(IBlockAccess world, int x, int y, int z) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().isBurning();
	}

	@Override
	public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().isFireSource();
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(this.getMiniatureItem(world, x, y, z));
		return ret;
	}

	@Override
	public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().isBed();
	}

	@Override
	public ChunkCoordinates getBedSpawnPosition(IBlockAccess world, int x, int y, int z, EntityPlayer player) {
		if (world instanceof World) {
			int p4 = 0;
			int l1 = x - 1;
			int i2 = z - 1;
			int j2 = l1 + 2;
			int k2 = i2 + 2;

			for (int l2 = l1; l2 <= j2; ++l2) {
				for (int i3 = i2; i3 <= k2; ++i3) {
					if (World.doesBlockHaveSolidTopSurface(world, l2, y - 1, i3) && !world.getBlock(l2, y, i3).getMaterial().isOpaque() && !world.getBlock(l2, y + 1, i3).getMaterial().isOpaque()) {
						if (p4 <= 0) {
							return new ChunkCoordinates(l2, y, i3);
						}
						--p4;
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getBedDirection(IBlockAccess world, int x, int y, int z) {
		return 0;
	}

	@Override
	public boolean isBedFoot(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return this.getMiniatureTileEntity(world, x, y, z).getMBState().explosionResistance;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return this.isProvidingStrongPower(world, x, y, z, side) > 0;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return this.getMiniatureItem(world, x, y, z);
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return false;
	}
}