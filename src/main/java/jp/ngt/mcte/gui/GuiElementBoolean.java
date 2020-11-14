package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.filter.Config;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class GuiElementBoolean extends GuiButton implements IGuiElement {
	private final Config cfg;
	private final String paramName;

	public GuiElementBoolean(int id, int x, int y, int w, int h, Config cfg, String key) {
		super(id, x, y, w, h, "");
		this.cfg = cfg;
		this.paramName = key;
		this.displayString = String.valueOf(cfg.getBoolean(key));
	}

	@Override
	public boolean mousePressed(Minecraft par1, int par2, int par3) {
		if (super.mousePressed(par1, par2, par3)) {
			this.updateValue();
			return true;
		}
		return false;
	}

	private void updateValue() {
        boolean value = !Boolean.parseBoolean(this.displayString);
        this.cfg.setBoolean(this.paramName, value);
        this.displayString = String.valueOf(this.cfg.getBoolean(this.paramName));
    }

	@Override
	public void init(GuiScreenCustom gui) {
		gui.getButtonList().add(this);
	}

	@Override
	public void setYPos(int y) {
		this.yPosition = y;
	}
}