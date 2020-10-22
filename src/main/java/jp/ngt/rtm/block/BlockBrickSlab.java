package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockBrickSlab extends BlockSlab//ItemSlab.class
{
	public static final String[] names = new String[]{"rtm:fireBrick"};

	/**
	 * @param par1 DoubleSlabはtrue
	 */
	public BlockBrickSlab(boolean par1) {
		super(par1, RTMMaterial.fireproof);
		if (!par1) {
			this.setLightOpacity(0);
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		//return this.field_150004_a ? meta : (side != 0 && (side == 1 || (double)hitY <= 0.5D) ? meta : meta | 8);
		return this.field_150004_a ? meta : (side != 0 && (side == 1 || (double) hitY <= 0.5D) ? meta : (meta + 8) & 15);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int meta = world.getBlockMetadata(x, y, z);
		if ((side >= 2 && ((meta >= 8 && hitY < 0.5F) || (meta < 8 && hitY > 0.5F))) || (meta >= 8 && side == 0) || (meta < 8 && side == 1)) {
			if (world.getBlock(x, y, z) == RTMBlock.brickSlab) {
				ItemStack itemstack = player.inventory.getCurrentItem();
				if (itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == RTMBlock.brickSlab) {
					if (world.isRemote) {
						return true;
					} else {
						world.setBlock(x, y, z, RTMBlock.brickDoubleSlab, 0, 2);
						return true;
					}
				}
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
		return RTMBlock.fireBrick.getBlockTextureFromSide(p_149691_1_);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		;
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(RTMBlock.brickSlab);
	}

	@Override
	protected ItemStack createStackedBlock(int p_149644_1_) {
		return new ItemStack(Item.getItemFromBlock(RTMBlock.brickSlab), 2, p_149644_1_ & 7);
	}

	@Override
	public String func_150002_b(int p_150002_1_) {
		if (p_150002_1_ < 0 || p_150002_1_ >= names.length) {
			p_150002_1_ = 0;
		}
		return super.getUnlocalizedName() + "." + names[p_150002_1_];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		if (!this.field_150004_a) {
			list.add(new ItemStack(Item.getItemFromBlock(RTMBlock.brickSlab), 1, 0));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
		return Item.getItemFromBlock(RTMBlock.brickSlab);
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