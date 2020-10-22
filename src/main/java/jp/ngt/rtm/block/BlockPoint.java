package jp.ngt.rtm.block;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityPoint;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPoint extends BlockMachineBase {
	public BlockPoint() {
		super(Material.rock);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityPoint();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (!this.clickMachine(world, x, y, z, player)) {
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null && tile instanceof TileEntityPoint) {
				TileEntityPoint point = (TileEntityPoint) tile;
				if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
					if (!world.isRemote) {
						float f0 = point.getMove();
						point.setMove(-f0);
					}
				} else {
					if (!world.isRemote) {
						boolean b0 = point.isActivated();
						point.setActivated(!b0);
						world.notifyBlockChange(x, y, z, this);
						world.notifyBlockChange(x, y - 1, z, this);
					}
				}
				point.onActivate();
				return true;
			}
		}
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int p5) {
		TileEntity tile = world.getTileEntity(x, y, z);
		boolean b = tile != null && tile instanceof TileEntityPoint && ((TileEntityPoint) tile).isActivated();
		return b ? 15 : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int p5) {
		return this.isProvidingWeakPower(world, x, y, z, p5);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}
}