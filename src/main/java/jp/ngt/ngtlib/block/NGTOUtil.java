package jp.ngt.ngtlib.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class NGTOUtil {
	public static NGTObject copyBlocks(World world, int x, int y, int z, int width, int height, int depth) {
		List<BlockSet> list = new ArrayList<BlockSet>();
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				for (int k = 0; k < depth; ++k) {
					int x0 = x + i;
					int y0 = y + j;
					int z0 = z + k;
					Block block = world.getBlock(x0, y0, z0);
					int meta = world.getBlockMetadata(x0, y0, z0);
					NBTTagCompound nbt = null;
					if (block.hasTileEntity(meta)) {
						TileEntity tile = world.getTileEntity(x0, y0, z0);
						if (tile != null) {
							nbt = new NBTTagCompound();
							tile.writeToNBT(nbt);
						}
					}
					list.add(new BlockSet(block, meta, nbt));
				}
			}
		}
		NGTObject object = NGTObject.createNGTO(list, width, height, depth, x, y, z);
		return object;
	}

	public static void deleteBlocks(World world, int x, int y, int z, int width, int height, int depth, int flag) {
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				for (int k = 0; k < depth; ++k) {
					int x0 = x + i;
					int y0 = y + j;
					int z0 = z + k;
					world.setBlock(x0, y0, z0, Blocks.air, 0, flag);
				}
			}
		}
	}

	public static void setBlocks(World world, int x, int y, int z, NGTObject ngto, int flag) {
		int index = 0;
		for (int i = 0; i < ngto.xSize; ++i) {
			for (int j = 0; j < ngto.ySize; ++j) {
				for (int k = 0; k < ngto.zSize; ++k) {
					int x0 = x + i;
					int y0 = y + j;
					int z0 = z + k;
					BlockSet set = ngto.blockList.get(index);
					setBlock(world, x0, y0, z0, set.block, set.metadata, flag);

					if (set.block.hasTileEntity(set.metadata)) {
						TileEntity tile = world.getTileEntity(x0, y0, z0);
						if (tile != null) {
							setTileEntityData(tile, set.nbt, x0, y0, z0);
						}
					}

					++index;
				}
			}
		}
	}

	private static void setBlock(World world, int x, int y, int z, Block block, int metadata, int flag) {
		if (block instanceof BlockLeavesBase && (metadata < 4 || metadata > 7)) {
			metadata = (metadata & 3) + 4;//4~7:手置き, 8~11:コピペ(消える)
		}

		world.setBlock(x, y, z, block, metadata, flag);

		if (block != Blocks.air) {
			world.func_147451_t(x, y, z);//明るさ更新
		}
	}

	private static void setTileEntityData(TileEntity tile, NBTTagCompound nbt, int x, int y, int z) {
		int prevX = 0;
		int prevY = 0;
		int prevZ = 0;

		if (nbt != null) {
			NBTTagCompound nbt0 = (NBTTagCompound) nbt.copy();
			prevX = nbt0.getInteger("x");
			prevY = nbt0.getInteger("y");
			prevZ = nbt0.getInteger("z");
			nbt0.setInteger("x", x);
			nbt0.setInteger("y", y);
			nbt0.setInteger("z", z);
			tile.readFromNBT(nbt0);
		}

		if (tile instanceof TileEntityCustom) {
			((TileEntityCustom) tile).setPos(x, y, z, prevX, prevY, prevZ);
		} else {
			tile.xCoord = x;
			tile.yCoord = y;
			tile.zCoord = z;
		}
	}
}