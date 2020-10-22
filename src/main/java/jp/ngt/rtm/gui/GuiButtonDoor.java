package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButtonDoor extends GuiButton {
	public boolean opened;

	public GuiButtonDoor(int id, int xPos, int yPos, int w, int h) {
		super(id, xPos, yPos, w, h, "");
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(TabTrainControlPanel.TAB_Inventory.getTexture());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
			int k = this.getHoverState(this.field_146123_n);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			int i0 = this.opened ? -10 : -4;
			this.drawTexturedModalRect(this.xPosition + 25, this.yPosition + i0, 242, 80, 14, 100);//スイッチ
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 192, 0, 64, 80);//本体
			int v = this.opened ? 80 : 88;
			this.drawTexturedModalRect(this.xPosition + 44, this.yPosition + 48, 224, v, 8, 8);//ランプ
			this.mouseDragged(mc, x, y);
		}
	}

	@Override
	public void func_146113_a(SoundHandler handler) {
		RTMCore.proxy.playSound(NGTUtilClient.getMinecraft().thePlayer, new ResourceLocation("rtm", "train.lever"), 1.0F, 1.0F);
		//handler.playSound(PositionedSoundRecord.func_147675_a(new ResourceLocation("rtm", "train.lever"), 1.0F, zLevel, zLevel));
	}
}