package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.filter.Config;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.gui.GuiTextField;

@SideOnly(Side.CLIENT)
public class GuiElementInt extends GuiTextField implements IGuiElement {
	private final Config cfg;
	private final String paramName;

	public GuiElementInt(int x, int y, int w, int h, Config cfg, String key) {
		super(NGTUtilClient.getMinecraft().fontRenderer, x, y, w, h);
		this.cfg = cfg;
		this.paramName = key;
		this.setText(String.valueOf(cfg.getInt(key)));
	}

	@Override
	public void setFocused(boolean par1) {
		super.setFocused(par1);
		if (!par1) {
			this.updateValue();
		}
	}

	private void updateValue() {
		try {
			int value = Integer.valueOf(this.getText());
			this.cfg.setInt(this.paramName, value);
			this.setText(String.valueOf(this.cfg.getInt(this.paramName)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
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