package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityStation;
import jp.ngt.rtm.network.PacketStationData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiStation extends GuiScreenCustom {
	private final TileEntityStation tileEntity;
	private GuiTextField stationName;
	private GuiTextField[] stationSize;

	public GuiStation(TileEntityStation par1) {
		this.tileEntity = par1;
	}

	@Override
	public void initGui() {
		super.initGui();

		int hw = this.width / 2;

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, hw - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
		this.buttonList.add(new GuiButton(1, hw + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

		this.stationName = this.setTextField(hw - 40, 60, 80, 20, this.tileEntity.getName());
		this.stationSize = new GuiTextField[3];
		this.stationSize[0] = this.setTextField(hw - 40, 84, 60, 20, String.valueOf(this.tileEntity.width));
		this.stationSize[1] = this.setTextField(hw - 40, 108, 60, 20, String.valueOf(this.tileEntity.height));
		this.stationSize[2] = this.setTextField(hw - 40, 132, 60, 20, String.valueOf(this.tileEntity.depth));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(null);
			this.sendPacket();
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(null);
		}

		super.actionPerformed(button);
	}

	private void sendPacket() {
		this.tileEntity.setName(this.stationName.getText());
		try {
			this.tileEntity.width = Integer.valueOf(this.stationSize[0].getText());
			this.tileEntity.height = Integer.valueOf(this.stationSize[1].getText());
			this.tileEntity.depth = Integer.valueOf(this.stationSize[2].getText());
		} catch (NumberFormatException e) {
		}
		RTMCore.NETWORK_WRAPPER.sendToServer(new PacketStationData(this.tileEntity));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);

		int w2 = (this.width / 2) - 80;
		this.drawString(this.fontRendererObj, "Name", w2, 66, 0xFFFFFF);
		this.drawString(this.fontRendererObj, "Width", w2, 90, 0xFFFFFF);
		this.drawString(this.fontRendererObj, "Height", w2, 114, 0xFFFFFF);
		this.drawString(this.fontRendererObj, "Depth", w2, 138, 0xFFFFFF);
	}
}