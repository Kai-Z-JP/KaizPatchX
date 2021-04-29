package jp.ngt.rtm.entity.train.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderTie extends Render {
    private final ModelBase model = new ModelTie();
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/train/tie.png");

    private void renderTie(EntityTie par1Entity, double par2, double par4, double par6, float par8, float par9) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);
        GL11.glRotatef(par1Entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-par1Entity.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        this.bindTexture(texture);
        this.model.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
        this.renderTie((EntityTie) par1, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return texture;
    }

    public static class ModelTie extends ModelBase {
        ModelRenderer shape;

        public ModelTie() {
            this.textureWidth = 256;
            this.textureHeight = 64;

            this.shape = new ModelRenderer(this, 0, 0);
            this.shape.addBox(-20F, -2F, -20F, 40, 2, 40);
            this.shape.setRotationPoint(0F, 0F, 0F);
            this.shape.setTextureSize(256, 64);
            this.shape.mirror = true;
            this.setRotation(this.shape, 0F, 0F, 0F);
        }

        @Override
        public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
            super.render(null, f, f1, f2, f3, f4, f5);
            this.setRotationAngles(f, f1, f2, f3, f4, f5);
            this.shape.render(f5);
        }

        private void setRotation(ModelRenderer model, float x, float y, float z) {
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }

        public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
            super.setRotationAngles(f, f1, f2, f3, f4, f5, null);
        }
    }
}