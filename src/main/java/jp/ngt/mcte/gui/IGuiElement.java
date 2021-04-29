package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;

@SideOnly(Side.CLIENT)
public interface IGuiElement {
    /**
     * buttonListへの登録など行う
     */
    void init(GuiScreenCustom gui);

    void setYPos(int y);
}