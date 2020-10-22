package jp.ngt.rtm.gui.vendor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.gui.GuiButtonColored;
import jp.ngt.rtm.gui.GuiTicketVendor;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class VendorScreenSelectTicket extends VendorScreen {
	public VendorScreenSelectTicket(GuiTicketVendor par1) {
		super(par1);
	}

	@Override
	public void init(int guiLeft, int guiTop) {
		this.vendor.addButton(new GuiButtonColored(10, guiLeft + 24, guiTop + 20, 100, 50, "Ticket", 0x00FFFF, 0x000000));
		this.vendor.addButton(new GuiButtonColored(11, guiLeft + 132, guiTop + 20, 100, 50, "Ticket book", 0x50FF30, 0x000000));
	}

	@Override
	public void onClickButton(GuiButton button) {
		if (button.id == 10) {
			this.vendor.setVendorScreen(new VendorScreenSelectPrice(this.vendor, "Ticket"));
		} else if (button.id == 10) {
			this.vendor.setVendorScreen(new VendorScreenSelectPrice(this.vendor, "TicketBook"));
		}
	}
}