package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityPipe;
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

public class ItemPipe extends Item {
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    private static final int num = 4;

    public ItemPipe() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10) {
        if (!world.isRemote) {
            Block block = null;
            int meta = itemstack.getItemDamage();
            byte dir = 0;

            if (side == 0)//up
            {
                --y;
                dir = 1;
            } else if (side == 1)//down
            {
                ++y;
                dir = 1;
            } else if (side == 2)//south
            {
                --z;
                dir = 2;
            } else if (side == 3)//north
            {
                ++z;
                dir = 2;
            } else if (side == 4)//east
            {
                --x;
                dir = 0;
            } else if (side == 5)//west
            {
                ++x;
                dir = 0;
            }

            if (!world.isAirBlock(x, y, z)) {
                return true;
            }

            if (meta <= 3) {
                world.setBlock(x, y, z, RTMBlock.pipe, meta, 2);
                world.notifyBlocksOfNeighborChange(x, y, z, RTMBlock.pipe);
                TileEntityPipe tile = (TileEntityPipe) world.getTileEntity(x, y, z);
                tile.setDirection(dir);
                tile.searchConnection();
                block = RTMBlock.pipe;
            }

            if (block != null) {
                world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --itemstack.stackSize;
            }
        }
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        int i = MathHelper.clamp_int(itemstack.getItemDamage(), 0, num - 1);
        return super.getUnlocalizedName() + "." + itemstack.getItemDamage();
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
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IIconRegister) {
        this.iconArray = new IIcon[num];
        this.iconArray[0] = par1IIconRegister.registerIcon("rtm:itemPipe");
        this.iconArray[1] = par1IIconRegister.registerIcon("rtm:itemPipe");
        this.iconArray[2] = par1IIconRegister.registerIcon("rtm:itemPipe");
        this.iconArray[3] = par1IIconRegister.registerIcon("rtm:itemPipe");
    }
}