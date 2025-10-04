package jp.ngt.mcte.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.world.MCTEWorld;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public final class RenderMiniature extends TileEntitySpecialRenderer {
    public static RenderMiniature INSTANCE = new RenderMiniature();

    private RenderMiniature() {
    }

    private void renderMiniatureAt(TileEntityMiniature tile, double par2, double par4, double par6, float par8) {
        if (tile.blocksObject == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);
        switch (tile.attachSide) {
            case 0:
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case 1:
                break;
            case 2:
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case 3:
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case 4:
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case 5:
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                break;
        }
        GL11.glTranslatef(0.0F, -0.5F, 0.0F);
        GL11.glTranslatef(tile.getOffsetX(), tile.getOffsetY(), tile.getOffsetZ());
        GL11.glRotatef(tile.getRotation(), 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(tile.offsetX, tile.offsetY, tile.offsetZ);
        float f0 = tile.scale;
        GL11.glScalef(f0, f0, f0);

        float x = (float) tile.blocksObject.xSize * 0.5F;
        float z = (float) tile.blocksObject.zSize * 0.5F;
        GL11.glTranslatef(-x, 0.0F, -z);

        MCTEWorld world = tile.getDummyWorld();
        int pass = MinecraftForgeClient.getRenderPass();
        if (pass == -1) {
            pass = 0;
        }
        //ブロック描画後だと明るさが変になる
        NGTRenderer.renderTileEntities(world, par8, pass);
        NGTRenderer.renderEntities(world, par8, pass);
        this.renderBlocks(world, tile, par8, pass);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    private void renderBlocks(MCTEWorld world, TileEntityMiniature tile, float par3, int pass) {
        if (tile.glLists == null) {
            tile.glLists = new DisplayList[2];
        }

        GLHelper.disableLighting();
        int i = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
        GLHelper.setBrightness(i);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.bindTexture(TextureMap.locationBlocksTexture);
        boolean smoothing = NGTUtilClient.getMinecraft().gameSettings.ambientOcclusion != 0;
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }
        if (!GLHelper.isValid(tile.glLists[pass])) {
            tile.glLists[pass] = GLHelper.generateGLList();
            GLHelper.startCompile(tile.glLists[pass]);
            NGTRenderer.renderNGTObject(world, tile.blocksObject, false, tile.mode.id, pass);
            GLHelper.endCompile();
        } else if (world.updated) {
            GLHelper.startCompile(tile.glLists[pass]);
            NGTRenderer.renderNGTObject(world, tile.blocksObject, false, tile.mode.id, pass);
            GLHelper.endCompile();
            world.updated = false;
        } else {
            GLHelper.callList(tile.glLists[pass]);
        }
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        GLHelper.enableLighting();
        NGTUtilClient.getMinecraft().entityRenderer.enableLightmap(par3);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);//明るさ戻す
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float par8) {
        this.renderMiniatureAt((TileEntityMiniature) tile, par2, par4, par6, par8);
    }
}