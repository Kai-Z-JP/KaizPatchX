package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignalClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderSignal extends TileEntitySpecialRenderer {
    public void renderTileEntitySignalAt(TileEntitySignal tileEntity, double par2, double par4, double par6, float par8) {
        ModelSetSignalClient modelSet = (ModelSetSignalClient) tileEntity.getModelSet();

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);//Block&TileEntity描画のため、座標移動に+0.5しない

        GL11.glPushMatrix();
        GL11.glTranslatef(0.5F, 0.0F, 0.5F);
        GL11.glTranslatef(tileEntity.getOffsetX(), tileEntity.getOffsetY(), tileEntity.getOffsetZ());
        float dir = tileEntity.getBlockDirection();
        GL11.glRotatef(dir, 0.0F, 1.0F, 0.0F);
        int pass = MinecraftForgeClient.getRenderPass();
        modelSet.model.render(tileEntity, modelSet.getConfig(), pass, par8);

        GL11.glPopMatrix();
        this.renderBaseTileEntity(tileEntity, par2, par4, par6, pass, dir);
        GL11.glPopMatrix();
    }

    private void renderBaseTileEntity(TileEntitySignal signal, double x, double y, double z, int pass, float partialTicks) {
        TileEntity tile = signal.getOrigTileEntity();
        if (tile == null) {
            return;
        }

        tile.setWorldObj(signal.getWorldObj());
        TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);

        if (tile.shouldRenderInPass(pass) && renderer != null) {
            try {
                renderer.renderTileEntityAt(tile, 0, 0, 0, partialTicks);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                CrashReport report = CrashReport.makeCrashReport(throwable, "Rendering TileEntity in Miniature");
                CrashReportCategory category = report.makeCategory("TileEntity Details");
                tile.func_145828_a(category);
                throw new ReportedException(report);
            }
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderTileEntitySignalAt((TileEntitySignal) tileEntity, par2, par4, par6, par8);
    }
}