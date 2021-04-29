package jp.ngt.rtm.entity.train.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.modelset.ModelSetContainerClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderContainer extends Render {
    private void renderContainer(EntityContainer entity, double par2, double par4, double par6, float par8, float par9) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);
        EntityVehicleBase vehicle = entity.getVehicle();
        if (vehicle != null) {
            float yaw = vehicle.prevRotationYaw + MathHelper.wrapAngleTo180_float(vehicle.rotationYaw - vehicle.prevRotationYaw) * par9;
            GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
            float pitch = vehicle.prevRotationPitch + (vehicle.rotationPitch - vehicle.prevRotationPitch) * par9;
            GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);
            float roll = vehicle.prevRotationRoll + (vehicle.rotationRoll - vehicle.prevRotationRoll) * par9;
            GL11.glRotatef(roll, 0.0F, 0.0F, 1.0F);
        } else {
            GL11.glRotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-entity.rotationPitch, 1.0F, 0.0F, 0.0F);
        }
        ModelSetContainerClient modelSet = (ModelSetContainerClient) entity.getModelSet();
        if (modelSet == null || modelSet.isDummy()) {
            RTMCore.proxy.renderMissingModel();
        } else {
            this.bindTexture(modelSet.texture);
            modelSet.model.renderAll(modelSet.getConfig().smoothing);
        }
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
        this.renderContainer((EntityContainer) par1, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity par1) {
        return null;
    }

    @Override
    protected void bindEntityTexture(Entity entiy) {
    }
}