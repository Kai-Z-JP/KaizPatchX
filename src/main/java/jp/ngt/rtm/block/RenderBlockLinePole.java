package jp.ngt.rtm.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.electric.BlockSignal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import java.util.stream.IntStream;

/**
 * 架線柱or鉄骨の描画
 */
@SideOnly(Side.CLIENT)
public class RenderBlockLinePole implements ISimpleBlockRenderingHandler {
    private static final float[][] vertex = {
            {0.0625F, 0.0F, 0.0F}, {0.0625F, 0.0625F, 0.0F}, {0.0625F, 0.9375F, 0.5F}, {0.0625F, 1.0F, 0.5F},
            {-0.0625F, 0.0F, 0.0F}, {-0.0625F, 0.0625F, 0.0F}, {-0.0625F, 0.9375F, 0.5F}, {-0.0625F, 1.0F, 0.5F}};
    //+x,-x,+y,-y,+z,-z
    private static final float[][] framePos = {
            vertex[3], vertex[2], vertex[0], vertex[1],
            vertex[5], vertex[4], vertex[6], vertex[7],
            vertex[3], vertex[1], vertex[5], vertex[7],
            vertex[6], vertex[4], vertex[0], vertex[2],
            vertex[7], vertex[6], vertex[2], vertex[3],
            vertex[1], vertex[0], vertex[4], vertex[5]};
    private static final float F0 = 0.0625F;

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        if (block == RTMBlock.linePole) {
            NGTRenderer.renderBlock(F0 * 8.0F, F0 * 0.0F, F0 * 7.0F, F0 * 16.0F, F0 * 16.0F, F0 * 9.0F, renderer, block);
            NGTRenderer.renderBlock(F0 * 0.0F, F0 * 0.0F, F0 * 7.0F, F0 * 8.0F, F0 * 16.0F, F0 * 9.0F, renderer, block);
        } else {
            NGTRenderer.renderBlock(F0 * 8.0F, F0 * 13.0F, F0 * 4.0F, F0 * 16.0F, F0 * 14.0F, F0 * 12.0F, renderer, block, metadata);
            NGTRenderer.renderBlock(F0 * 8.0F, F0 * 3.0F, F0 * 7.5F, F0 * 16.0F, F0 * 13.0F, F0 * 8.5F, renderer, block, metadata);
            NGTRenderer.renderBlock(F0 * 8.0F, F0 * 2.0F, F0 * 4.0F, F0 * 16.0F, F0 * 3.0F, F0 * 12.0F, renderer, block, metadata);

            NGTRenderer.renderBlock(F0 * 0.0F, F0 * 13.0F, F0 * 4.0F, F0 * 8.0F, F0 * 14.0F, F0 * 12.0F, renderer, block, metadata);
            NGTRenderer.renderBlock(F0 * 0.0F, F0 * 3.0F, F0 * 7.5F, F0 * 8.0F, F0 * 13.0F, F0 * 8.5F, renderer, block, metadata);
            NGTRenderer.renderBlock(F0 * 0.0F, F0 * 2.0F, F0 * 4.0F, F0 * 8.0F, F0 * 3.0F, F0 * 12.0F, renderer, block, metadata);
        }

        NGTRenderer.renderBlock(F0 * 4.0F, F0 * 0.0F, F0 * 4.0F, F0 * 12.0F, F0 * 16.0F, F0 * 12.0F, renderer, block, metadata);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            boolean overrided = false;

            if (block == RTMBlock.signal) {
                block = ((BlockSignal) block).getRenderBlock(world, x, y, z);

                renderer.renderBlockByRenderType(block, x, y, z);
                return true;
            }

            int meta = world.getBlockMetadata(x, y, z);
            boolean[] ba = new boolean[6];
            ba[0] = BlockLinePole.isConnected(world, x + 1, y, z, false);
            ba[1] = BlockLinePole.isConnected(world, x - 1, y, z, false);
            ba[2] = BlockLinePole.isConnected(world, x, y, z + 1, false);
            ba[3] = BlockLinePole.isConnected(world, x, y, z - 1, false);
            ba[4] = BlockLinePole.isConnected(world, x, y + 1, z, false);
            ba[5] = BlockLinePole.isConnected(world, x, y - 1, z, false);
            int flagI = 0;
            for (boolean b : ba) {
                if (b) {
                    ++flagI;
                }
            }

            //IIcon icon = ((BlockLinePole)block).getIcon(0, 0);
            IIcon icon = block.getIcon(0, 0);
            if (block == RTMBlock.linePole && (meta == 2 || meta == 3)) {
                renderer.setOverrideBlockTexture(icon);
                overrided = true;
            }

