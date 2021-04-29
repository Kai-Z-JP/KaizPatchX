package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.IntStream;

public class BlockMeltedMetal extends BlockLiquidBase {
    public BlockMeltedMetal() {
        super(Material.lava);
        this.setLightLevel(1.0F);
    }

    @Override
    public int tickRate(World world) {
        return 10;
    }

    @Override
    public int getRenderId() {
        return RTMBlock.renderIdLiquid;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        super.updateTick(world, x, y, z, random);

        if (this == RTMBlock.liquefiedSteel) {
            if (world.getBlockMetadata(x, y, z) == 0 && random.nextInt(5) == 0) {
                if (!world.isRemote) {
                    boolean flag0 = false;//air
                    boolean flag1 = true;//meltedMetal
                    for (int i = 0; i < BlockUtil.facing.length; ++i) {
                        int x0 = x + BlockUtil.facing[i][0];
                        int y0 = y + BlockUtil.facing[i][1];
                        int z0 = z + BlockUtil.facing[i][2];
                        Block block1 = world.getBlock(x0, y0, z0);
                        if (block1 == Blocks.air) {
                            flag0 = true;
                        }

                        if (block1 instanceof BlockLiquidBase && world.getBlockMetadata(x0, y0, z0) > 0) {
                            flag1 = false;
                        }
                    }

                    if (flag0 && flag1 && random.nextInt(5) == 0) {
                        world.setBlock(x, y, z, RTMBlock.steelSlab, 15, 2);
                    }
                }
            }
        }
    }

    @Override
    protected int canFlowLiquid(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == RTMBlock.furnaceFire || block == RTMBlock.exhaustGas) {
            return 15;
        }
        return super.canFlowLiquid(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block p_149695_5_) {
        if (!world.isRemote) {
            IntStream.range(0, BlockUtil.facing.length).forEach(i -> {
                int x0 = x + BlockUtil.facing[i][0];
                int y0 = y + BlockUtil.facing[i][1];
                int z0 = z + BlockUtil.facing[i][2];
                if (world.getBlock(x0, y0, z0).getMaterial() == Material.water) {
                    world.setBlock(x0, y0, z0, Blocks.air);
                    world.setBlock(x, y, z, Blocks.air);
                    world.createExplosion(null, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, 8.0F, true);
                }
            });
        }

        super.onNeighborBlockChange(world, x, y, z, p_149695_5_);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        entity.motionY = 0.20000000298023224D;
        entity.motionX = (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F;
        entity.motionZ = (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F;
        entity.playSound("random.fizz", 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
        entity.attackEntityFrom(DamageSource.lava, 1.0F);
        entity.setFire(5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        return this.blockIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        if (this == RTMBlock.liquefiedPigIron) {
            this.blockIcon = p_149651_1_.registerIcon("rtm:pigIron_L");
        } else {
            this.blockIcon = p_149651_1_.registerIcon("rtm:slag");
        }
    }

    @SideOnly(Side.CLIENT)
    public static IIcon getLiquidIcon(String p_149803_0_) {
        return RTMBlock.furnaceFire.getIcon(0, 0);
    }
}