package jp.ngt.rtm.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSwitch extends Block {
	public BlockSwitch() {
		super(Material.rock);
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta < 4) {
                meta += 4;
            } else {
                meta -= 4;
            }
            world.setBlockMetadataWithNotify(x, y, z, meta, 3);
            world.notifyBlockChange(x, y, z, this);
            world.notifyBlockChange(x, y - 1, z, this);
            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.click", 0.3F, 0.6F);
        }
        return true;
    }

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		this.setBlockBounds(par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 3);
	}

	protected void setBlockBounds(int par1) {
		switch (par1) {

		}
        float f = (float) (1 + par1) / 16.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int p5) {
		return world.getBlockMetadata(x, y, z) < 4 ? 0 : 15;
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