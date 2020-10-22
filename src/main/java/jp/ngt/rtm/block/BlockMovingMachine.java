package jp.ngt.rtm.block;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import jp.ngt.rtm.block.tileentity.TileEntityTurnplate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMovingMachine extends BlockContainer {
	public BlockMovingMachine() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new TileEntityMovingMachine();
			case 1:
				return new TileEntityTurnplate();
			default:
				return new TileEntityMovingMachine();
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == 0) {
			TileEntityMovingMachine tile = (TileEntityMovingMachine) world.getTileEntity(x, y, z);
			if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
				if (!world.isRemote) {
					if (!tile.hasPair()) {
						tile.searchMM(x, y, z);
					}
				}
			} else {
				if (world.isRemote) {
					TileEntityMovingMachine core = tile.getCore();
					player.openGui(RTMCore.instance, RTMCore.guiIdMovingMachine, world, core.xCoord, core.yCoord, core.zCoord);
				}
			}
		} else if (meta == 1) {
			if (world.isRemote) {
				TileEntityTurnplate tile = (TileEntityTurnplate) world.getTileEntity(x, y, z);
				player.openGui(RTMCore.instance, RTMCore.guiIdTurnplate, world, tile.xCoord, tile.yCoord, tile.zCoord);
			}
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta == 0) {
				TileEntityMovingMachine tile = (TileEntityMovingMachine) world.getTileEntity(x, y, z);
				tile.onBlockChanged();
			} else if (meta == 1) {
				TileEntityTurnplate tile = (TileEntityTurnplate) world.getTileEntity(x, y, z);
				tile.onBlockChanged();
			}
		}
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta == 0) {
				TileEntityMovingMachine tile = (TileEntityMovingMachine) world.getTileEntity(x, y, z);
				tile.reset(true);
			} else if (meta == 1) {
				TileEntityTurnplate tile = (TileEntityTurnplate) world.getTileEntity(x, y, z);
				tile.removed();
			}
		}
		return super.removedByPlayer(world, player, x, y, z, willHarvest);
	}
}