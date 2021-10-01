package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityScaffold;
import jp.ngt.rtm.block.tileentity.TileEntityScaffoldStairs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.IntStream;

public class BlockScaffoldStairs extends BlockOrnamentBase {
    /**
     * @param par1 階段の元のブロック
     */
    public BlockScaffoldStairs(Block par1) {
        super(par1.getMaterial());
        this.setHardness(2.0F);
        this.setResistance(10.0F);
        this.setStepSound(RTMBlock.soundTypeMetal2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityScaffoldStairs();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack) {
        int meta = itemstack.getItemDamage();
        int playerFacing = (MathHelper.floor_double((NGTMath.normalizeAngle(entityliving.rotationYaw + 180.0D) / 90D) + 0.5D) & 3);
        world.setBlock(x, y, z, this, meta, 3);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffold) {
            ((TileEntityScaffold) tile).setDir((byte) playerFacing);
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffold) {
            byte dir = ((TileEntityScaffold) tile).getDir();
            byte flag0 = getConnectionType(world, x + 1, y, z, dir);
            byte flag1 = getConnectionType(world, x - 1, y, z, dir);
            byte flag2 = getConnectionType(world, x, y, z + 1, dir);
            byte flag3 = getConnectionType(world, x, y, z - 1, dir);

            if (dir == 0 || dir == 2)//Z
            {
                if (flag1 != 3) {
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0625F, 2.0F, 1.0F);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                }

                if (flag0 != 3) {
                    this.setBlockBounds(0.9375F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                }

                IntStream.range(0, 4).forEach(i -> {
                    float f0 = i * 0.25F;
                    float f1 = (dir == 2) ? f0 : 0.75F - f0;
                    this.setBlockBounds(0.0F, 0.0F + f0, f1, 1.0F, 0.25F + f0, 0.25F + f1);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                });
            } else//X
            {
                if (flag3 != 3) {
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0625F);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                }

                if (flag2 != 3) {
                    this.setBlockBounds(0.0F, 0.0F, 0.9375F, 1.0F, 2.0F, 1.0F);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                }

                IntStream.range(0, 4).forEach(i -> {
                    float f0 = i * 0.25F;
                    float f1 = (dir == 1) ? f0 : 0.75F - f0;
                    this.setBlockBounds(f1, 0.0F + f0, 0.0F, 0.25F + f1, 0.25F + f0, 1.0F);
                    super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
                });
            }

            this.setBlockBoundsForItemRender();
        } else {
            this.setBlockBoundsForItemRender();
            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
     * @return なし:0,  足場Z:1, 足場X:2, 階段:3, 立方体:4
     */
    public static byte getConnectionType(IBlockAccess world, int x, int y, int z, byte dir) {
        Block block = world.getBlock(x, y, z);
        Block blockD = world.getBlock(x, y - 1, z);
        Block blockU = world.getBlock(x, y + 1, z);

        if (block == RTMBlock.scaffold) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityScaffold) {
                boolean b0 = ((TileEntityScaffold) tile).getDir() == 0;
                return (byte) (b0 ? 1 : 2);
            }
            return 0;
        } else if (block == RTMBlock.scaffoldStairs) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityScaffoldStairs) {
                if (((TileEntityScaffoldStairs) tile).getDir() == dir) {
                    return 3;
                }
            }
            return 0;
        } else if (blockD == RTMBlock.scaffoldStairs) {
            TileEntity tile = world.getTileEntity(x, y - 1, z);
            if (tile instanceof TileEntityScaffoldStairs) {
                if (((TileEntityScaffoldStairs) tile).getDir() == dir) {
                    return 3;
                }
            }
            return 0;
        } else if (blockU == RTMBlock.scaffoldStairs) {
            TileEntity tile = world.getTileEntity(x, y + 1, z);
            if (tile instanceof TileEntityScaffoldStairs) {
                if (((TileEntityScaffoldStairs) tile).getDir() == dir) {
                    return 3;
                }
            }
            return 0;
        } else {
            if (block.isOpaqueCube()) {
                return 4;
            } else {
                return 0;
            }
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, x, y, z, this.getItem(par5));
        }
    }

    private ItemStack getItem(int damage) {
        return new ItemStack(Item.getItemFromBlock(this), 1, damage);
    }


    @Override
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffoldStairs) {
            jp.ngt.ngtlib.math.Vec3 vec = ((TileEntityScaffoldStairs) tile).getMotionVec();
            BlockScaffold.addVecToEntity(entity, vec);
        }
    }

    //net/minecraft/world/World.java:2552 1.7.10では無理
    @Override
    public void velocityToAddToEntity(World world, int x, int y, int z, Entity entity, Vec3 motion) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityScaffoldStairs) {
            jp.ngt.ngtlib.math.Vec3 vec = ((TileEntityScaffoldStairs) tile).getMotionVec();
            BlockScaffold.addVecToEntity(entity, vec);
            motion.addVector(vec.getX(), vec.getY(), vec.getZ());
        }
    }
}