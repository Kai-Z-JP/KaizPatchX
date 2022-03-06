package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.rtm.gui.GuiButtonSelectModel;
import jp.ngt.rtm.gui.GuiSelectModel;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.VehicleBaseConfig;
import jp.ngt.rtm.modelpack.model.ModelTrain_kiha600;
import jp.ngt.rtm.render.BasicVehiclePartsRenderer;
import jp.ngt.rtm.render.ModelObject;
import jp.ngt.rtm.render.PartsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.script.ScriptEngine;

@SideOnly(Side.CLIENT)
public abstract class ModelSetVehicleBaseClient<T extends VehicleBaseConfig> extends ModelSetVehicleBase<T> implements IModelSetClient {
    public final ModelObject vehicleModel;
    public final ResourceLocation buttonTexture;
    public final ResourceLocation rollsignTexture;

    public final ResourceLocation sound_Stop;
    public final ResourceLocation sound_S_A;
    public final ResourceLocation sound_Acceleration;
    public final ResourceLocation sound_Deceleration;
    public final ResourceLocation sound_D_S;
    public final ResourceLocation sound_Horn;

    public ScriptEngine se;

    public ModelSetVehicleBaseClient() {
        super();
        TextureSet tex = new TextureSet(new Material((byte) 0, ModelPackManager.INSTANCE.getResource("textures/train/hoge.png")), 0, false);
        this.vehicleModel = new ModelObject(new ModelTrain_kiha600(), new TextureSet[]{tex}, this);
        this.buttonTexture = ModelPackManager.INSTANCE.getResource("textures/train/hoge.png");
        this.rollsignTexture = null;

        this.sound_Stop = null;
        this.sound_S_A = null;
        this.sound_Acceleration = null;
        this.sound_Deceleration = null;
        this.sound_D_S = null;
        this.sound_Horn = null;
    }

    public ModelSetVehicleBaseClient(VehicleBaseConfig cfg) {
        super(cfg);
        PartsRenderer renderer = (!PartsRenderer.validPath(cfg.getModel().rendererPath)) ? new BasicVehiclePartsRenderer(String.valueOf(true)) : null;
        this.vehicleModel = new ModelObject(cfg.getModel(), this, renderer);

        this.buttonTexture = ModelPackManager.INSTANCE.getResource(cfg.buttonTexture);
        this.rollsignTexture = cfg.rollsignTexture == null ? null : ModelPackManager.INSTANCE.getResource(cfg.rollsignTexture);

        this.sound_Stop = this.getSoundResource(cfg.sound_Stop);
        this.sound_S_A = this.getSoundResource(cfg.sound_S_A);
        this.sound_Acceleration = this.getSoundResource(cfg.sound_Acceleration);
        this.sound_Deceleration = this.getSoundResource(cfg.sound_Deceleration);
        this.sound_D_S = this.getSoundResource(cfg.sound_D_S);
        this.sound_Horn = this.getSoundResource(cfg.sound_Horn);

        if (cfg.soundScriptPath != null) {
            this.se = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(cfg.soundScriptPath));
        }
        this.finishConstruct();
    }

    @Override
    public void renderSelectButton(GuiButtonSelectModel par1, Minecraft par2, int par3, int par4) {
        GL11.glPushMatrix();

        par2.getTextureManager().bindTexture(this.buttonTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean b0 = par3 >= par1.xPosition && par4 >= par1.yPosition && par3 < par1.xPosition + par1.width && par4 < par1.yPosition + par1.height;
        int k = par1.getHoverState(b0);
        par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 0, 160, 32);

        if (k == 2)//マウスオーバー
        {
            par2.getTextureManager().bindTexture(GuiSelectModel.ButtonBlue);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 0, 160, 32);
            GL11.glDisable(GL11.GL_BLEND);

            GL11.glTranslatef(par1.xPosition + 320, par1.yPosition + 16, 0.0F);
            GuiSelectModel.renderModel(this, par2);

            /*GL11.glPushMatrix();
            GL11.glTranslatef(340.0F, (float)(this.height / 2) + 10.0F, 50.0F);//x300
            GL11.glScalef(-20.0F, 20.0F, 20.0F);//15
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            //RenderHelper.enableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glRotatef(-40.0F, 0.0F, 1.0F, 0.0F);
            //GL11.glRotatef(5.0F, 1.0F, 0.0F, 0.0F);
            this.renderTrainModel(ClientProxy.getMinecraft());
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);*/
        } else if (par1.isSelected) {
            par2.getTextureManager().bindTexture(GuiSelectModel.ButtonBlue);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 32, 160, 32);
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }

    @Override
    public void renderModelInGui(Minecraft par1) {
        GL11.glTranslatef(11.0F, -1.0F, -12.0F);//X:右が+, Z:手前が+
        GL11.glRotatef(-65.0F, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(1.2F, 1.2F, 1.2F);

        VehicleBaseConfig cfg = this.cfg;
        this.vehicleModel.render(null, cfg, 0, 0.0F);
        this.vehicleModel.render(null, cfg, 1, 0.0F);
        this.renderPartsInGui(par1);
    }

    protected void renderPartsInGui(Minecraft par1) {
    }

    @Override
    public IModelNGT getModelObject() {
        return this.vehicleModel.model;
    }
}