package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import jp.ngt.rtm.network.PacketMovingMachine;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiMovingMachine extends GuiScreenCustom {
	private final TileEntityMovingMachine tileEntity;
	private GuiTextField[] range;
	private GuiTextField speed;
	private GuiButton buttonV;

	private boolean guideVisibility;

	public GuiMovingMachine(TileEntityMovingMachine par1) {
		this.tileEntity = par1;
		this.guideVisibility = par1.guideVisibility;
	}

	@Override
	public void initGui() {
		super.initGui();

		int hw = this.width / 2;

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, hw - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
		this.buttonList.add(new GuiButton(1, hw + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

		int h = 44;
		this.buttonV = new GuiButton(100, hw - 120, h, 100, 20, "GuideVisibility : " + this.guideVisibility);
		this.buttonList.add(this.buttonV);

		this.range = new GuiTextField[3];
		this.range[0] = this.setTextField(hw + 40, h, 60, 20, String.valueOf(this.tileEntity.width));
		h += 24;
		this.range[1] = this.setTextField(hw + 40, h, 60, 20, String.valueOf(this.tileEntity.height));
		h += 24;
		this.range[2] = this.setTextField(hw + 40, h, 60, 20, String.valueOf(this.tileEntity.depth));
		h += 24;
		this.speed = this.setTextField(hw + 40, h, 60, 20, String.valueOf(this.tileEntity.speed));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(null);
			this.sendPacket();
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(null);
		} else if (button.id == 100) {
			this.guideVisibility ^= true;
			this.buttonV.displayString = "GuideVisibility : " + this.guideVisibility;
		}

		super.actionPerformed(button);
	}

	private void sendPacket() {
		int w = NGTMath.getIntFromString(this.range[0].getText(), 0, Integer.MAX_VALUE, 0);
		int h = NGTMath.getIntFromString(this.range[1].getText(), 0, Integer.MAX_VALUE, 0);
		int d = NGTMath.getIntFromString(this.range[2].getText(), 0, Integer.MAX_VALUE, 0);
		float s = NGTMath.getFloatFromString(this.speed.getText(), 0, Integer.MAX_VALUE, 0);
		RTMCore.NETWORK_WRAPPER.sendToServer(
				new PacketMovingMachine(this.tileEntity, w, h, d, s, this.guideVisibility));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);

		int w2 = (this.width / 2) + 10;
		int h = 50;
		this.drawString(this.fontRendererObj, "Width", w2, h, 0xFFFFFF);
		h += 24;
		this.drawString(this.fontRendererObj, "Height", w2, h, 0xFFFFFF);
		h += 24;
		this.drawString(this.fontRendererObj, "Depth", w2, h, 0xFFFFFF);
		h += 24;
		this.drawString(this.fontRendererObj, "Speed", w2, h, 0xFFFFFF);
	}
}