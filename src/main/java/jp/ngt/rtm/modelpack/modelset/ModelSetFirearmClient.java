package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.gui.GuiButtonSelectModel;
import jp.ngt.rtm.gui.GuiSelectModel;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.FirearmConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelSetFirearmClient extends ModelSetFirearm implements IModelSetClient {
    public final IModelNGT model;
    public final ResourceLocation texture;
    public final ResourceLocation buttonTexture;

    public ModelSetFirearmClient() {
        super();
        this.model = ModelLoader.loadModel(new ResourceLocation("models/ModelFirearm_40cmArtillery.obj"), VecAccuracy.LOW);
        this.texture = ModelPackManager.INSTANCE.getResource("textures/firearm/40cmArtillery.png");
        this.buttonTexture = ModelPackManager.INSTANCE.getResource("textures/firearm/button_40cmArtillery.png");
    }

    public ModelSetFirearmClient(FirearmConfig par1) {
        super(par1);
        this.model = ModelPackManager.INSTANCE.loadModel(par1.firearmModel, GL11.GL_TRIANGLES, true, par1);
        this.texture = ModelPackManager.INSTANCE.getResource(par1.firearmTexture);
        this.buttonTexture = ModelPackManager.INSTANCE.getResource(par1.buttonTexture);
    }

    @Override
    public void renderSelectButton(GuiButtonSelectModel par1, Minecraft par2, int par3, int par4) {
        GL11.glPushMatrix();

        par2.getTextureManager().bindTexture(this.buttonTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean b0 = par3 >= par1.xPosition && par4 >= par1.yPosition && par3 < par1.xPosition + par1.width && par4 < par1.yPosition + par1.height;
        int k = par1.getHoverState(b0);
        par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 0, 160, 32);

        if (k == 2) {
            par2.getTextureManager().bindTexture(GuiSelectModel.ButtonBlue);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 0, 160, 32);
            GL11.glDisable(GL11.GL_BLEND);

            GL11.glTranslatef(par1.xPosition + 240, par1.yPosition + 16, 0.0F);
            GuiSelectModel.renderModel(this, par2);
        } else if (par1.isSelected) {
            par2.getTextureManager().bindTexture(GuiSelectModel.ButtonBlue);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            par1.drawTexturedModalRect(par1.xPosition, par1.yPosition, 0, 32, 160, 32);
            GL11.glDisable(GL11.GL_BLEND);
        }

        //FontRenderer fr = par2.fontRenderer;
        //fr.drawString(par1.displayString, (par1.xPosition + par1.width / 2) - fr.getStringWidth(par1.displayString) / 2, par1.yPosition + (par1.height - 8) / 2, 0x000000);

        GL11.glPopMatrix();
    }

    @Override
    public void renderModelInGui(Minecraft par1) {
        GL11.glTranslatef(3.0F, -1.0F, -10.0F);//X:右が+, Z:手前が+
        GL11.glRotatef(-60.0F, 0.0F, 1.0F, 0.0F);
        //GL11.glScalef(1.0F, 1.0F, 1.0F);

        par1.getTextureManager().bindTexture(this.texture);
        this.model.renderAll(RTMConfig.smoothing);
    }
}