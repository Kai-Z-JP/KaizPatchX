package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityScaffold;
import jp.ngt.rtm.item.ItemInstalledObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockScaffold extends BlockContainer {
    public BlockScaffold() {
        super(Material.rock);
        this.setStepSound(RTMBlock.soundTypeMetal2);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
        this.setBlockBoundsForItemRender();
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
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public int getRenderType() {
        return RTMBlock.renderIdScaffold;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityScaffold();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs tab, List list) {
//        IntStream.range(0, 16).mapToObj(i -> new ItemStack(par1, 1, i)).forEach(list::add);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
        int meta = itemstack.getItemDamage();
        int i0 = (MathHelper.floor_double((NGTMath.normalizeAngle(entity.rotationYaw + 180.0D) / 90D) + 0.5D) & 3);
        int i1 = (i0 == 0 || i0 == 2) ? 0 : 1;
        world.setBlockMetadataWithNotify(x, y, z, meta, 3);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffold) {
            ((TileEntityScaffold) tile).setDir((byte) i1);
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);

        boolean b0 = true;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffold) {
            b0 = ((TileEntityScaffold) tile).getDir() == 0;
        }
        byte flag0 = BlockScaffold.getConnectionType(world, x + 1, y, z);
        byte flag1 = BlockScaffold.getConnectionType(world, x - 1, y, z);
        byte flag2 = BlockScaffold.getConnectionType(world, x, y, z + 1);
        byte flag3 = BlockScaffold.getConnectionType(world, x, y, z - 1);

        if ((b0 && flag0 == 0) || (!b0 && flag0 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3)))//XPos
        {
            this.setBlockBounds(0.9375F, 0.0F, 0.0F, 1.0F, 1.5F, 1.0F);
            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        }

        if ((b0 && flag1 == 0) || (!b0 && flag1 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3)))//XNeg
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0625F, 1.5F, 1.0F);
            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        }

        if ((!b0 && flag2 == 0) || (b0 && flag2 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3)))//ZPos
        {
            this.setBlockBounds(0.0F, 0.0F, 0.9375F, 1.0F, 1.5F, 1.0F);
            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        }

        if ((!b0 && flag3 == 0) || (b0 && flag3 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3)))//ZNeg
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.5F, 0.0625F);
            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        }

        this.setBlockBoundsForItemRender();
    }

    @Override
    public void setBlockBoundsForItemRender() {
        //this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int par1) {
        return MapColor.getMapColorForBlockColored(par1).colorValue;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getRenderColor(meta);
    }

    /**
     * @return なし:0,  足場Z:1, 足場X:2, 階段:3, 立方体:4
     */
    public static byte getConnectionType(IBlockAccess world, int x, int y, int z) {
        Block block0 = world.getBlock(x, y, z);
        Block block1 = world.getBlock(x, y - 1, z);

        if (block0 == RTMBlock.scaffold) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityScaffold) {
                boolean b0 = ((TileEntityScaffold) tile).getDir() == 0;
                return (byte) (b0 ? 1 : 2);
            }
            return 0;
        } else if (block0 == RTMBlock.scaffoldStairs || block1 == RTMBlock.scaffoldStairs) {
            return 3;
        } else if (block0.isOpaqueCube() || block1.isOpaqueCube()) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * @return なし:0,  足場Z:1, 足場X:2, 階段:3, 立方体:4
     */
    public static byte getConnectionType(IBlockAccess world, int x, int y, int z, byte dir) {
        return BlockScaffoldStairs.getConnectionType(world, x, y, z, dir);
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, x, y, z, this.getItem(par5));
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityScaffold) {
            ItemStack itemStack = new ItemStack(RTMItem.installedObject);
            itemStack.setItemDamage(ItemInstalledObject.IstlObjType.SCAFFOLD.id);
            ((ItemInstalledObject) RTMItem.installedObject).setModelName(itemStack, ((TileEntityScaffold) tileEntity).getModelName());
            ((ItemInstalledObject) RTMItem.installedObject).setModelState(itemStack, ((TileEntityScaffold) tileEntity).getResourceState());
            return itemStack;
        }
        return null;
    }

    private ItemStack getItem(int damage) {
        return new ItemStack(Item.getItemFromBlock(this), 1, damage);
    }

    public static float getSpeed(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffold) {
            return ((TileEntityScaffold) tile).getModelSet().getConfig().conveyorSpeed;
        }
        return 0.0F;
    }

    public static void addVecToEntity(Entity entity, Vec3 vec) {
        if (vec.length() > 0.0D && entity.isPushedByWater()) {
            //vec = vec.normalize();
            double d1 = 1.0;//0.014D;
            entity.motionX += vec.getX() * d1;
            entity.motionY += vec.getY() * d1;
            entity.motionZ += vec.getZ() * d1;
        }
    }
}