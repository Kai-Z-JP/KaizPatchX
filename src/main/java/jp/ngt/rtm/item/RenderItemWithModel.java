package jp.ngt.rtm.item;

import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.ModelConfig;
import jp.ngt.rtm.modelpack.modelset.IModelSetClient;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderItemWithModel implements IItemRenderer {
    public static RenderItemWithModel INSTANCE = new RenderItemWithModel();

    private RenderItemWithModel() {
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return this.shouldRenderCustomIcon(item);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        NGTUtilClient.bindTexture(this.getCustomIconResource(item));
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            ItemRenderer.renderItemIn2D(Tessellator.instance, 1.0F, 0.0F, 0.0F, 1.0F, 256, 256, 0.0625F);
        } else if (type == ItemRenderType.ENTITY) {
            if (!item.isOnItemFrame() && data[1] instanceof EntityItem) {
                EntityItem entityItem = (EntityItem) data[1];
                GL11.glRotatef((((float) entityItem.age) / 20.0F + entityItem.hoverStart) * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0.0F, MathHelper.sin(((float) entityItem.age) / 10.0F + entityItem.hoverStart) * 0.1F + 0.1F, 0.0F);
            }
            GL11.glTranslatef(-0.5F, -0.1F, 0.0F);
            ItemRenderer.renderItemIn2D(Tessellator.instance, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        } else if (type == ItemRenderType.EQUIPPED) {
            ItemRenderer.renderItemIn2D(Tessellator.instance, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        } else if (type == ItemRenderType.INVENTORY) {
            GL11.glDisable(GL11.GL_LIGHTING);
            this.renderQuad();
        }

        GL11.glPopMatrix();
    }

    private void renderQuad() {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, 16, RenderItem.getInstance().zLevel, 0, 1);
        tessellator.addVertexWithUV(16, 16, RenderItem.getInstance().zLevel, 1, 1);
        tessellator.addVertexWithUV(16, 0, RenderItem.getInstance().zLevel, 1, 0);
        tessellator.addVertexWithUV(0, 0, RenderItem.getInstance().zLevel, 0, 0);
        tessellator.draw();
    }


    private boolean shouldRenderCustomIcon(ItemStack item) {
        ItemWithModel itemWithModel = (ItemWithModel) item.getItem();
        String type = itemWithModel.getModelType(item);
        if (type.isEmpty()) {
            return false;
        }
        String name = itemWithModel.getModelName(item);
        if (name.isEmpty()) {
            return false;
        }
        ModelSetBase<?> modelSet = ModelPackManager.INSTANCE.getModelSet(type, name);
        if (modelSet instanceof IModelSetClient) {
            ModelConfig config = modelSet.getConfig();
            return config != null && config.customIconTexture != null;
        } else {
            return false;
        }
    }

    private ResourceLocation getCustomIconResource(ItemStack item) {
        return this.getResourceLocation(this.getModelSet(item).getConfig().customIconTexture);
    }

    private ModelSetBase<?> getModelSet(ItemStack item) {
        ItemWithModel itemWithModel = (ItemWithModel) item.getItem();
        String type = itemWithModel.getModelType(item);
        String name = itemWithModel.getModelName(item);
        return ModelPackManager.INSTANCE.getModelSet(type, name);
    }

    private ResourceLocation getResourceLocation(String location) {
        if (location.contains(":")) {
            String[] sa = location.split(":");
            return new ResourceLocation(sa[0], sa[1]);
        } else {
            return new ResourceLocation(location);
        }
    }
}
