package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.ClientProxy;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import jp.ngt.rtm.util.RTMUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiIngameCustom extends GuiScreen {
	private static final ResourceLocation tex_cab = new ResourceLocation("rtm", "textures/gui/cab.png");
	private static final ResourceLocation tex_scope = new ResourceLocation("rtm", "textures/gui/scope.png");
	private static final ResourceLocation tex_nvd = new ResourceLocation("rtm", "textures/gui/nvd.png");

	public GuiIngameCustom(Minecraft par1) {
		super();
		this.mc = par1;
	}

	public void onRenderGui(RenderGameOverlayEvent.Pre event) {
		if (event.type == ElementType.HOTBAR) {
			if (!this.mc.thePlayer.isRiding() || this.mc.gameSettings.thirdPersonView != 0) {
				return;
			}

			this.setScale(event.resolution);

			if (this.mc.thePlayer.ridingEntity instanceof EntityTrainBase) {
				this.renderTrainGui((EntityTrainBase) this.mc.thePlayer.ridingEntity);
			} else if (this.mc.thePlayer.ridingEntity instanceof EntityArtillery) {
				this.renderArtilleryGui((EntityArtillery) this.mc.thePlayer.ridingEntity);
			}
		} else if (event.type == ElementType.HELMET) {
			byte viewMode = ClientProxy.getViewMode(this.mc.thePlayer);
			if (viewMode >= 0) {
				int k = event.resolution.getScaledWidth();
				int l = event.resolution.getScaledHeight();
				if (viewMode == 3) {
					this.renderNVD(k, l);
				} else {
					this.renderScope(k, l);
				}
				event.setCanceled(true);
			}
		} else if (event.type == ElementType.DEBUG) {
			//if(this.mc.gameSettings.showDebugInfo)
			this.setScale(event.resolution);
			this.renderDebugGui();
		}

		//fontrenderer.drawStringWithShadow("viewY : " + RenderManager.instance.playerViewY, 2, 2, 16777215);
		//fontrenderer.drawStringWithShadow("viewX : " + RenderManager.instance.playerViewX, 2, 12, 16777215);
	}

	private void renderTrainGui(EntityTrainBase train) {
		FontRenderer fontrenderer = this.mc.fontRenderer;
		ModelSetVehicleBase<TrainConfig> model = train.getModelSet();
		if (model != null && !model.getConfig().notDisplayCab) {
			this.mc.getTextureManager().bindTexture(tex_cab);
			int k = this.width / 2;
			this.drawTexturedModalRect(k - 208, this.height - 48, 0, 0, 416, 48);
			this.drawMeterAndLever(train);
			this.drawWatch();

			//Speed
			fontrenderer.drawStringWithShadow(String.valueOf(this.getSpeed(train)), k - 138, this.height - 11, 0x00FF00);
			//Brake
			fontrenderer.drawStringWithShadow(String.valueOf(this.getBrake(train)), k - 178, this.height - 11, 0x00FF00);
			//Time
			fontrenderer.drawStringWithShadow(String.valueOf(this.getWorldTime()), k + 130, this.height - 40, 0x00FF00);
			fontrenderer.drawStringWithShadow(this.getTime(), k + 130, this.height - 30, 0x00FF00);
		} else {
			//speed/3.0F*20F*3.6F*3F
			//2,2が一番上, 文字サイズ10
			fontrenderer.drawStringWithShadow("Speed : " + this.getSpeed(train), 2, this.height - 50, 16777215);
			fontrenderer.drawStringWithShadow("Notch : " + train.getNotch(), 2, this.height - 40, 16777215);
			fontrenderer.drawStringWithShadow("Signal : " + train.getSignal(), 2, this.height - 30, 16777215);
			fontrenderer.drawStringWithShadow("Time : " + this.getWorldTime(), 2, this.height - 20, 16777215);
			fontrenderer.drawStringWithShadow("Time : " + this.getTime(), 2, this.height - 10, 16777215);
		}
	}

	private void renderArtilleryGui(EntityArtillery artillery) {
		FontRenderer fontrenderer = this.mc.fontRenderer;
		fontrenderer.drawStringWithShadow("Yaw : " + artillery.getBarrelYaw(), 2, this.height - 40, 16777215);
		fontrenderer.drawStringWithShadow("Pitch : " + -artillery.getBarrelPitch(), 2, this.height - 30, 16777215);
	}

	private void renderDebugGui() {
		FontRenderer fontrenderer = this.mc.fontRenderer;
		fontrenderer.drawStringWithShadow("Time : " + this.getWorldTime(), 2, this.height - 20, 0xFFFFFF);
		fontrenderer.drawStringWithShadow("Time : " + this.getTime(), 2, this.height - 10, 0xFFFFFF);

		if (RTMUtil.MESSAGELIST.size() > 0) {
			for (int i = 0; i < RTMUtil.MESSAGELIST.size(); ++i) {
				String s = RTMUtil.MESSAGELIST.get(i);
				fontrenderer.drawStringWithShadow(s, this.width - s.length() * 10, this.height - 20 * (i + 1), 0xFFFFFF);
			}
			RTMUtil.MESSAGELIST.clear();
		}
	}

	/**
	 * 速度を時速に変換
	 */
	private int getSpeed(EntityTrainBase train) {
		return (int) ((train.getSpeed() * 72.0F) + 0.5F);
	}

	/**
	 * @return 0~432
	 */
	private int getBrake(EntityTrainBase train) {
		return train.brakeCount * 3;
	}

	private int getWorldTime() {
		return (int) this.mc.thePlayer.worldObj.getWorldTime() % 24000;
	}

	/**
	 * @return 0:00~23:59
	 */
	private String getTime() {
		int t0 = this.getWorldTime();
		int hour = ((t0 / 1000) + 6) % 24;
		int minute = (int) ((float) (t0 % 1000) * 0.06F);
		StringBuilder sb = new StringBuilder(String.valueOf(hour));
		sb.append(":");
		sb.append(minute);
		return sb.toString();
	}

	private void setScale(ScaledResolution par1) {
		this.width = par1.getScaledWidth();
		this.height = par1.getScaledHeight();
	}

	private void drawMeterAndLever(EntityTrainBase train) {
		int halfW = this.width / 2;

		//速度計
		float max = train.getModelSet().getConfig().maxSpeed[train.getModelSet().getConfig().maxSpeed.length - 1];
		float r0 = 270.0F * train.getSpeed() / max;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) (halfW - 136), (float) (this.height - 29), 0.0F);
		GL11.glRotatef(r0, 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		double d = 0.001953125D;
		tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, 96.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(16.0D, -16.0D, this.zLevel, 96.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, -16.0D, this.zLevel, 64.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, 16.0D, this.zLevel, 64.0D * d, 80.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();

		//圧力計
		GL11.glPushMatrix();
		GL11.glTranslatef((float) (halfW - 176), (float) (this.height - 29), 0.0F);
		float rMax = 240.0F;

		//赤,空気残量
		GL11.glPushMatrix();
		//float r1 = 140.0F;
		float r1 = rMax * (float) train.brakeAirCount / (float) EntityTrainBase.MAX_AIR_COUNT;
		GL11.glRotatef(r1, 0.0F, 0.0F, 1.0F);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, 64.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(16.0D, -16.0D, this.zLevel, 64.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, -16.0D, this.zLevel, 32.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, 16.0D, this.zLevel, 32.0D * d, 80.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();

		//黒,ブレーキ圧
		GL11.glPushMatrix();
		float r2 = rMax * (float) this.getBrake(train) / 432.0F;
		GL11.glRotatef(r2, 0.0F, 0.0F, 1.0F);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, 32.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(16.0D, -16.0D, this.zLevel, 32.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, -16.0D, this.zLevel, 0.0D, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, 16.0D, this.zLevel, 0.0D, 80.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glPopMatrix();


		//レバー
		int notch = train.getNotch() * 3;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) (halfW - 104), (float) (this.height + notch) - 19.5F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(4.0D, 1.5D, this.zLevel, 8.0D * d, 83.0D * d);
		tessellator.addVertexWithUV(4.0D, -1.5D, this.zLevel, 8.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(-4.0D, -1.5D, this.zLevel, 0.0D, 80.0D * d);
		tessellator.addVertexWithUV(-4.0D, 1.5D, this.zLevel, 0.0D, 83.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();
	}

	private void drawWatch() {
		int t0 = this.getWorldTime();
		int t1 = ((t0 / 1000) + 6) % 12;
		int t2 = (int) ((float) (t0 % 1000) * 0.06F);
		float hour = 360.0F * (float) t1 / 12.0F;
		float minute = 360.0F * (float) t2 / 60.0F;

		GL11.glPushMatrix();
		GL11.glTranslatef((float) ((this.width / 2) + 112), (float) (this.height - 16), 0.0F);

		GL11.glPushMatrix();
		GL11.glRotatef(hour + 135F, 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		double d = 0.001953125D;
		tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, 128.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(16.0D, -16.0D, this.zLevel, 128.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, -16.0D, this.zLevel, 96.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, 16.0D, this.zLevel, 96.0D * d, 80.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(minute + 135.0F, 0.0F, 0.0F, 1.0F);//minute
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, 160.0D * d, 80.0D * d);
		tessellator.addVertexWithUV(16.0D, -16.0D, this.zLevel, 160.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, -16.0D, this.zLevel, 128.0D * d, 48.0D * d);
		tessellator.addVertexWithUV(-16.0D, 16.0D, this.zLevel, 128.0D * d, 80.0D * d);
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}

	@Override
	public void drawTexturedModalRect(int p_73729_1_, int p_73729_2_, int p_73729_3_, int p_73729_4_, int p_73729_5_, int p_73729_6_) {
		float f = 0.001953125F;
		float f1 = 0.001953125F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(p_73729_1_ + 0, p_73729_2_ + p_73729_6_, this.zLevel, (float) (p_73729_3_ + 0) * f, (float) (p_73729_4_ + p_73729_6_) * f1);
		tessellator.addVertexWithUV(p_73729_1_ + p_73729_5_, p_73729_2_ + p_73729_6_, this.zLevel, (float) (p_73729_3_ + p_73729_5_) * f, (float) (p_73729_4_ + p_73729_6_) * f1);
		tessellator.addVertexWithUV(p_73729_1_ + p_73729_5_, p_73729_2_ + 0, this.zLevel, (float) (p_73729_3_ + p_73729_5_) * f, (float) (p_73729_4_ + 0) * f1);
		tessellator.addVertexWithUV(p_73729_1_ + 0, p_73729_2_ + 0, this.zLevel, (float) (p_73729_3_ + 0) * f, (float) (p_73729_4_ + 0) * f1);
		tessellator.draw();
	}

	protected void renderScope(int w, int h) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		double d0 = (double) (w - h) / 2.0D;
		double d1 = -90.0D;
		this.mc.getTextureManager().bindTexture(tex_scope);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(d0, h, d1, 0.0D, 1.0D);
		tessellator.addVertexWithUV(d0 + (double) h, h, d1, 1.0D, 1.0D);
		tessellator.addVertexWithUV(d0 + (double) h, 0.0D, d1, 1.0D, 0.0D);
		tessellator.addVertexWithUV(d0, 0.0D, d1, 0.0D, 0.0D);
		tessellator.draw();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0);
		tessellator.addVertexWithUV(0.0D, h, d1, 0.0D, 1.0D);
		tessellator.addVertexWithUV(d0, h, d1, 1.0D, 1.0D);
		tessellator.addVertexWithUV(d0, 0.0D, d1, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, d1, 0.0D, 0.0D);

		tessellator.addVertexWithUV(d0 + (double) h, h, d1, 0.0D, 1.0D);
		tessellator.addVertexWithUV(w, h, d1, 1.0D, 1.0D);
		tessellator.addVertexWithUV(w, 0.0D, d1, 1.0D, 0.0D);
		tessellator.addVertexWithUV(d0 + (double) h, 0.0D, d1, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	protected void renderNVD(int w, int h) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		double d0 = (double) (w - h) / 2.0D;
		double d1 = -90.0D;
		Tessellator tessellator = Tessellator.instance;

		//加算合成:緑
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0x309030);
		tessellator.addVertexWithUV(0.0D, h, d1, 0.0D, 1.0D);
		tessellator.addVertexWithUV(w, h, d1, 1.0D, 1.0D);
		tessellator.addVertexWithUV(w, 0.0D, d1, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, d1, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		//枠
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(tex_nvd);
		tessellator.startDrawingQuads();
		float f0 = ((float) h / (float) w) * 0.5F;
		tessellator.addVertexWithUV(0.0D, h, d1, 0.0D, 0.5F + f0);
		tessellator.addVertexWithUV(w, h, d1, 1.0D, 0.5F + f0);
		tessellator.addVertexWithUV(w, 0.0D, d1, 1.0D, 0.5F - f0);
		tessellator.addVertexWithUV(0.0D, 0.0D, d1, 0.0D, 0.5F - f0);
		tessellator.draw();

        /*List list = this.mc.theWorld.getLoadedEntityList();
        for(int i = 0; i < list.size(); ++i)
        {
        	;
        }*/

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}