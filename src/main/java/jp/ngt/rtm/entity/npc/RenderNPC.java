package jp.ngt.rtm.entity.npc;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.rtm.item.ItemGun;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.util.Calendar;

@SideOnly(Side.CLIENT)
public class RenderNPC extends RenderBiped {
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/motorman.png");
    private static final ResourceLocation tex_santa = new ResourceLocation("rtm", "textures/motorman_santa.png");
    private static final ResourceLocation tex_shishi = new ResourceLocation("rtm", "textures/motorman_shishi.png");

    private int textureType;

    public RenderNPC() {
        super(new ModelBiped(), 0.5F);
        this.setRenderPassModel(new ModelBiped());

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (month == 1 && day >= 1 && day <= 3) {
            this.textureType = 1;
        } else if (month == 12 && day >= 24 && day <= 26) {
            this.textureType = 12;
        }
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float par8, float par9) {
        super.doRender(entity, x, y, z, par8, par9);

        ItemStack heldItem = entity.getHeldItem();
        boolean hasGun = (heldItem != null && heldItem.getItem() instanceof ItemGun);
        boolean usingGun = hasGun && ((EntityNPC) entity).isUsingItem();
        this.field_82423_g.aimedBow = this.field_82425_h.aimedBow = this.modelBipedMain.aimedBow = usingGun;
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int par2, float par3) {
        EntityNPC npc = (EntityNPC) entity;
        if (npc.isMotorman()) {
            return -1;
        }

        ResourceLocation tex = npc.getModelSet().lightTexture;
        if (tex == null) {
            return -1;
        }

        GL11.glDepthMask(!entity.isInvisible());

        if (par2 == 1) {
            this.bindTexture(tex);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GLHelper.setLightmapMaxBrightness();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            return 1;
        } else if (par2 == 2) {
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        }

        return -1;
    }

    @Override
    protected void renderEquippedItems(EntityLiving entity, float par2) {
        ItemStack heldItem = entity.getHeldItem();
        boolean hasGun = (heldItem != null && heldItem.getItem() instanceof ItemGun);
        if (!(hasGun && ((EntityNPC) entity).isUsingItem())) {
            super.renderEquippedItems(entity, par2);
            return;
        }

        /*************************************************************/

        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        ItemStack headItem = entity.func_130225_q(3);

        if (headItem != null) {
            GL11.glPushMatrix();
            this.modelBipedMain.bipedHead.postRender(0.0625F);
            Item item = headItem.getItem();

            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(headItem, IItemRenderer.ItemRenderType.EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(IItemRenderer.ItemRenderType.EQUIPPED, headItem, IItemRenderer.ItemRendererHelper.BLOCK_3D));

            if (item instanceof ItemBlock) {
                if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType())) {
                    float f1 = 0.625F;
                    GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(f1, -f1, -f1);
                }

                this.renderManager.itemRenderer.renderItem(entity, headItem, 0);
            } else if (item == Items.skull) {
                float f1 = 1.0625F;
                GL11.glScalef(f1, -f1, -f1);
                GameProfile gameprofile = null;

                if (headItem.hasTagCompound()) {
                    NBTTagCompound nbt = headItem.getTagCompound();

                    if (nbt.hasKey("SkullOwner", 10)) {
                        gameprofile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"));
                    } else if (nbt.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(nbt.getString("SkullOwner"))) {
                        gameprofile = new GameProfile(null, nbt.getString("SkullOwner"));
                    }
                }

                TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 180.0F, headItem.getItemDamage(), gameprofile);
            }

            GL11.glPopMatrix();
        }

        if (heldItem != null && heldItem.getItem() != null) {
            //Item item = heldItem.getItem();
            GL11.glPushMatrix();

            if (this.mainModel.isChild) {
                float f1 = 0.5F;
                GL11.glTranslatef(0.0F, 0.625F, 0.0F);
                GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
                GL11.glScalef(f1, f1, f1);
            }

            this.modelBipedMain.bipedRightArm.postRender(0.0625F);
            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

            //if(item == Items.bow)
            {
                float f1 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f1, -f1, f1);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }

            if (heldItem.getItem().requiresMultipleRenderPasses()) {
                for (int i = 0; i < heldItem.getItem().getRenderPasses(heldItem.getItemDamage()); ++i) {
                    int j = heldItem.getItem().getColorFromItemStack(heldItem, i);
                    float f5 = (float) (j >> 16 & 255) / 255.0F;
                    float f2 = (float) (j >> 8 & 255) / 255.0F;
                    float f3 = (float) (j & 255) / 255.0F;
                    GL11.glColor4f(f5, f2, f3, 1.0F);
                    this.renderManager.itemRenderer.renderItem(entity, heldItem, i);
                }
            } else {
                int i = heldItem.getItem().getColorFromItemStack(heldItem, 0);
                float f4 = (float) (i >> 16 & 255) / 255.0F;
                float f5 = (float) (i >> 8 & 255) / 255.0F;
                float f2 = (float) (i & 255) / 255.0F;
                GL11.glColor4f(f4, f5, f2, 1.0F);
                this.renderManager.itemRenderer.renderItem(entity, heldItem, 0);
            }

            GL11.glPopMatrix();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        EntityNPC npc = (EntityNPC) entity;
        if (npc.isMotorman()) {
            switch (this.textureType) {
                case 1:
                    return tex_shishi;
                case 12:
                    return tex_santa;
                default:
                    return texture;
            }
        } else {
            return npc.getModelSet().texture;
        }
    }
}