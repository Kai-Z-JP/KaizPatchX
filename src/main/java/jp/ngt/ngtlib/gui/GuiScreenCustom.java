package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class GuiScreenCustom extends GuiScreen {
    protected int xSize, ySize;
    protected List<GuiTextFieldCustom> textFields = new ArrayList<>();
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

    public List<GuiTextFieldCustom> getTextFields() {
        return this.textFields;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    protected GuiTextFieldCustom setTextField(int xPos, int yPos, int w, int h, String text) {
        GuiTextFieldCustom field = new GuiTextFieldCustom(this.fontRendererObj, xPos, yPos, w, h, this);
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

        int mouseX = Mouse.getEventX() * this.width / NGTUtilClient.getMinecraft().displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / NGTUtilClient.getMinecraft().displayHeight - 1;
        this.textFields.forEach(field -> field.drawTextBox(mouseX, mouseY));

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

    protected float getFieldValue(GuiTextFieldCustom field, float defaultVal) {
        try {
            return Float.parseFloat(field.getText());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    @Override
    public void drawHoveringText(List textLines, int x, int y, FontRenderer font) {
        super.drawHoveringText(textLines, x, y, font);
    }

    public static void drawHoveringTextS(List<String> textLines, int x, int y, GuiScreen screen) {
        FontRenderer fontRenderer = NGTUtilClient.getMinecraft().fontRenderer;
        if (screen instanceof GuiScreenCustom) {
            ((GuiScreenCustom) screen).drawHoveringText(textLines, x, y, fontRenderer);
        } else if (screen instanceof GuiContainerCustom) {
            ((GuiContainerCustom) screen).drawHoveringText(textLines, x, y, fontRenderer);
        }

        //以降で描画するボタンの明るさを変えないように
        itemRender.zLevel = 0.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
}