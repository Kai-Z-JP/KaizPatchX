package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.RTMMaterial;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.modelpack.modelset.ModelSetRail;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BlockLargeRailBase extends BlockContainer {
    public static final float THICKNESS = 0.0625F;
    /**
     * 2:Gravel, 3:Stone, 4:Snow, 5:Asphalt
     */
    public final int railTextureType;

    public BlockLargeRailBase(int par1) {
        super(RTMMaterial.rail);
        this.setHardness(1.0F);
        this.setLightOpacity(0);
        this.setResistance(15.0F);
        //this.setHarvestLevel("pickaxe", 0);
        this.setStepSound(par1 == 2 ? Block.soundTypeGravel : (par1 == 4 ? Block.soundTypeSnow : Block.soundTypeStone));
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
        this.railTextureType = par1;
    }

    @Override
    public int getRenderType() {
        return RTMBlock.renderIdBlockRail;
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
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityLargeRailBase();
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
        boolean flag = this.preventMobMovement(world, x, y, z) && entity instanceof EntityLiving;
        AxisAlignedBB aabb2 = this.getAABB(world, x, y, z, flag);
        if (aabb.intersectsWith(aabb2)) {
            list.add(aabb2);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return this.getAABB(world, x, y, z, this.preventMobMovement(world, x, y, z));
    }

    private AxisAlignedBB getAABB(World world, int x, int y, int z, boolean par5) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        double d0 = par5 ? 256.0D : 0.0D;
        return AxisAlignedBB.getBoundingBox(
                (double) x + this.minX, (double) y + this.minY, (double) z + this.minZ,
                (double) x + this.maxX, (double) y + this.maxY + d0, (double) z + this.maxZ);
    }

    public boolean preventMobMovement(World world, int x, int y, int z) {
        TileEntityLargeRailBase rail = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
        if (rail != null && rail.getRailCore() != null) {
            TileEntityLargeRailCore core = rail.getRailCore();
            ModelSetRail set = core.getProperty().getModelSet();
            return !set.getConfig().allowCrossing;
        }
        return false;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess blockAccess, int x, int y, int z) {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
        TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityLargeRailBase) {
            TileEntityLargeRailBase rail = (TileEntityLargeRailBase) tileEntity;
            float f0 = 0.0625F;
            float[] fa = rail.getBlockHeights(x, y, z, f0, true);
            float height2 = 0.0F;
            for (int i = 0; i < 4; ++i) {
                height2 += fa[i];
            }
            height2 *= 0.25F;
            if (height2 < 0.0625F) {
                height2 = 0.0625F;
            }
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, height2, 1.0F);
        }
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return null;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityLargeRailBase) {
            ItemStack itemStack = new ItemStack(RTMItem.itemLargeRail);
            TileEntityLargeRailCore coreTile = ((TileEntityLargeRailBase) tileEntity).getRailCore();
            if (coreTile != null) {
                RailProperty property = coreTile.getProperty();
                ItemRail.writePropToItem(property, itemStack);
                return itemStack;
            }
        }
        return null;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        if (world.isRemote) {
            return false;
//            return super.removedByPlayer(world, player, x, y, z);
        } else {
            if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_RAIL)) {
                if (!player.capabilities.isCreativeMode) {
                    RailProperty prop = ItemRail.getDefaultProperty();
                    TileEntity tile0 = world.getTileEntity(x, y, z);
                    if (tile0 instanceof TileEntityLargeRailBase) {
                        TileEntityLargeRailCore tile1 = ((TileEntityLargeRailBase) tile0).getRailCore();
                        if (tile1 != null) {
                            prop = tile1.getProperty();
                        }
                    }
                    this.dropRail(world, x, y, z, prop);
                }
                return super.removedByPlayer(world, player, x, y, z);
            }
            return false;
        }
    }

    protected void dropRail(World par1World, int par2, int par3, int par4, RailProperty prop) {
        if (!par1World.isRemote) {
            this.dropBlockAsItem(par1World, par2, par3, par4, ItemRail.getRailItem(prop));
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityLargeRailBase tile0 = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
        TileEntityLargeRailCore core = tile0.getRailCore();
        if (!world.isRemote && core != null) {
            Arrays.stream(core.getAllRailMaps()).forEach(rm -> rm.breakRail(world, core.getProperty(), core));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote) {
            if (this.railTextureType == 4 && entity instanceof EntityBogie) {
                EntityTrainBase train = ((EntityBogie) entity).getTrain();
                if (train != null && Math.abs(train.getSpeed()) > 0.0F) {
                    double speed = (double) train.getSpeed() * 0.125D;//7.5

                    //10
                    IntStream.range(0, 5).forEach(i -> {
                        double d0 = x + (double) world.rand.nextFloat();
                        double d1 = y + (double) world.rand.nextFloat() * 0.25D;
                        double d2 = z + (double) world.rand.nextFloat();
                        double vx = (d0 - entity.posX) * speed;//0.125
                        double vz = (d2 - entity.posZ) * speed;
                        world.spawnParticle("snowshovel", d0, d1, d2, vx, 0.125D, vz);
                    });
                }
            }
        } else {
            if (entity instanceof EntityTrainBase) {
                TileEntity tile0 = world.getTileEntity(x, y, z);
                if (tile0 instanceof TileEntityLargeRailBase) {
                    TileEntityLargeRailCore tile = ((TileEntityLargeRailBase) tile0).getRailCore();
                    if (tile != null) {
                        tile.colliding = true;
                    }
                }
            }
        }
    }

    public boolean isCore() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
        int[] ia = BlockUtil.facing[side];
        Block sideBlock = access.getBlock(x + ia[0], y + ia[1], z + ia[2]);
        boolean flag = sideBlock.isOpaqueCube() || sideBlock == this;
        return super.shouldSideBeRendered(access, x, y, z, side) && flag;
    }
}