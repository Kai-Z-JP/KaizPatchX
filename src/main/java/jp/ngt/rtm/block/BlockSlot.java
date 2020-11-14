package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMMaterial;
import jp.ngt.rtm.block.tileentity.TileEntitySlot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockSlot extends BlockContainer implements IPipeConnectable {
	@SideOnly(Side.CLIENT)
	protected IIcon icon_front;//吸込口
	@SideOnly(Side.CLIENT)
	protected IIcon icon_back;//排出口

	public BlockSlot() {
		super(RTMMaterial.fireproof);
		this.setTickRandomly(true);
	}

	@Override
	public TileEntity createNewTileEntity(World par1, int par2) {
		return new TileEntitySlot();
	}

	//TileEntityからTick毎呼び出し
	public void inhaleLiquid(World world, int x, int y, int z) {
		int m0 = world.getBlockMetadata(x, y, z);
		int x0 = x + BlockUtil.facing[m0][0];
		int y0 = y + BlockUtil.facing[m0][1];
		int z0 = z + BlockUtil.facing[m0][2];
		Block block = world.getBlock(x0, y0, z0);
		int meta = world.getBlockMetadata(x0, y0, z0);
		if (block instanceof BlockLiquid) {
			if (this.addLiquid(world, x, y, z, block, meta)) {
				world.setBlock(x0, y0, z0, Blocks.air);
			}
		}
	}

	public boolean addLiquid(World world, int x, int y, int z, Block block, int metadata) {
		int m0 = world.getBlockMetadata(x, y, z);
		int x1 = x - BlockUtil.facing[m0][0];
		int y1 = y - BlockUtil.facing[m0][1];
		int z1 = z - BlockUtil.facing[m0][2];
		Block b0 = world.getBlock(x1, y1, z1);

		if (b0 == RTMBlock.pipe) {
            List<BlockSet> list = ((BlockPipe) RTMBlock.pipe).setLiquid(world, x1, y1, z1, x, y, z, new ArrayList<>(), 0);
            while (!list.isEmpty()) {
                BlockSet bs = list.get(world.rand.nextInt(list.size()));
                if (this.setLiquid(world, bs.x, bs.y, bs.z, block, metadata)) {
                    return true;
                }
                list.remove(bs);
            }
            return false;
        } else {
			return this.setLiquid(world, x1, y1, z1, block, metadata);
		}
	}

	protected boolean setLiquid(World world, int x, int y, int z, Block block, int metadata) {
		int m0 = world.getBlockMetadata(x, y, z);
		int x0 = x - BlockUtil.facing[m0][0];
		int y0 = y - BlockUtil.facing[m0][1];
		int z0 = z - BlockUtil.facing[m0][2];
		if (world.getBlock(x0, y0, z0) == Blocks.air) {
			world.setBlock(x0, y0, z0, block, metadata, 2);
			return true;
		} else if (block instanceof BlockLiquidBase) {
			for (int y1 = 1; y1 < 16; ++y1) {
				if (world.getBlock(x0, y0 + y1 - 1, z0) == block && world.getBlock(x0, y0 + y1, z0) == Blocks.air) {
					world.setBlock(x0, y0 + y1, z0, block, metadata, 2);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
		int l = BlockPistonBase.determineOrientation(world, x, y, z, entity);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
		world.notifyBlocksOfNeighborChange(x, y, z, RTMBlock.pipe);
	}

	private boolean isIndirectlyPowered(World world, int x, int y, int z, int dir) {
		return dir != 0 && world.getIndirectPowerOutput(x, y - 1, z, 0) || (dir != 1 && world.getIndirectPowerOutput(x, y + 1, z, 1) || (dir != 2 && world.getIndirectPowerOutput(x, y, z - 1, 2) || (dir != 3 && world.getIndirectPowerOutput(x, y, z + 1, 3) || (dir != 5 && world.getIndirectPowerOutput(x + 1, y, z, 5) || (dir != 4 && world.getIndirectPowerOutput(x - 1, y, z, 4) || (world.getIndirectPowerOutput(x, y, z, 0) || (world.getIndirectPowerOutput(x, y + 2, z, 1) || (world.getIndirectPowerOutput(x, y + 1, z - 1, 2) || (world.getIndirectPowerOutput(x, y + 1, z + 1, 3) || (world.getIndirectPowerOutput(x - 1, y + 1, z, 4) || world.getIndirectPowerOutput(x + 1, y + 1, z, 5)))))))))));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)//0:bottom,1:top,
	{
		int k = meta & 7;
		return side == k ? this.icon_front : (((side == 0 && k == 1) || (side == 1 && k == 0) || (side == 2 && k == 3) || (side == 3 && k == 2) || (side == 4 && k == 5) || (side == 5 && k == 4)) ? this.icon_back : this.blockIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1) {
		this.blockIcon = par1.registerIcon("rtm:slot");
		this.icon_front = par1.registerIcon("rtm:slot_front");
		this.icon_back = par1.registerIcon("rtm:slot_back");
	}

	@Override
	public String getHarvestTool(int metadata)//Material != rockのとき必須？
	{
		return "pickaxe";
	}

	@Override
	public int getHarvestLevel(int metadata) {
		return 0;
	}
}