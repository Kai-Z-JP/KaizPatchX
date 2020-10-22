package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.electric.SignalConverterType;
import jp.ngt.rtm.electric.TileEntitySignalConverter;
import jp.ngt.rtm.electric.TileEntitySignalConverter.ComparatorType;
import jp.ngt.rtm.network.PacketSignalConverter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiSignalConverter extends GuiScreenCustom {
	private final TileEntitySignalConverter tileEntity;
	private final SignalConverterType scType;
	private GuiButton button;
	private GuiTextField[] signalValues;
	private int comparatorIndex = 0;

	public GuiSignalConverter(TileEntitySignalConverter par1) {
		this.tileEntity = par1;
		this.scType = SignalConverterType.getType(par1.getBlockMetadata());
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));

		int i0 = 0;
		if (this.scType == SignalConverterType.RSOut) {
			ComparatorType type = this.tileEntity.getComparator();
			this.comparatorIndex = type.id;
			this.button = new GuiButton(100, this.width / 2 - 36, 60, 30, 20, type.operator);
			this.buttonList.add(this.button);
			i0 = 16;
		}

		int[] ia = this.tileEntity.getSignalLevel();
		this.textFields.clear();
		this.signalValues = new GuiTextField[this.scType == SignalConverterType.RSOut ? 1 : 2];
		this.signalValues[0] = this.setTextField(this.width / 2 - 20 + i0, 60, 40, 20, String.valueOf(ia[0]));
		if (this.scType == SignalConverterType.RSIn || this.scType == SignalConverterType.Wireless) {
			this.signalValues[1] = this.setTextField(this.width / 2 - 20 + i0, 100, 40, 20, String.valueOf(ia[1]));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(null);
			this.sendPacket();
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(null);
		} else if (button.id == 100) {
			this.comparatorIndex = (this.comparatorIndex + 1) % ComparatorType.values().length;
			this.button.displayString = ComparatorType.getType(this.comparatorIndex).operator;
		}

		super.actionPerformed(button);
	}

	private void sendPacket() {
		int[] ia = this.formatSignalLevel();
		if (!(this.scType == SignalConverterType.RSIn || this.scType == SignalConverterType.Wireless)) {
			ia[1] = 0;
		}
		RTMCore.NETWORK_WRAPPER.sendToServer(new PacketSignalConverter(this.tileEntity, this.comparatorIndex, ia[0], ia[1]));
	}

	/**
	 * 配列長=2
	 */
	private int[] formatSignalLevel() {
		int[] ia = new int[2];
		for (int i = 0; i < this.signalValues.length; ++i) {
			int max = Short.MAX_VALUE;
			if (this.scType == SignalConverterType.Wireless) {
				if (i == 1)//チャンク範囲
				{
					max = 25;
				}
			} else {
				max = 127;
			}
			int i0 = NGTMath.getIntFromString(this.signalValues[i].getText(), 0, max, 0);
			this.signalValues[i].setText(String.valueOf(i0));
			ia[i] = i0;
		}
		return ia;
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode())//1:Esc
		{
			this.mc.thePlayer.closeScreen();
		}

		if (this.currentTextField != null) {
			if ((par2 >= 2 && par2 <= 11) || (par2 >= 200 && par2 <= 205) || par2 == 12 || par2 == 14 || par2 == 211)//14:Back, 211:Del
			{
				this.currentTextField.textboxKeyTyped(par1, par2);
			}
		}

		if (par2 == 28) {
			this.formatSignalLevel();
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);

		if (this.scType == SignalConverterType.RSIn) {
			this.drawCenteredString(this.fontRendererObj, "Output signal level", this.width / 2, 30, 0xffffff);
			this.drawCenteredString(this.fontRendererObj, "RS_ON", this.width / 2, 45, 0xffffff);
			this.drawCenteredString(this.fontRendererObj, "RS_OFF", this.width / 2, 85, 0xffffff);
		} else if (this.scType == SignalConverterType.RSOut) {
			this.drawCenteredString(this.fontRendererObj, "Input signal level", this.width / 2, 40, 0xffffff);
		} else if (this.scType == SignalConverterType.Wireless) {
			this.drawCenteredString(this.fontRendererObj, "Channel", this.width / 2, 45, 0xffffff);
			this.drawCenteredString(this.fontRendererObj, "Chunk Load Range", this.width / 2, 85, 0xffffff);
		}
	}
}