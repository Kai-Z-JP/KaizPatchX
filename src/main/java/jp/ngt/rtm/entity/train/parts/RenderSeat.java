package jp.ngt.rtm.entity.train.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.util.RenderUtil;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderSeat extends Render {
    private final ModelCrossSeat model = new ModelCrossSeat();
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/train/seat.png");

    private void renderSeat(EntityFloor entity, double par2, double par4, double par6, float par8, float par9) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);

        RenderUtil.enableCustomLighting(0, 0.0F, 2.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.renderSeat(entity);
        RenderUtil.disableCustomLighting(0);

        GL11.glPopMatrix();
    }

    private void renderSeat(EntityFloor entity) {
        byte seatType = entity.getSeatType();
        if (entity.getVehicle() == null) {
            RTMCore.proxy.renderMissingModel();
        } else if (seatType == 1 || seatType == 3) {
            //Light[] lights = entity.getTrain().getTrainModelSet().getConfig().interiorLights;

            GL11.glRotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);

            this.bindTexture(texture);
            //-17~+17
            this.model.Shape3.rotateAngleX = (float) Math.toRadians((float) entity.getVehicle().seatRotation * 17.0F / (float) EntityVehicleBase.MAX_SEAT_ROTATION);
            this.model.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        } else if (seatType == 3)//寝台
        {
        }
    }

    @Override
    public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
        this.renderSeat((EntityFloor) par1, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return texture;
    }
}