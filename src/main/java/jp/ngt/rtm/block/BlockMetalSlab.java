package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMMaterial;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockMetalSlab extends Block {
	public BlockMetalSlab() {
		super(RTMMaterial.fireproof);
		this.setLightOpacity(0);
		this.setTickRandomly(true);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
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
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (world.getBlockMetadata(x, y, z) > 0) {
			entity.attackEntityFrom(DamageSource.lava, 1.0F);
			entity.setFire(1);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta > 0) {
				world.setBlockMetadataWithNotify(x, y, z, --meta, 2);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		int c0 = (meta << 4) & 255;
		int c1 = 0xffffff - (c0 << 8) - c0;
		return c1;
	}

	@Override
	public String getHarvestTool(int metadata)//Material != rockのとき必須？
	{
		return "";
	}

	@Override
	public int getHarvestLevel(int metadata) {
		return -1;
	}
}