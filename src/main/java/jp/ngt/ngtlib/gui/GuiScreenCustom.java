package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class GuiScreenCustom extends GuiScreen {
	protected int xSize, ySize;
	protected List<GuiTextField> textFields = new ArrayList<>();
	protected GuiTextField currentTextField;
	protected List<GuiSlotCustom> slotList = new ArrayList<>();

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.textFields.clear();
	}

	public List getButtonList() {
		return this.buttonList;
	}

	public List<GuiTextField> getTextFields() {
		return this.textFields;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected GuiTextFieldCustom setTextField(int xPos, int yPos, int w, int h, String text) {
		GuiTextFieldCustom field = new GuiTextFieldCustom(this.fontRendererObj, xPos, yPos, w, h);
		field.setMaxStringLength(32767);
		field.setFocused(false);
		field.setText(text);
		this.textFields.add(field);
		return field;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		this.slotList.forEach(slot -> slot.actionPerformed(button));
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);

		for (GuiTextField field : this.textFields) {
			field.mouseClicked(par1, par2, par3);
			if (field.isFocused()) {
				this.currentTextField = field;
				this.onTextFieldClicked(field);
				break;
			}
		}
	}

	protected void onTextFieldClicked(GuiTextField field) {
	}

	/**
	 * スロット内の要素がクリックされた時、最初に呼ばれる
	 */
	protected void onElementClicked(int par1, boolean par2) {
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE)//ESC, テキスト入力中でも有効に
		{
			this.mc.displayGuiScreen(null);
			this.mc.setIngameFocus();
			return;
		}

		if (this.currentTextField != null) {
			this.currentTextField.textboxKeyTyped(par1, par2);
		} else {
			super.keyTyped(par1, par2);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawGuiContainerBackgroundLayer(par3, par1, par2);

		this.slotList.forEach(slot -> slot.drawScreen(par1, par2, par3));

		this.textFields.forEach(GuiTextField::drawTextBox);

		this.drawGuiContainerForegroundLayer(par1, par2);

		super.drawScreen(par1, par2, par3);
	}

	protected void drawGuiContainerBackgroundLayer(float par3, int par1, int par2) {
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
	}

	@Override
	public void updateScreen() {
		if (this.currentTextField != null) {
			this.currentTextField.updateCursorCounter();
		}
	}

	public float getZLevel() {
		return this.zLevel;
	}
}