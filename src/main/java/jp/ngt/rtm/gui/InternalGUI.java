package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.util.NGTUtilClient;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class InternalGUI {
	private final float startX;

	private final float startY;

	private final float width;

	private final float height;

	private int color;

	private final List<InternalButton> buttons = new ArrayList<>();

	private boolean mouseClicked;

	public InternalGUI(float startX, float startY, float width, float height) {
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
	}

	public InternalGUI setColor(int par1) {
		this.color = par1;
		return this;
	}

	public InternalButton addButton(InternalButton button) {
		this.buttons.add(button);
		return button;
	}

	public void render() {
		GL11.glPushMatrix();
		GL11.glDisable(3553);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		NGTTessellator tessellator = NGTTessellator.instance;
		tessellator.startDrawing(7);
		tessellator.setColorRGBA_I(this.color, 176);
		NGTRenderHelper.addQuadGuiFaceWithSize(this.startX, this.startY, this.width, this.height, 0.0F);
		tessellator.draw();
		GL11.glDisable(3042);
		for (int i = 0; i < 2; i++) {
			boolean pickMode = (i == 0);
			if (pickMode) {
				if (!Mouse.isButtonDown(1) && NGTUtilClient.getMinecraft().inGameHasFocus) {
					this.mouseClicked = false;
				}
				GLHelper.startMousePicking(2.0F);
			}
			int id = 1;
			for (InternalButton button : this.buttons) {
				if (pickMode) {
					GL11.glLoadName(id++);
					button.hovered = false;
				}
				button.render(pickMode);
			}
			if (pickMode) {
				int hits = GLHelper.finishMousePicking();
				if (hits > 0) {
					int pickedId = GLHelper.getPickedObjId(0);
					InternalButton button = this.buttons.get(pickedId - 1);
					button.hovered = true;
					if (!this.mouseClicked && Mouse.isButtonDown(1) && NGTUtilClient.getMinecraft().inGameHasFocus) {
						clickButton(button);
						this.mouseClicked = true;
					}
				}
			}
		}
		GL11.glPopMatrix();
	}

	private void clickButton(InternalButton button) {
		button.getListner().onClick(button);
		(NGTUtilClient.getMinecraft()).thePlayer.playSound("random.click", 1.0F, 1.0F);
	}
}
