package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.filter.Config;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class GuiElementStringList extends GuiButton implements IGuiElement {
	private final Config cfg;
	private final String paramName;
	private final String[] strlist;
	private int index;

	public GuiElementStringList(int id, int x, int y, int w, int h, Config cfg, String key) {
		super(id, x, y, w, h, "");
		this.cfg = cfg;
		this.paramName = key;
		this.strlist = cfg.getStringList(key);
		this.initValue();
	}

	private void initValue() {
		String s = this.cfg.getString(this.paramName);
		for (this.index = 0; this.index < this.strlist.length; ++this.index) {
			if (s.equals(this.strlist[this.index])) {
				break;
			}
		}
		this.updateValue();
	}

	@Override
	public boolean mousePressed(Minecraft par1, int par2, int par3) {
		if (super.mousePressed(par1, par2, par3)) {
			this.onClickButton();
			return true;
		}
		return false;
	}

	private void onClickButton() {
		++this.index;
		if (this.index >= this.strlist.length) {
			this.index = 0;
		}
		this.updateValue();
	}

	private void updateValue() {
		String s = this.strlist[this.index];
		this.cfg.setString(this.paramName, s);
		this.displayString = String.format("%s (%d / %d)", s, this.index + 1, this.strlist.length);
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