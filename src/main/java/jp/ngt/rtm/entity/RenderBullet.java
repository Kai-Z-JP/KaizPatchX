package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.PolygonModel;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.item.ItemAmmunition.BulletType;
import jp.ngt.rtm.item.ItemGun;
import jp.ngt.rtm.item.ItemGun.GunType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderBullet extends Render {
    private final PolygonModel model = ModelLoader.loadModel(new ResourceLocation("rtm", "models/Model_Cannonball.obj"), VecAccuracy.MEDIUM);
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/cannonball.png");
    private static final ResourceLocation tex_flash = new ResourceLocation("rtm", "textures/effect/muzzleFlash.png");

    public static RenderBullet INSTANCE = new RenderBullet();

    private RenderBullet() {
    }

    private void renderBullet(EntityBullet entity, double par2, double par4, double par6, float par8, float par9) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);

        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);

        BulletType type = entity.getBulletType();
        boolean brightBullet = ((type == BulletType.rifle_5_56mm || type == BulletType.rifle_7_62mm || type == BulletType.rifle_12_7mm) && entity.getCanBreakBlock());

        if (brightBullet) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if (type == BulletType.rifle_12_7mm) {
                GL11.glScalef(0.1F, 1.5F, 0.1F);
            } else {
                GL11.glScalef(0.05F, 0.75F, 0.05F);
            }

            GLHelper.disableLighting();
            GLHelper.setLightmapMaxBrightness();
            GL11.glColor4f(1.0F, 1.0F, 0.25F, 1.0F);
        } else if (type == BulletType.handgun_9mm || type == BulletType.rifle_5_56mm || type == BulletType.rifle_7_62mm) {
            this.bindTexture(texture);
            GL11.glScalef(0.05F, 0.05F, 0.05F);
        } else if (type == BulletType.rifle_12_7mm) {
            this.bindTexture(texture);
            GL11.glScalef(0.1F, 0.1F, 0.1F);
        }

        this.model.renderAll(false);

        if (brightBullet) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLHelper.enableLighting();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    private void renderMuzzleFlash(EntityLivingBase entity, double x, double y, double z, boolean firstPersonView) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glRotatef(-entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, 1.6240F - entity.yOffset + entity.ySize, 0.0F);
        GL11.glRotatef(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(-0.3115F, 0.0F, 0.9F);

        GLHelper.disableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GLHelper.setLightmapMaxBrightness();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        this.bindTexture(tex_flash);
        double d0 = 0.75D;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-d0, d0, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-d0, -d0, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(d0, -d0, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(d0, d0, 0.0D, 0.0D, 0.0D);
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLHelper.enableLighting();

        GL11.glPopMatrix();
    }

    public void onPlayerRender(EntityPlayer player, boolean firstPersonView) {
        int useCount = player.getItemInUseCount();
        if (useCount % ItemGun.INTERVAL > 0) {
            ItemStack stack = player.inventory.getCurrentItem();
            if (stack != null && stack.getItem() instanceof ItemGun) {
                GunType gunType = ((ItemGun) stack.getItem()).gunType;
                if (gunType.rapidFire || useCount == gunType.useDuration - 1) {
                    RenderBullet.INSTANCE.renderMuzzleFlash(player, 0.0D, 0.0D, 0.0D, firstPersonView);
                }
            }
        }
    }

    public void onNPCRender(EntityNPC npc, double x, double y, double z) {
        int useCount = npc.getItemUseCount();
        if (useCount % ItemGun.INTERVAL > 0) {
            ItemStack stack = npc.getHeldItem();
            if (stack != null && stack.getItem() instanceof ItemGun) {
                GunType gunType = ((ItemGun) stack.getItem()).gunType;
                if (gunType.rapidFire || useCount == gunType.useDuration - 1) {
                    RenderBullet.INSTANCE.renderMuzzleFlash(npc, x, y, z, false);
                }
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity par1) {
        return texture;
    }

    @Override
    public void doRender(Entity par1, double par2, double par4, double par6, float par8, float par9) {
        this.renderBullet((EntityBullet) par1, par2, par4, par6, par8, par9);
    }
}