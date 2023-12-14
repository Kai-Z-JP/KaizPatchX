package jp.ngt.rtm.render;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.item.ItemGun;
import jp.ngt.rtm.modelpack.modelset.ModelSetNPCClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class NPCPartsRenderer extends EntityPartsRenderer<ModelSetNPCClient> {
    public float headAngleX;

    public float headAngleY;

    public float headAngleZ;

    public float bodyAngleX;

    public float bodyAngleY;

    public float bodyAngleZ;

    public float leftArmAngleX;

    public float leftArmAngleY;

    public float leftArmAngleZ;

    public float rightArmAngleX;

    public float rightArmAngleY;

    public float rightArmAngleZ;

    public float leftLegAngleX;

    public float leftLegAngleY;

    public float leftLegAngleZ;

    public float rightLegAngleX;

    public float rightLegAngleY;

    public float rightLegAngleZ;

    public NPCPartsRenderer(String... par1) {
        super(par1);
    }

    public void init(ModelSetNPCClient par1, ModelObject par2) {
        super.init(par1, par2);
    }

    public void rotateAndRender(Parts parts, float x, float y, float z, float rotationX, float rotationY, float rotationZ) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(NGTMath.toDegrees(rotationZ), 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(NGTMath.toDegrees(rotationY), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(NGTMath.toDegrees(rotationX), 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(-x, -y, -z);
        parts.render(this);
        GL11.glPopMatrix();
    }

    public void setRotationAngles(EntityLivingBase entity, float partialTicks) {
        if (entity == null)
            return;
        setupRotateCorpse(entity, partialTicks);
        float arg1 = getRendererArg1(entity, partialTicks);
        float arg2 = getRendererArg2(entity, partialTicks);
        float arg3 = getRendererArg3(entity, partialTicks);
        float arg4 = getRendererArg4(entity, partialTicks);
        float arg5 = getRendererArg5(entity, partialTicks);
        int heldItemRight = heldItemRight(entity);
        int heldItemLeft = heldItemLeft(entity);
        float radPIdiv10 = NGTMath.PI / 10.0F;
        this.headAngleY = NGTMath.toRadians(arg4);
        this.headAngleX = NGTMath.toRadians(arg5);
        this.rightArmAngleX = NGTMath.getCos(arg1 * 0.6662F + NGTMath.PI) * 2.0F * arg2 * 0.5F;
        this.leftArmAngleX = NGTMath.getCos(arg1 * 0.6662F) * 2.0F * arg2 * 0.5F;
        this.rightArmAngleZ = 0.0F;
        this.leftArmAngleZ = 0.0F;
        this.rightLegAngleX = NGTMath.getCos(arg1 * 0.6662F) * 1.4F * arg2;
        this.leftLegAngleX = NGTMath.getCos(arg1 * 0.6662F + NGTMath.PI) * 1.4F * arg2;
        this.rightLegAngleY = 0.0F;
        this.leftLegAngleY = 0.0F;
        if (isRiding(entity)) {
            this.rightArmAngleX += -(NGTMath.PI / 5.0F);
            this.leftArmAngleX += -(NGTMath.PI / 5.0F);
            this.rightLegAngleX = -(NGTMath.PI * 2.0F / 5.0F);
            this.leftLegAngleX = -(NGTMath.PI * 2.0F / 5.0F);
            this.rightLegAngleY = radPIdiv10;
            this.leftLegAngleY = -radPIdiv10;
        }
        if (heldItemLeft > 0)
            this.leftArmAngleX = this.leftArmAngleX * 0.5F - radPIdiv10 * heldItemLeft;
        this.rightArmAngleY = 0.0F;
        this.leftArmAngleY = 0.0F;
        switch (heldItemRight) {
            case 1:
                this.rightArmAngleX = this.rightArmAngleX * 0.5F - radPIdiv10 * heldItemRight;
                break;
            case 3:
                this.rightArmAngleX = this.rightArmAngleX * 0.5F - radPIdiv10 * heldItemRight;
                this.rightArmAngleY = -0.5235988F;
                break;
        }
        float swingProgress = getSwingProgress(entity, partialTicks);
        if (swingProgress > 0.0F) {
            this.bodyAngleY = NGTMath.getSin((float) (Math.sqrt(swingProgress) * NGTMath.PI * 2.0D)) * 0.2F;
            this.rightArmAngleY += this.bodyAngleY;
            this.leftArmAngleY += this.bodyAngleY;
            this.leftArmAngleX += this.bodyAngleY;
            float f = 1.0F - swingProgress;
            f = f * f * f * f;
            f = 1.0F - f;
            float f1 = NGTMath.getSin(f * NGTMath.PI);
            float f2 = NGTMath.getSin(swingProgress * NGTMath.PI) * -(this.headAngleX - 0.7F) * 0.75F;
            this.rightArmAngleX = (float) (this.rightArmAngleX - f1 * 1.2D + f2);
            this.rightArmAngleY += this.bodyAngleY * 2.0F;
            this.rightArmAngleZ += NGTMath.getSin(swingProgress * NGTMath.PI) * -0.4F;
        }
        if (isSneak(entity)) {
            this.bodyAngleX = 0.5F;
            this.rightArmAngleX += 0.4F;
            this.leftArmAngleX += 0.4F;
        } else {
            this.bodyAngleX = 0.0F;
        }
        this.rightArmAngleZ += NGTMath.getCos(arg3 * 0.09F) * 0.05F + 0.05F;
        this.leftArmAngleZ -= NGTMath.getCos(arg3 * 0.09F) * 0.05F + 0.05F;
        this.rightArmAngleX += NGTMath.getSin(arg3 * 0.067F) * 0.05F;
        this.leftArmAngleX -= NGTMath.getSin(arg3 * 0.067F) * 0.05F;
        if (aimedBow(entity)) {
            float f3 = 0.0F;
            float f4 = 0.0F;
            this.rightArmAngleZ = 0.0F;
            this.leftArmAngleZ = 0.0F;
            this.rightArmAngleY = -(0.1F - f3 * 0.6F) + this.headAngleY;
            this.leftArmAngleY = 0.1F - f3 * 0.6F + this.headAngleY + 0.4F;
            this.rightArmAngleX = -NGTMath.toRadians(90.0F) + this.headAngleX;
            this.leftArmAngleX = -NGTMath.toRadians(90.0F) + this.headAngleX;
            this.rightArmAngleX -= f3 * 1.2F - f4 * 0.4F;
            this.leftArmAngleX -= f3 * 1.2F - f4 * 0.4F;
            this.rightArmAngleZ += NGTMath.getCos(arg3 * 0.09F) * 0.05F + 0.05F;
            this.leftArmAngleZ -= NGTMath.getCos(arg3 * 0.09F) * 0.05F + 0.05F;
            this.rightArmAngleX += NGTMath.getSin(arg3 * 0.067F) * 0.05F;
            this.leftArmAngleX -= NGTMath.getSin(arg3 * 0.067F) * 0.05F;
        }
        float f0 = this.rightArmAngleY;
        this.rightArmAngleY = this.leftArmAngleY;
        this.leftArmAngleY = f0;
        f0 = this.rightArmAngleZ;
        this.rightArmAngleZ = this.leftArmAngleZ;
        this.leftArmAngleZ = f0;
        f0 = this.rightLegAngleX;
        this.rightLegAngleX = this.leftLegAngleX;
        this.leftLegAngleX = f0;
        f0 = this.rightLegAngleY;
        this.rightLegAngleY = this.leftLegAngleY;
        this.leftLegAngleY = f0;
        f0 = this.rightLegAngleZ;
        this.rightLegAngleZ = this.leftLegAngleZ;
        this.leftLegAngleZ = f0;
        this.headAngleY = -this.headAngleY;
        this.bodyAngleY = -this.bodyAngleY;
    }

    public boolean aimedBow(EntityLivingBase entity) {
        ItemStack heldItem = entity.getHeldItem();
        boolean hasGun = (heldItem != null && heldItem.getItem() instanceof ItemGun);
        boolean usingGun = (hasGun && ((EntityNPC) entity).isUsingItem());
        return usingGun;
    }

    public boolean isRiding(EntityLivingBase entity) {
        return entity.isRiding();
    }

    public int heldItemLeft(EntityLivingBase entity) {
        return 0;
    }

    public int heldItemRight(EntityLivingBase entity) {
        ItemStack heldItem = entity.getHeldItem();
        return heldItem == null ? 0 : 1;
    }

    public float getSwingProgress(EntityLivingBase entity, float partialTicks) {
        return entity.getSwingProgress(partialTicks);
    }

    public boolean isSneak(EntityLivingBase entity) {
        return entity.isSneaking();
    }

    public float getRendererArg1(EntityLivingBase entity, float partialTicks) {
        return entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
    }

    public float getRendererArg2(EntityLivingBase entity, float partialTicks) {
        return entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
    }

    public float getRendererArg3(EntityLivingBase entity, float partialTicks) {
        return entity.ticksExisted + partialTicks;
    }

    public float getRendererArg4(EntityLivingBase entity, float partialTicks) {
        float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float f2 = f1 - f;
        if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase riding = (EntityLivingBase) entity.ridingEntity;
            f = interpolateRotation(riding.prevRenderYawOffset, riding.renderYawOffset, partialTicks);
            f2 = f1 - f;
        }
        return f2;
    }

    public float getRendererArg5(EntityLivingBase entity, float partialTicks) {
        return entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
    }

    public float interpolateRotation(float par1, float par2, float par3) {
        float f;
        for (f = par2 - par1; f < -180.0F; f += 360.0F) ;
        while (f >= 180.0F)
            f -= 360.0F;
        return par1 + par3 * f;
    }

    public void setupRotateCorpse(EntityLivingBase entity, float partialTicks) {
        float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f8 = handleRotationFloat(entity, partialTicks);
        rotateCorpse(entity, f8, f, partialTicks);
    }

    public void rotateCorpse(EntityLivingBase entity, float p_77043_2_, float yaw, float partialTicks) {
        GL11.glRotatef(-yaw, 0.0F, 1.0F, 0.0F);
        if (entity.deathTime > 0) {
            float f = (float) NGTMath.firstSqrt(((entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F));
            if (f > 1.0F)
                f = 1.0F;
            GL11.glRotatef(f * getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
        }
    }

    public float handleRotationFloat(EntityLivingBase entity, float partialTicks) {
        return entity.ticksExisted + partialTicks;
    }

    public float getDeathMaxRotation(EntityLivingBase entity) {
        return 90.0F;
    }
}
