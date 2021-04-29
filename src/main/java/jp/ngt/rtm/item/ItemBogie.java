package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemBogie extends Item {
    @SideOnly(Side.CLIENT)
    private IIcon[] IIconarray;
    private static final int num = 2;

    public ItemBogie() {
        super();
        this.setHasSubtypes(true);
        this.maxStackSize = 16;
    }

	/*@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        RailMap rm0 = TileEntityLargeRailBase.getRailMapFromCoordinates(world, x, y, z);
    	if(rm0 != null)
    	{
    		int i0 = rm0.getNearlestPoint(128, x, z);
	    	float f0 = rm0.getRailRotation(128, i0);

	    	double x0 = rm0.getRailPos(128, i0)[1];
			double y0 = y + 1.0D;
			double z0 = rm0.getRailPos(128, i0)[0];

			EntityBogie bogie = new EntityBogie(world);
			bogie.setPositionAndRotation(x0, y0, z0, f0, 0.0F);
            if (!world.isRemote)
            {
                world.spawnEntityInWorld(bogie);
            }
            --itemStack.stackSize;
			return true;
    	}
        else
        {
            return false;
        }
    }*/

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        int i = MathHelper.clamp_int(itemStack.getItemDamage(), 0, num - 1);
        return super.getUnlocalizedName() + "." + itemStack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        int j = MathHelper.clamp_int(par1, 0, num - 1);
        return this.IIconarray[j];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List list) {
        list.add(new ItemStack(par1, 1, 0));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.IIconarray = new IIcon[num];
        this.IIconarray[0] = register.registerIcon("rtm:bogie");
        this.IIconarray[1] = register.registerIcon("rtm:bogie");
    }
}