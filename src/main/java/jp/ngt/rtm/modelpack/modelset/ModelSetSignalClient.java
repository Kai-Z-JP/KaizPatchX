package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.rtm.gui.GuiButtonSelectModel;
import jp.ngt.rtm.gui.GuiSelectModel;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.SignalConfig;
import jp.ngt.rtm.render.BasicSignalPartsRenderer;
import jp.ngt.rtm.render.ModelObject;
import jp.ngt.rtm.render.PartsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelSetSignalClient extends ModelSetSignal implements IModelSetClient {
	public final ModelObject model;
	public final ResourceLocation buttonTexture;

	public ModelSetSignalClient() {
		super();
		this.model = new ModelObject(ModelLoader.loadModel(new ResourceLocation("models/ModelSignal_4color.mqo"), VecAccuracy.LOW),
				new TextureSet[]{new TextureSet(new Material((byte) 0, ModelPackManager.INSTANCE.getResource("textures/signal/colorSignal.png")),
						1, false, "textures/signal/colorSignal.png")}, this);
		this.buttonTexture = ModelPackManager.INSTANCE.getResource("textures/signal/button_4cB.png");
	}

	public ModelSetSignalClient(SignalConfig par1) {
		super(par1);
		PartsRenderer renderer = (!PartsRenderer.validPath(par1.model.rendererPath)) ? new BasicSignalPartsRenderer(par1) : null;
		this.model = new ModelObject(par1.model, this, renderer);
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

		GL11.glPopMatrix();
	}

	@Override
	public void renderModelInGui(Minecraft par1) {
		GL11.glTranslatef(3.0F, -2.0F, -10.0F);//X:右が+, Z:手前が+
		GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
		GL11.glScalef(5.0F, 5.0F, 5.0F);

		SignalConfig cfg = this.getConfig();
		this.model.render(null, cfg, 0, 0.0F);
		this.model.render(null, cfg, 1, 0.0F);
	}
}