            if (ba[0]) {
                if (block == RTMBlock.linePole) {
                    if (meta == 2 || meta == 3) {
                        NGTRenderer.renderBlock(F0 * 8.0F, F0 * 7.0F, F0 * 7.0F, F0 * 16.0F, F0 * 9.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                    } else {
                        if (flagI > 1) {
                            NGTRenderer.renderBlock(F0 * 8.0F, F0 * 15.0F, F0 * 7.0F, F0 * 16.0F, F0 * 16.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                        }
                        NGTRenderer.renderBlock(F0 * 8.0F, F0 * 0.0F, F0 * 7.0F, F0 * 16.0F, F0 * 1.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                        this.renderFrameParts(renderer, icon, 90.0F, (double) x + 0.5D, y, (double) z + 0.5D);
                        //NGTRenderer.renderBlock(F0*8.0F, F0*0.0F, F0*7.0F, F0*16.0F, F0*16.0F, F0*9.0F, true, renderer, block, x, y, z);
                    }
                } else {
                    NGTRenderer.renderBlock(F0 * 8.0F, F0 * 13.0F, F0 * 4.0F, F0 * 16.0F, F0 * 14.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 8.0F, F0 * 3.0F, F0 * 7.5F, F0 * 16.0F, F0 * 13.0F, F0 * 8.5F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 8.0F, F0 * 2.0F, F0 * 4.0F, F0 * 16.0F, F0 * 3.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                }
            }

            if (ba[1]) {
                if (block == RTMBlock.linePole) {
                    if (meta == 2 || meta == 3) {
                        NGTRenderer.renderBlock(F0 * 0.0F, F0 * 7.0F, F0 * 7.0F, F0 * 8.0F, F0 * 9.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                    } else {
                        if (flagI > 1) {
                            NGTRenderer.renderBlock(F0 * 0.0F, F0 * 15.0F, F0 * 7.0F, F0 * 8.0F, F0 * 16.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                        }
                        NGTRenderer.renderBlock(F0 * 0.0F, F0 * 0.0F, F0 * 7.0F, F0 * 8.0F, F0 * 1.0F, F0 * 9.0F, true, renderer, block, x, y, z);
                        this.renderFrameParts(renderer, icon, -90.0F, (double) x + 0.5D, y, (double) z + 0.5D);
                        //NGTRenderer.renderBlock(F0*0.0F, F0*0.0F, F0*7.0F, F0*8.0F, F0*16.0F, F0*9.0F, true, renderer, block, x, y, z);
                    }
                } else {
                    NGTRenderer.renderBlock(F0 * 0.0F, F0 * 13.0F, F0 * 4.0F, F0 * 8.0F, F0 * 14.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 0.0F, F0 * 3.0F, F0 * 7.5F, F0 * 8.0F, F0 * 13.0F, F0 * 8.5F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 0.0F, F0 * 2.0F, F0 * 4.0F, F0 * 8.0F, F0 * 3.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                }
            }

            if (ba[2]) {
                if (block == RTMBlock.linePole) {
                    if (meta == 2 || meta == 3) {
                        NGTRenderer.renderBlock(F0 * 7.0F, F0 * 7.0F, F0 * 8.0F, F0 * 9.0F, F0 * 9.0F, F0 * 16.0F, true, renderer, block, x, y, z);
                    } else {
                        if (flagI > 1) {
                            NGTRenderer.renderBlock(F0 * 7.0F, F0 * 15.0F, F0 * 8.0F, F0 * 9.0F, F0 * 16.0F, F0 * 16.0F, true, renderer, block, x, y, z);
                        }
                        NGTRenderer.renderBlock(F0 * 7.0F, F0 * 0.0F, F0 * 8.0F, F0 * 9.0F, F0 * 1.0F, F0 * 16.0F, true, renderer, block, x, y, z);
                        this.renderFrameParts(renderer, icon, 0.0F, (double) x + 0.5D, y, (double) z + 0.5D);
                        //NGTRenderer.renderBlock(F0*7.0F, F0*0.0F, F0*8.0F, F0*9.0F, F0*16.0F, F0*16.0F, true, renderer, block, x, y, z);
                    }
                } else {
                    NGTRenderer.renderBlock(F0 * 4.0F, F0 * 13.0F, F0 * 8.0F, F0 * 12.0F, F0 * 14.0F, F0 * 16.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 7.5F, F0 * 3.0F, F0 * 8.0F, F0 * 8.5F, F0 * 13.0F, F0 * 16.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 4.0F, F0 * 2.0F, F0 * 8.0F, F0 * 12.0F, F0 * 3.0F, F0 * 16.0F, true, renderer, block, x, y, z, meta);
                }
            }

            if (ba[3]) {
                if (block == RTMBlock.linePole) {
                    if (meta == 2 || meta == 3) {
                        NGTRenderer.renderBlock(F0 * 7.0F, F0 * 7.0F, F0 * 0.0F, F0 * 9.0F, F0 * 9.0F, F0 * 8.0F, true, renderer, block, x, y, z);
                    } else {
                        if (flagI > 1) {
                            NGTRenderer.renderBlock(F0 * 7.0F, F0 * 15.0F, F0 * 0.0F, F0 * 9.0F, F0 * 16.0F, F0 * 8.0F, true, renderer, block, x, y, z);
                        }
                        NGTRenderer.renderBlock(F0 * 7.0F, F0 * 0.0F, F0 * 0.0F, F0 * 9.0F, F0 * 1.0F, F0 * 8.0F, true, renderer, block, x, y, z);
                        this.renderFrameParts(renderer, icon, 180.0F, (double) x + 0.5D, y, (double) z + 0.5D);
                        //NGTRenderer.renderBlock(F0*7.0F, F0*0.0F, F0*0.0F, F0*9.0F, F0*16.0F, F0*8.0F, true, renderer, block, x, y, z);
                    }
                } else {
                    NGTRenderer.renderBlock(F0 * 4.0F, F0 * 13.0F, F0 * 0.0F, F0 * 12.0F, F0 * 14.0F, F0 * 8.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 7.5F, F0 * 3.0F, F0 * 0.0F, F0 * 8.5F, F0 * 13.0F, F0 * 8.0F, true, renderer, block, x, y, z, meta);
                    NGTRenderer.renderBlock(F0 * 4.0F, F0 * 2.0F, F0 * 0.0F, F0 * 12.0F, F0 * 3.0F, F0 * 8.0F, true, renderer, block, x, y, z, meta);
                }
            }

            boolean flag0 = (flagI == 0);//どことも繋がっていない
            boolean flag1 = block == RTMBlock.linePole
                    ? (IntStream.of(0, 1, 2).allMatch(i -> ba[i])) || (IntStream.of(1, 2, 3).allMatch(v -> ba[v])) || (IntStream.of(2, 3, 0).allMatch(k -> ba[k])) || (IntStream.of(3, 0, 1).allMatch(j -> ba[j]))
                    : (ba[0] && ba[2]) || (ba[2] && ba[1]) || (ba[1] && ba[3]) || (ba[3] && ba[0]);
            //boolean flag1 = !((b0 && b1 && !b2 && !b3) || (!b0 && !b1 && b2 && b3));
            //if(!b4 && (BlockLinePole.isConnected(world, x, y + 1, z, true) || BlockLinePole.isConnected(world, x, y - 1, z, true)))
            // && (BlockLinePole.isConnected(world, x, y + 1, z, false) || BlockLinePole.isConnected(world, x, y - 1, z, false)))
            if (flag0 || flag1 || ba[4] || ba[5]) {
                //柱部分
                if (block == RTMBlock.linePole) {
                    renderer.setOverrideBlockTexture(((BlockLinePole) block).getLinePoleIcon(meta));
                    overrided = true;
                    if (meta == 3) {
                        NGTRenderer.renderBlock(F0 * 7.0F, F0 * 0.0F, F0 * 7.0F, F0 * 9.0F, F0 * 16.0F, F0 * 9.0F, true, renderer, block, x, y, z, meta);
                    } else {
                        NGTRenderer.renderBlock(F0 * 4.0F, F0 * 0.0F, F0 * 4.0F, F0 * 12.0F, F0 * 16.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                    }
                } else {
                    NGTRenderer.renderBlock(F0 * 4.0F, F0 * 0.0F, F0 * 4.0F, F0 * 12.0F, F0 * 16.0F, F0 * 12.0F, true, renderer, block, x, y, z, meta);
                }
            }

            if (overrided) {
                renderer.clearOverrideBlockTexture();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int par1) {
        return true;
    }

    @Override
    public int getRenderId() {
        return RTMBlock.renderIdSignalBase;
    }

    private void setBlockColor(IBlockAccess world, int x, int y, int z, Block block) {
        this.setBlockColor(block.colorMultiplier(world, x, y, z), block);
    }

    private void setBlockColor(int metadata, Block block) {
        Tessellator tessellator = Tessellator.instance;
        float f = (float) (metadata >> 16 & 255) / 255.0F;
        float f1 = (float) (metadata >> 8 & 255) / 255.0F;
        float f2 = (float) (metadata & 255) / 255.0F;
        if (EntityRenderer.anaglyphEnable) {
            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
            f = f3;
            f1 = f4;
            f2 = f5;
        }
        tessellator.setColorOpaque_F(f, f1, f2);
    }

    private void renderFrameParts(RenderBlocks renderer, IIcon icon, float angle, double x, double y, double z) {
        Tessellator tessellator = Tessellator.instance;
        float angle2 = NGTMath.toRadians(angle);
        IntStream.range(0, framePos.length).forEach(i -> {
            Vec3 vec3 = Vec3.createVectorHelper(framePos[i][0], framePos[i][1], framePos[i][2]);
            vec3.rotateAroundY(angle2);
            int i0 = i % 4;
            float u = i0 <= 1 ? icon.getMaxU() : icon.getMinU();
            float v = (i0 == 0 || i0 == 3) ? icon.getMinV() : icon.getMaxV();
            tessellator.addVertexWithUV(x + vec3.xCoord, y + vec3.yCoord, z + vec3.zCoord, u, v);
        });
    }
}