package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.render.RailPartsRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderLargeRail extends TileEntitySpecialRenderer {
    public static final RenderLargeRail INSTANCE = new RenderLargeRail();

    protected RenderLargeRail() {
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d0, double d1, double d2, float f) {
        this.renderTileEntityLargeRail((TileEntityLargeRailCore) tileEntity, d0, d1, d2, f);
    }

    private void renderTileEntityLargeRail(TileEntityLargeRailCore tileEntity, double par2, double par4, double par6, float par8) {
        if (!tileEntity.isLoaded()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);

        try {
            ModelSetRailClient modelSet = (ModelSetRailClient) tileEntity.getProperty().getModelSet();
            RailPartsRenderer renderer = (RailPartsRenderer) modelSet.model.renderer;
            renderer.renderRail(tileEntity, par2, par4, par6, par8);
        } catch (ClassCastException ignored) {
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}