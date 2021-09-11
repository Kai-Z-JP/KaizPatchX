package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiButtonCustom extends GuiButton {
    private final GuiScreen screen;
    private final List<String> tips = new ArrayList<>();

    public GuiButtonCustom(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, GuiScreen pScr) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.screen = pScr;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);

        if (this.field_146123_n && !this.tips.isEmpty()) {
            GuiScreenCustom.drawHoveringTextS(this.tips, mouseX, mouseY, this.screen);
        }
    }

    public GuiButtonCustom addTips(String par1) {
        if (par1 != null) {
            this.tips.add(par1);
        }
        return this;
    }
}