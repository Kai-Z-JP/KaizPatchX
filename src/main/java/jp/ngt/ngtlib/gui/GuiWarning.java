package jp.ngt.ngtlib.gui;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTCertificate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiWarning extends GuiScreen {
	//private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/gui/cab.png");
	private int counter;
	private boolean field_01;

	public GuiWarning(Minecraft par1) {
		super();
		this.mc = par1;
	}

	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent event) {
		if (event.isCancelable() || event.type != ElementType.EXPERIENCE) {
			return;
		}

		if (!NGTCertificate.canUse()) {
			if (this.counter < 10) {
				++this.counter;
			} else {
				this.counter = 0;
				this.field_01 = !this.field_01;
			}

			this.setScale(event.resolution);
			FontRenderer fontrenderer = this.mc.fontRenderer;
			GL11.glPushMatrix();
			GL11.glScalef(2.5F, 2.5F, 2.5F);
			int i0 = this.field_01 ? 0xFF0000 : 0xFFFF00;
			fontrenderer.drawStringWithShadow(I18n.format("gui.warning", new Object[]{}), 2, 5, i0);
			GL11.glPopMatrix();
		}
	}

	private void setScale(ScaledResolution par1) {
		this.width = par1.getScaledWidth();
		this.height = par1.getScaledHeight();
	}

	@Override
	public void drawTexturedModalRect(int p_73729_1_, int p_73729_2_, int p_73729_3_, int p_73729_4_, int p_73729_5_, int p_73729_6_) {
		float f = 0.001953125F;
		float f1 = 0.001953125F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) (p_73729_1_ + 0), (double) (p_73729_2_ + p_73729_6_), (double) this.zLevel, (double) ((float) (p_73729_3_ + 0) * f), (double) ((float) (p_73729_4_ + p_73729_6_) * f1));
		tessellator.addVertexWithUV((double) (p_73729_1_ + p_73729_5_), (double) (p_73729_2_ + p_73729_6_), (double) this.zLevel, (double) ((float) (p_73729_3_ + p_73729_5_) * f), (double) ((float) (p_73729_4_ + p_73729_6_) * f1));
		tessellator.addVertexWithUV((double) (p_73729_1_ + p_73729_5_), (double) (p_73729_2_ + 0), (double) this.zLevel, (double) ((float) (p_73729_3_ + p_73729_5_) * f), (double) ((float) (p_73729_4_ + 0) * f1));
		tessellator.addVertexWithUV((double) (p_73729_1_ + 0), (double) (p_73729_2_ + 0), (double) this.zLevel, (double) ((float) (p_73729_3_ + 0) * f), (double) ((float) (p_73729_4_ + 0) * f1));
		tessellator.draw();
	}
}