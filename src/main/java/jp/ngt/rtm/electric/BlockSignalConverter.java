package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockSignalConverter extends BlockContainer implements IBlockConnective {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockSignalConverter() {
		super(Material.rock);
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World par1, int par2) {
		return TileEntitySignalConverter.createTileEntity(par2);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack) {
		int meta = itemstack.getItemDamage();
		world.setBlock(x, y, z, this, meta, 3);
	}

	@Override
	public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
		int meta = world.getBlockMetadata(par2, par3, par4);
		if (meta == SignalConverterType.Increment.id || meta == SignalConverterType.Decrement.id) {
			return true;
		} else {
			if (world.isRemote) {
				player.openGui(RTMCore.instance, RTMCore.guiIdSignalConverter, player.worldObj, par2, par3, par4);
			}
			return true;
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int direction) {
		return this.isProvidingStrongPower(world, x, y, z, direction);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int direction) {
		TileEntitySignalConverter tile = (TileEntitySignalConverter) world.getTileEntity(x, y, z);
		return tile.getRSOutput();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs tab, List list) {
		list.add(new ItemStack(par1, 1, SignalConverterType.RSIn.id));
		list.add(new ItemStack(par1, 1, SignalConverterType.RSOut.id));
		list.add(new ItemStack(par1, 1, SignalConverterType.Increment.id));
		list.add(new ItemStack(par1, 1, SignalConverterType.Decrement.id));
		list.add(new ItemStack(par1, 1, SignalConverterType.Wireless.id));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return this.icons[par2];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		this.icons = new IIcon[5];
		this.icons[0] = register.registerIcon("rtm:signalConverter_0");//RS->Sig
		this.icons[1] = register.registerIcon("rtm:signalConverter_1");//Sig->RS
		this.icons[2] = register.registerIcon("rtm:signalConverter_2");//++Sig
		this.icons[3] = register.registerIcon("rtm:signalConverter_3");//--Sig
		this.icons[4] = register.registerIcon("rtm:signalConverter_4");//wireless
	}

	@Override
	public boolean canConnect(World world, int x, int y, int z) {
		return true;
	}
}