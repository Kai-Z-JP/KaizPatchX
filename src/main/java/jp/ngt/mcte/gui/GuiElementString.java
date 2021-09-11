package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.filter.Config;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiTextFieldCustom;
import jp.ngt.ngtlib.util.NGTUtilClient;

@SideOnly(Side.CLIENT)
public class GuiElementString extends GuiTextFieldCustom implements IGuiElement {
    private final Config cfg;
    private final String paramName;

    public GuiElementString(int x, int y, int w, int h, Config cfg, String key) {
        super(NGTUtilClient.getMinecraft().fontRenderer, x, y, w, h, null);
        this.cfg = cfg;
        this.paramName = key;
        this.setText(cfg.getString(key));
    }

    @Override
    public void setFocused(boolean par1) {
        super.setFocused(par1);
        if (!par1) {
            this.updateValue();
        }
    }

    private void updateValue() {
        String value = this.getText();
        this.cfg.setString(this.paramName, value);
        this.setText(this.cfg.getString(this.paramName));
    }

    @Override
    public void init(GuiScreenCustom gui) {
        gui.getTextFields().add(this);
    }

    @Override
    public void setYPos(int y) {
        this.yPosition = y;
    }
}