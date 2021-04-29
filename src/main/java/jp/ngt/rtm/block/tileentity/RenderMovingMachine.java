package jp.ngt.rtm.block.tileentity;

import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderMovingMachine extends TileEntitySpecialRenderer {
    private void renderMovingMachine(TileEntityMovingMachine tileEntity, double x, double y, double z, float p5) {
        if ((!tileEntity.isCore && tileEntity.hasPair())) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

        GLHelper.disableLighting();
        GLHelper.setLightmapMaxBrightness();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        if (tileEntity.guideVisibility) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL11.GL_LINES);
            tessellator.setColorRGBA_I(0xFF0000, 0xFF);
            tessellator.addVertex(0.0D, 0.0D, 0.0D);
            tessellator.addVertex(tileEntity.pairBlockX, tileEntity.pairBlockY, tileEntity.pairBlockZ);
            tessellator.draw();
        }

        double dx = tileEntity.prevPosX + (tileEntity.posX - tileEntity.prevPosX) * (double) p5;
        double dy = tileEntity.prevPosY + (tileEntity.posY - tileEntity.prevPosY) * (double) p5;
        double dz = tileEntity.prevPosZ + (tileEntity.posZ - tileEntity.prevPosZ) * (double) p5;
        GL11.glTranslatef((float) (dx + 0.5D), (float) (dy + 0.5D), (float) (dz + 0.5D));

        if (tileEntity.guideVisibility) {
            NGTRenderer.renderFrame(0.0D, 0.0D, 0.0D, tileEntity.width, tileEntity.height, tileEntity.depth, 0x00FF00, 0xFF);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GLHelper.enableLighting();

        if (this.setupBrightness(tileEntity)) {

        }
        this.renderBlocks(tileEntity, p5);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    /**
     * 現在位置にブロックが存在しないならtrue
     */
    private boolean setupBrightness(TileEntityMovingMachine tileEntity) {
        int x = MathHelper.floor_double((double) tileEntity.xCoord + tileEntity.posX);
        int y = MathHelper.floor_double((double) tileEntity.yCoord + tileEntity.posY);
        int z = MathHelper.floor_double((double) tileEntity.zCoord + tileEntity.posZ);
        if (tileEntity.getWorldObj().getBlock(x, y, z) == Blocks.air) {
            int i = tileEntity.getWorldObj().getLightBrightnessForSkyBlocks(x, y, z, 0);
            GLHelper.setBrightness(i);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private void renderBlocks(TileEntityMovingMachine tile, float p2) {
        if (tile.dummyWorld == null || tile.blocksObject == null) {
            return;
        }

        if (tile.glLists == null) {
            tile.glLists = new DisplayList[2];
        }

        int pass = MinecraftForgeClient.getRenderPass();
        if (pass == -1) {
            pass = 0;
        }
        NGTWorld world = (NGTWorld) tile.dummyWorld;
        NGTRenderer.renderTileEntities(world, p2, pass);

        GLHelper.disableLighting();
        this.bindTexture(TextureMap.locationBlocksTexture);
        if (!GLHelper.isValid(tile.glLists[pass])) {
            tile.glLists[pass] = GLHelper.generateGLList();
            GLHelper.startCompile(tile.glLists[pass]);
            NGTRenderer.renderNGTObject(world, tile.blocksObject, false, 0, pass);
            GLHelper.endCompile();
        } else {
            GLHelper.callList(tile.glLists[pass]);
        }
        GLHelper.enableLighting();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double p2, double p3, double p4, float p5) {
        this.renderMovingMachine((TileEntityMovingMachine) tileEntity, p2, p3, p4, p5);
    }
}