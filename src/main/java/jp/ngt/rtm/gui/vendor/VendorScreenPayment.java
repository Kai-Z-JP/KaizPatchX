package jp.ngt.rtm.gui.vendor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.gui.GuiTicketVendor;
import net.minecraft.client.gui.GuiButton;

/**
 * 支払画面
 */
@SideOnly(Side.CLIENT)
public class VendorScreenPayment extends VendorScreen {
    public VendorScreenPayment(GuiTicketVendor par1) {
        super(par1);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
    }

    @Override
    public void onClickButton(GuiButton button) {
    }
}