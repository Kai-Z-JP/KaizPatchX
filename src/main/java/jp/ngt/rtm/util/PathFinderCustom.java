package jp.ngt.rtm.util;

import jp.ngt.rtm.block.BlockTurnstile;
import jp.ngt.rtm.rail.BlockLargeRailBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public class PathFinderCustom extends PathFinder {
	protected boolean isWoddenDoorAllowed;
	protected boolean isMovementBlockAllowed;
	protected boolean isPathingInWater;

	public PathFinderCustom(IBlockAccess blockAccess, boolean allowDoor, boolean allowBlock, boolean pathWater, boolean drown) {
		super(blockAccess, allowDoor, allowBlock, pathWater, drown);
		this.isWoddenDoorAllowed = allowDoor;
		this.isMovementBlockAllowed = allowBlock;
		this.isPathingInWater = pathWater;
	}

	@Override
	public int getVerticalOffset(Entity entity, int x, int y, int z, PathPoint point) {
		boolean flag3 = false;

		for (int x0 = x; x0 < x + point.xCoord; ++x0) {
			for (int y0 = y; y0 < y + point.yCoord; ++y0) {
				for (int z0 = z; z0 < z + point.zCoord; ++z0) {
					Block block = entity.worldObj.getBlock(x0, y0, z0);

					if (block.getMaterial() != Material.air) {
						if (block == Blocks.trapdoor) {
							flag3 = true;
						} else if (block != Blocks.flowing_water && block != Blocks.water) {
							if (!this.isWoddenDoorAllowed && block == Blocks.wooden_door) {
								return 0;
							}
						} else {
							if (this.isPathingInWater) {
								return -1;
							}

							flag3 = true;
						}

						int renderType = block.getRenderType();

						if (entity.worldObj.getBlock(x0, y0, z0).getRenderType() == 9) {
							int j2 = MathHelper.floor_double(entity.posX);
							int l1 = MathHelper.floor_double(entity.posY);
							int i2 = MathHelper.floor_double(entity.posZ);

							if (entity.worldObj.getBlock(j2, l1, i2).getRenderType() != 9 && entity.worldObj.getBlock(j2, l1 - 1, i2).getRenderType() != 9) {
								return -3;
							}
						} else if (!block.getBlocksMovement(entity.worldObj, x0, y0, z0) && (!this.isMovementBlockAllowed || block != Blocks.wooden_door)) {
							if (renderType == 11 || block == Blocks.fence_gate || renderType == 32) {
								return -3;
							}

							if (block instanceof BlockTurnstile) {
								return BlockTurnstile.isOpen(entity.worldObj.getBlockMetadata(x0, y0, z0)) ? 1 : -3;
							}

							if (block instanceof BlockLargeRailBase) {
								return ((BlockLargeRailBase) block).preventMobMovement(entity.worldObj, x0, y0, z0) ? -3 : 1;
							}

							if (block == Blocks.trapdoor) {
								return -4;
							}

							Material material = block.getMaterial();

							if (material != Material.lava) {
								return 0;
							}

							if (!entity.handleLavaMovement()) {
								return -2;
							}
						}
					}
				}
			}
		}

		return flag3 ? 2 : 1;
	}

	public static PathEntity getPathEntityToEntity(Entity entity, Entity target, float par3, boolean par4, boolean par5, boolean par6, boolean par7) {
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY + 1.0D);
		int k = MathHelper.floor_double(entity.posZ);
		int l = (int) (par3 + 16.0F);
		int i1 = i - l;
		int j1 = j - l;
		int k1 = k - l;
		int l1 = i + l;
		int i2 = j + l;
		int j2 = k + l;
		ChunkCache chunkcache = new ChunkCache(entity.worldObj, i1, j1, k1, l1, i2, j2, 0);
        return (new PathFinderCustom(chunkcache, par4, par5, par6, par7)).createEntityPathTo(entity, target, par3);
	}

	public static PathEntity getEntityPathToXYZ(Entity entity, int x, int y, int z, float par5, boolean par6, boolean par7, boolean par8, boolean par9) {
		int l = MathHelper.floor_double(entity.posX);
		int i1 = MathHelper.floor_double(entity.posY);
		int j1 = MathHelper.floor_double(entity.posZ);
		int k1 = (int) (par5 + 8.0F);
		int l1 = l - k1;
		int i2 = i1 - k1;
		int j2 = j1 - k1;
		int k2 = l + k1;
		int l2 = i1 + k1;
		int i3 = j1 + k1;
		ChunkCache chunkcache = new ChunkCache(entity.worldObj, l1, i2, j2, k2, l2, i3, 0);
        return (new PathFinderCustom(chunkcache, par6, par7, par8, par9)).createEntityPathTo(entity, x, y, z, par5);
	}
}