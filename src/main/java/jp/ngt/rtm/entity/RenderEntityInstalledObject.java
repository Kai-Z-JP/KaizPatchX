package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.electric.EntityElectricalWiring;
import jp.ngt.rtm.electric.RenderElectricalWiring;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachineClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderEntityInstalledObject extends Render {
    public static final RenderEntityInstalledObject INSTANCE = new RenderEntityInstalledObject();

    private RenderEntityInstalledObject() {
    }

    private void renderEntityInstalledObject(EntityInstalledObject entity, double par2, double par4, double par6, float par8, float par9) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);

        if (entity instanceof EntityElectricalWiring) {
            TileEntity tile = ((EntityElectricalWiring) entity).tileEW;
            int pass = MinecraftForgeClient.getRenderPass();
            if (tile.shouldRenderInPass(pass)) {
                try {
                    TileEntitySpecialRenderer renderer = RenderElectricalWiring.INSTANCE;
                    renderer.renderTileEntityAt(tile, 0, 0, 0, par9);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReport.makeCrashReport(throwable, "Rendering TileEntity");
                    CrashReportCategory category = report.makeCategory("TileEntity Details");
                    tile.func_145828_a(category);
                    throw new ReportedException(report);
                }
            }
        }
        GL11.glRotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);

        ModelSetMachineClient modelSet = (ModelSetMachineClient) entity.getModelSet();
        MachineConfig cfg = modelSet.getConfig();
        if (cfg.followRailAngle) {
            GL11.glRotatef(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(entity.rotationRoll, 0.0F, 0.0F, 1.0F);
        }
        int pass = MinecraftForgeClient.getRenderPass();
        modelSet.modelObj.render(entity, cfg, pass, par9);

        GL11.glPopMatrix();
    }

    /*@Override
    public boolean isStaticEntity()//DisplayListに入れられる
    {
        return true;//たまにStack overflow
    }*/

    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderEntityInstalledObject((EntityInstalledObject) entity, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}