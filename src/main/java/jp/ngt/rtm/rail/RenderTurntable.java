package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.render.RailPartsRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderTurntable extends RenderLargeRail {
    public RenderTurntable() {
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d0, double d1, double d2, float f) {
        this.renderTurntable((TileEntityTurnTableCore) tileEntity, d0, d1, d2, f);
    }

    private void renderTurntable(TileEntityTurnTableCore tileEntity, double par2, double par4, double par6, float par8) {
        if (!tileEntity.isLoaded()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPushAttrib(8256);//GlStateManager参照

        ModelSetRailClient modelSet = (ModelSetRailClient) tileEntity.getProperty().getModelSet();
        if (modelSet != null && !modelSet.isDummy()) {
            GLHelper.disableLighting();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderLargeRail.BLOCK_RENDERER.renderRailBlocks(tileEntity, par2, par4, par6, par8);
            GLHelper.enableLighting();
            NGTUtilClient.getMinecraft().entityRenderer.enableLightmap(0.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glTranslatef((float) (par2 + 0.5F), (float) (par4), (float) (par6 + 0.5F));
            GL11.glRotatef(tileEntity.getRotation(), 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-(float) (par2 + 0.5F), -(float) (par4), -(float) (par6 + 0.5F));

            try {
                RailPartsRenderer renderer = (RailPartsRenderer) modelSet.model.renderer;
                renderer.renderRail(tileEntity, par2, par4, par6, par8);
            } catch (ClassCastException ignored) {
            }
        }

        GL11.glPopAttrib();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}