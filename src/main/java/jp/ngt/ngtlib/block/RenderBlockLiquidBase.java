package jp.ngt.ngtlib.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public abstract class RenderBlockLiquidBase implements ISimpleBlockRenderingHandler {
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            Tessellator tessellator = Tessellator.instance;
            IIcon icon = renderer.getBlockIconFromSideAndMetadata(block, 0, 0);
            double minU = icon.getMinU();
            double minV = icon.getMinV();
            double maxU = icon.getMaxU();
            double maxV = icon.getMaxV();
            double midU = (maxU + minU) / 2.0D;
            double midV = (maxV + minV) / 2.0D;

            int meta = world.getBlockMetadata(x, y, z);
            double y0 = ((float) meta + 1) / 16.0F;//minX,minZ
            double y1 = y0;//minX,maxZ
            double y2 = y0;//maxX,maxZ
            double y3 = y0;//maxX,minZ
            double y4 = y0;
            boolean flag0 = true;//+x
            boolean flag1 = true;//-x
            boolean flag2 = true;//+y
            boolean flag3 = true;//-y
            boolean flag4 = true;//+z
            boolean flag5 = true;//-z

            if (world.getBlock(x - 1, y, z - 1) == block) {
                int meta2 = world.getBlockMetadata(x - 1, y, z - 1);
                if (meta2 > meta) {
                    y0 = ((float) meta2 + 1) / 16.0F;
                }
            }

            if (world.getBlock(x - 1, y, z) == block) {
                int meta2 = world.getBlockMetadata(x - 1, y, z);
                if (meta2 > meta) {
                    y0 = ((float) meta2 + 1) / 16.0F;
                    y1 = y0;
                    flag1 = false;
                }
            }

            if (world.getBlock(x - 1, y, z + 1) == block) {
                int meta2 = world.getBlockMetadata(x - 1, y, z + 1);
                if (meta2 > meta) {
                    y1 = ((float) meta2 + 1) / 16.0F;
                }
            }

            if (world.getBlock(x, y, z + 1) == block) {
                int meta2 = world.getBlockMetadata(x, y, z + 1);
                if (meta2 > meta) {
                    y1 = ((float) meta2 + 1) / 16.0F;
                    y2 = y1;
                    flag4 = false;
                }
            }

            if (world.getBlock(x + 1, y, z + 1) == block) {
                int meta2 = world.getBlockMetadata(x + 1, y, z + 1);
                if (meta2 > meta) {
                    y2 = ((float) meta2 + 1) / 16.0F;
                }
            }

            if (world.getBlock(x + 1, y, z) == block) {
                int meta2 = world.getBlockMetadata(x + 1, y, z);
                if (meta2 > meta) {
                    y2 = ((float) meta2 + 1) / 16.0F;
                    y3 = y2;
                    flag0 = false;
                }
            }

            if (world.getBlock(x + 1, y, z - 1) == block) {
                int meta2 = world.getBlockMetadata(x + 1, y, z - 1);
                if (meta2 > meta) {
                    y3 = ((float) meta2 + 1) / 16.0F;
                }
            }

            if (world.getBlock(x, y, z - 1) == block) {
                int meta2 = world.getBlockMetadata(x, y, z - 1);
                if (meta2 > meta) {
                    y0 = ((float) meta2 + 1) / 16.0F;
                    y3 = y0;
                    flag5 = false;
                }
            }

            if (world.getBlock(x, y + 1, z) == block) {
                y0 = 1.0D;
                y1 = 1.0D;
                y2 = 1.0D;
                y3 = 1.0D;
                y4 = 1.0D;
                flag2 = false;
            }

            y4 = (y0 + y1 + y2 + y3) / 4.0D;

            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            if (flag1) {
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 1.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 0.0D, y + y1, z + 1.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 0.0D, minU, minV);
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 0.0D, minU, maxV);
            }

            if (flag4) {
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 1.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 1.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 0.0D, y + y1, z + 1.0D, minU, minV);
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 1.0D, minU, maxV);
            }

            if (flag0) {
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 0.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 1.0D, y + y3, z + 0.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 1.0D, minU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 1.0D, minU, maxV);
            }

            if (flag5) {
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 0.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 0.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + y3, z + 0.0D, minU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 0.0D, minU, maxV);
            }

            if (flag3) {
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 1.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 1.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 0.0D, y + 0.0D, z + 0.0D, minU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + 0.0D, z + 0.0D, minU, maxV);
            }

            if (flag2) {
                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);
                tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 0.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 0.0D, y + y1, z + 1.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);

                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);
                tessellator.addVertexWithUV(x + 0.0D, y + y1, z + 1.0D, maxU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 1.0D, minU, minV);
                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);

                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);
                tessellator.addVertexWithUV(x + 1.0D, y + y2, z + 1.0D, minU, minV);
                tessellator.addVertexWithUV(x + 1.0D, y + y3, z + 0.0D, minU, maxV);
                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);

                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);
                tessellator.addVertexWithUV(x + 1.0D, y + y3, z + 0.0D, minU, maxV);
                tessellator.addVertexWithUV(x + 0.0D, y + y0, z + 0.0D, maxU, maxV);
                tessellator.addVertexWithUV(x + 0.5D, y + y4, z + 0.5D, midU, midV);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int par1) {
        return false;
    }
}