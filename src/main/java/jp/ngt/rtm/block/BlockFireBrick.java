package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMAchievement;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMMaterial;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFireBrick extends Block {
    public BlockFireBrick(boolean randomTick) {
        super(RTMMaterial.fireproof);
        this.setTickRandomly(randomTick);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, living, stack);
        if (!world.isRemote && living instanceof EntityPlayer) {
            if (this == RTMBlock.fireBrick) {
                ((EntityPlayer) living).addStat(RTMAchievement.startIronMaking, 1);
            } else if (this == RTMBlock.hotStoveBrick) {
                ((EntityPlayer) living).addStat(RTMAchievement.startIronMaking2, 1);
            }
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            if (this == RTMBlock.hotStoveBrick) {
                int meta = world.getBlockMetadata(x, y, z);
                int n = 16 - meta;
                int x0;
                int y0;
                int z0;
                for (int i = 0; i < 6; ++i) {
                    x0 = x + BlockUtil.field_01[i][0];
                    y0 = y + BlockUtil.field_01[i][1];
                    z0 = z + BlockUtil.field_01[i][2];
                    Block block = world.getBlock(x0, y0, z0);
                    if (block == Blocks.air) {
                        if (meta > 0 && world.rand.nextInt(n) == 0) {
                            world.setBlock(x0, y0, z0, RTMBlock.furnaceFire, 15, 2);
                            world.setBlockMetadataWithNotify(x, y, z, 0, 2);
                            break;
                        }
                    } else if (block == RTMBlock.exhaustGas) {
                        int m0 = world.getBlockMetadata(x0, y0, z0);
                        if (meta < 15 && m0 > 0) {
                            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
                            world.setBlockMetadataWithNotify(x0, y0, z0, m0 - 1, 2);
                            break;
                        }
                    } else if (block == Blocks.lava) {
                        if (meta < 15) {
                            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
                            break;
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (this == RTMBlock.hotStoveBrick) {
            int meta = world.getBlockMetadata(x, y, z);
            int c0 = (meta << 4) & 255;
            return 0xffffff - (c0 << 8) - c0;
        }
        return super.colorMultiplier(world, x, y, z);
    }

    @Override
    public String getHarvestTool(int metadata)//Material != rockのとき必須？
    {
        return "pickaxe";
    }

    @Override
    public int getHarvestLevel(int metadata) {
        return 0;
    }
}