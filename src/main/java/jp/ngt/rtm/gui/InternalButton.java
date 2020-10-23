package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.util.ColorUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class InternalButton {
	private final float startX;

	private final float startY;

	private final float width;

	private final float height;

	private int color;

	private String text = "";

	private int textColor = 16777215;

	private float textScale = 0.05F;

	private ButtonListner listner;

	public boolean hovered;

	public InternalButton(float startX, float startY, float width, float height) {
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
	}

	public InternalButton setText(String text, int color, float scale) {
		this.text = text;
		this.textColor = color;
		this.textScale = scale;
		return this;
	}

	public InternalButton setColor(int par1) {
		this.color = par1;
		return this;
	}

	public InternalButton setListner(ButtonListner par1) {
		this.listner = par1;
		return this;
	}

	public ButtonListner getListner() {
		return this.listner;
	}

	public void render(boolean pickMode) {
		GL11.glDisable(3553);
		int color2 = this.hovered ? ColorUtil.multiplicating(this.color, 13684944) : this.color;
		NGTTessellator tessellator = NGTTessellator.instance;
		tessellator.startDrawing(7);
		tessellator.setColorRGBA_I(color2, 255);
		NGTRenderHelper.addQuadGuiFaceWithSize(this.startX, this.startY, this.width, this.height, 0.01F);
		tessellator.draw();
		GL11.glEnable(3553);
		if (!pickMode) {
			GL11.glPushMatrix();
			GL11.glScalef(this.textScale, -this.textScale, this.textScale);
			GL11.glTranslatef(1.0F, 0.0F, 0.25F);
			FontRenderer fontRenderer = (NGTUtilClient.getMinecraft()).fontRenderer;
			float x = this.startX / this.textScale;
			float y = -this.startY / this.textScale - 10.0F;
			fontRenderer.drawString(this.text, MathHelper.floor_float(x), MathHelper.floor_float(y), this.textColor, false);
			GL11.glPopMatrix();
		}
	}

	public interface ButtonListner {
		void onClick(InternalButton param1InternalButton);
	}
}
