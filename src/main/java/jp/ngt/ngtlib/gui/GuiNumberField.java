package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

import java.util.regex.Pattern;

@SideOnly(Side.CLIENT)
public class GuiNumberField extends GuiTextFieldCustom {

    private final boolean allowFloat;

    public GuiNumberField(FontRenderer par1, int x, int y, int w, int h, GuiScreen pScr, boolean allowFloat) {
        super(par1, x, y, w, h, pScr);
        this.allowFloat = allowFloat;
    }

    private static final Pattern intPattern = Pattern.compile("^[+-]?\\d*$");
    private static final Pattern floatPattern = Pattern.compile("^[+-]?\\d*\\.?\\d*$");

    @Override
    public void setText(String p_146180_1_) {
        if ((allowFloat ? floatPattern : intPattern).matcher(p_146180_1_).matches()) {
            super.setText(p_146180_1_);
        }
    }

    @Override
    public void writeText(String p_146191_1_) {
        String s1 = "";
        String s2 = ChatAllowedCharacters.filerAllowedCharacters(p_146191_1_);
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        int k = this.maxStringLength - this.text.length() - (i - this.selectionEnd);
        boolean flag = false;

        if (this.text.length() > 0) {
            s1 = s1 + this.text.substring(0, i);
        }

        int l;

        if (k < s2.length()) {
            s1 = s1 + s2.substring(0, k);
            l = k;
        } else {
            s1 = s1 + s2;
            l = s2.length();
        }

        if (this.text.length() > 0 && j < this.text.length()) {
            s1 = s1 + this.text.substring(j);
        }


        if ((allowFloat ? floatPattern : intPattern).matcher(s1).matches()) {
            this.text = s1;
            this.moveCursorBy(i - this.selectionEnd + l);
        }
    }
}