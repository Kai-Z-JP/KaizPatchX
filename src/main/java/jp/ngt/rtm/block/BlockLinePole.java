package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BlockLinePole extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] icon;
    @SideOnly(Side.CLIENT)
    private IIcon icon_top;

    public BlockLinePole() {
        super(Material.rock);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
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
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public int getRenderType() {
        return RTMBlock.renderIdLinePole;
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return null;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, x, y, z, this.getItem(par5));
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack par6ItemStack) {
        int meta = par6ItemStack.getItemDamage();
        world.setBlock(x, y, z, this, meta, 3);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getItem(meta);
    }

    private ItemStack getItem(int damage) {
        if (this == RTMBlock.linePole) {
            return new ItemStack(RTMItem.itemLinePole, 1, damage);
        } else {
            return new ItemStack(RTMBlock.framework, 1, damage);
        }
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs tab, List list) {
        if (this == RTMBlock.linePole) {
        } else {
            IntStream.range(0, 16).mapToObj(i -> new ItemStack(par1, 1, i)).forEach(list::add);
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int par1, int par2) {
        if (this == RTMBlock.linePole) {
            return (par1 == 0 || par1 == 1) ? this.icon_top : this.blockIcon;
        } else {
            return this.blockIcon;
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        if (this == RTMBlock.linePole) {
            this.icon = new IIcon[4];
            this.icon[0] = register.registerIcon("rtm:linePoleBase_0");
            this.icon[1] = register.registerIcon("rtm:linePoleBase_1");
            this.icon[2] = register.registerIcon("rtm:linePoleBase_0");
            this.icon[3] = register.registerIcon("rtm:linePoleBase_3");
            this.icon_top = register.registerIcon("rtm:linePole_top");
            this.blockIcon = register.registerIcon("rtm:linePole_side");
        } else {
            this.blockIcon = register.registerIcon("rtm:framework");
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getLinePoleIcon(int meta) {
        meta = MathHelper.clamp_int(meta, 0, 3);
        return this.icon[meta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int par1) {
        return MapColor.getMapColorForBlockColored(par1).colorValue;//BlockColored
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (this == RTMBlock.framework) {
            int meta = world.getBlockMetadata(x, y, z);
            return this.getRenderColor(meta);
        }
        return super.colorMultiplier(world, x, y, z);
    }

    /**
     * 接続先の座標を指定
     *
     * @param connectOther 他の不透明ブロックとつながるか
     */
    public static boolean isConnected(IBlockAccess world, int x, int y, int z, boolean connectOther) {
        Block block = world.getBlock(x, y, z);
        return block == RTMBlock.linePole || block == RTMBlock.framework || block == RTMBlock.signal || (connectOther && block.isOpaqueCube());
    }
}