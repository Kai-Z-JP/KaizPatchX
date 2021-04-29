package jp.ngt.rtm.gui.vendor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.gui.GuiButtonColored;
import jp.ngt.rtm.gui.GuiTicketVendor;
import net.minecraft.client.gui.GuiButton;

/**
 * 切符の値段選択
 */
@SideOnly(Side.CLIENT)
public class VendorScreenSelectPrice extends VendorScreen {
    private final String options;

    public VendorScreenSelectPrice(GuiTicketVendor par1, String options) {
        super(par1);
        this.options = options;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        int sizeX = 40;
        int sizeY = 20;

        for (int i = 0; i < 4; ++i)//Y
        {
            for (int j = 0; j < 5; ++j)//X
            {
                int id = i * 5 + j;
                int x = guiLeft + 10 + j * (sizeX + 5);
                int y = guiTop + 10 + i * (sizeY + 5);
                String s = String.valueOf(160 + id * 60);
                this.vendor.addButton(new GuiButtonColored(id, x, y, sizeX, sizeY, s, 0x00FFFF, 0x000000));
            }
        }
    }

    @Override
    public void onClickButton(GuiButton button) {
        if (button.id >= 0 && button.id < 20) {
        }
    }
}