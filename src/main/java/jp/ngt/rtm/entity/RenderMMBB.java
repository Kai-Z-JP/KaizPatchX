package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderMMBB extends Render {
    @Override
    public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_) {
        //this.renderAABB(entity, x, y, z);
    }

    private void renderAABB(Entity entity, double x, double y, double z) {
        GL11.glPushMatrix();
        renderOffsetAABB(entity.boundingBox, x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}