package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityPole;
import jp.ngt.rtm.item.ItemWithModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class BlockLinePole extends BlockOrnamentBase {

    public BlockLinePole() {
        super(Material.rock);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityPole();
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world != null && !world.isRemote) {
            if (world.getTileEntity(x, y, z) == null) {
                world.setTileEntity(x, y, z, new TileEntityPole());
            }
        }
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

    private ItemStack getItem(int damage) {
        if (this == RTMBlock.linePole) {
            return new ItemStack(RTMItem.itemLinePole, 1, damage);
        } else {
            return new ItemStack(RTMBlock.framework, 1, damage);
        }
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

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = super.getPickBlock(target, world, x, y, z, player);
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (itemStack != null && tileEntity instanceof TileEntityPlaceable && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            ItemWithModel.copyOffsetToItemStack((TileEntityPlaceable) tileEntity, itemStack);
        }

        return itemStack;
    }
}