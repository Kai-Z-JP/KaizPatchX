package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class ItemLinePole extends Item {
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    private static final int num = 4;

    public ItemLinePole() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            int meta = itemStack.getItemDamage();
            int x = par4;
            int y = par5;
            int z = par6;
            Block block;

            if (par7 == 0)//up
            {
                --par5;
            } else if (par7 == 1)//down
            {
                ++par5;
            } else if (par7 == 2)//south
            {
                --par6;
            } else if (par7 == 3)//north
            {
                ++par6;
            } else if (par7 == 4)//east
            {
                --par4;
            } else if (par7 == 5)//west
            {
                ++par4;
            }

            if (!world.isAirBlock(par4, par5, par6)) {
                return true;
            }

            world.setBlock(par4, par5, par6, RTMBlock.linePole, meta, 2);
            block = RTMBlock.linePole;
            world.playSoundEffect((double) par4 + 0.5D, (double) par5 + 0.5D, (double) par6 + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
            --itemStack.stackSize;
        }
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        int i = MathHelper.clamp_int(itemStack.getItemDamage(), 0, num - 1);
        return super.getUnlocalizedName() + "." + itemStack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        int j = MathHelper.clamp_int(par1, 0, num - 1);
        return this.iconArray[j];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int j = 0; j < num; ++j) {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.iconArray = new IIcon[num];
        this.iconArray[0] = register.registerIcon("rtm:itemLinePole_0");//コンクリ
        this.iconArray[1] = register.registerIcon("rtm:itemLinePole_1");//鉄骨
        this.iconArray[2] = register.registerIcon("rtm:itemLinePole_2");//コンクリ2
        this.iconArray[3] = register.registerIcon("rtm:itemLinePole_3");//信号
    }
}