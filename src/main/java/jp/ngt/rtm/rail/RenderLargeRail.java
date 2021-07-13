package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.rail.util.RailProperty;
import jp.ngt.rtm.render.RailPartsRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class RenderLargeRail extends TileEntitySpecialRenderer {
    public static final RenderLargeRail INSTANCE = new RenderLargeRail();

    protected RenderLargeRail() {
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d0, double d1, double d2, float f) {
        this.renderTileEntityLargeRail((TileEntityLargeRailBase) tileEntity, d0, d1, d2, f);
    }

    private void renderTileEntityLargeRail(TileEntityLargeRailBase tileEntity, double par2, double par4, double par6, float par8) {
        TileEntityLargeRailCore core = tileEntity.getRailCore();

        if (core == null || !core.isLoaded() || core.shouldRender(tileEntity)) {
            return;
        }
        core.setTickRender(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef(core.xCoord - tileEntity.xCoord, core.yCoord - tileEntity.yCoord, core.zCoord - tileEntity.zCoord);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);

        try {
            ModelSetRailClient modelSet = (ModelSetRailClient) core.getProperty().getModelSet();
            RailPartsRenderer renderer = (RailPartsRenderer) modelSet.model.renderer;
            renderer.renderRail(core, 0, par2, par4, par6, par8);
            IntStream.range(0, core.subRails.size()).forEach(i -> {
                RailProperty property = core.subRails.get(i);
                RailPartsRenderer subRenderer = (RailPartsRenderer) ((ModelSetRailClient) property.getModelSet()).model.renderer;
                subRenderer.renderRail(core, i + 1, par2, par4, par6, par8);
            });
        } catch (ClassCastException ignored) {
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}