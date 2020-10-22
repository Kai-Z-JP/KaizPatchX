package jp.ngt.rtm.gui.vendor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.gui.GuiTicketVendor;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public abstract class VendorScreen {
	protected GuiTicketVendor vendor;

	public VendorScreen(GuiTicketVendor par1) {
		this.vendor = par1;
	}

	public abstract void init(int guiLeft, int guiTop);

	public abstract void onClickButton(GuiButton button);

	public void drawScreen() {
	}
}