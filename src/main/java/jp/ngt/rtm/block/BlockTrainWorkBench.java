package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityTrainWorkBench;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrainWorkBench extends BlockContainer {
	@SideOnly(Side.CLIENT)
	private IIcon[] icon_side;
	@SideOnly(Side.CLIENT)
	private IIcon icon_top;
	@SideOnly(Side.CLIENT)
	private IIcon[] icon_front;

	public BlockTrainWorkBench() {
		super(Material.rock);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityTrainWorkBench();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (!world.isRemote) {
			player.openGui(RTMCore.instance, RTMCore.instance.guiIdTrainWorkBench, world, x, y, z);
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		int i0 = MathHelper.clamp_int(metadata, 0, 1);
		return side == 1 ? this.icon_top : (side == 0 ? Blocks.iron_block.getBlockTextureFromSide(side) : (side != 5 && side != 4 ? this.icon_side[i0] : this.icon_front[i0]));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1) {
		this.icon_side = new IIcon[2];
		this.icon_side[0] = par1.registerIcon(this.getTextureName() + "_side_0");
		this.icon_side[1] = par1.registerIcon(this.getTextureName() + "_side_1");
		this.icon_top = par1.registerIcon(this.getTextureName() + "_top");
		this.icon_front = new IIcon[2];
		this.icon_front[0] = par1.registerIcon(this.getTextureName() + "_front_0");
		this.icon_front[1] = par1.registerIcon(this.getTextureName() + "_front_1");
	}
}