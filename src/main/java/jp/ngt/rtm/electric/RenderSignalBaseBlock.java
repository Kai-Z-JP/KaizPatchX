package jp.ngt.rtm.electric;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class RenderSignalBaseBlock implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            if (block == RTMBlock.signal) {
                block = ((BlockSignal) block).getRenderBlock(world, x, y, z);

                renderer.renderBlockByRenderType(block, x, y, z);
                return true;
            }
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

}
