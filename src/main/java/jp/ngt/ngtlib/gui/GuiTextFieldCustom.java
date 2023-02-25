package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiTextFieldCustom extends GuiTextField {
    protected final FontRenderer fontRenderer;
    //public int xPosition, yPosition;
    //public int width, height;
    protected String text = "";
    protected int maxStringLength = 32;
    protected int cursorCounter;
    protected boolean enableBackgroundDrawing = true;
    protected boolean canLoseFocus = true;
    protected boolean isFocused;
    protected boolean isEnabled = true;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    protected int lineScrollOffset;
    protected int cursorPosition;
    protected int selectionEnd;
    protected int enabledColor = 0xE0E0E0;
    protected int disabledColor = 0x707070;
    protected boolean visible = true;

    private final GuiScreen screen;
    private final List<String> tips = new ArrayList<>();
    protected boolean isDisplayMode;

    public GuiTextFieldCustom(FontRenderer par1, int x, int y, int w, int h, GuiScreen pScr) {
        super(par1, x, y, w, h);
        this.fontRenderer = par1;
        this.xPosition = x;
        this.yPosition = y;
        this.width = w;
        this.height = h;

        this.screen = pScr;
    }

    public void setDisplayMode(boolean par1) {
        this.isDisplayMode = par1;
        this.setEnabled(!par1);
    }

    @Override
    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    @Override
    public void setText(String p_146180_1_) {
        if (p_146180_1_.length() > this.maxStringLength) {
            this.text = p_146180_1_.substring(0, this.maxStringLength);
        } else {
            this.text = p_146180_1_;
        }
        this.setCursorPositionEnd();
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getSelectedText() {
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        return this.text.substring(i, j);
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

        this.text = s1;
        this.moveCursorBy(i - this.selectionEnd + l);
    }

    @Override
    public void deleteWords(int p_146177_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }
    }

    @Override
    public void deleteFromCursor(int p_146175_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = p_146175_1_ < 0;
                int j = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
                int k = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
                String s = "";

                if (j >= 0) {
                    s = this.text.substring(0, j);
                }

                if (k < this.text.length()) {
                    s = s + this.text.substring(k);
                }

                this.text = s;

                if (flag) {
                    this.moveCursorBy(p_146175_1_);
                }
            }
        }
    }

    @Override
    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    @Override
    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, this.getCursorPosition(), true);
    }

    @Override
    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int k = p_146197_2_;
        boolean flag1 = p_146197_1_ < 0;
        int l = Math.abs(p_146197_1_);

        for (int i1 = 0; i1 < l; ++i1) {
            if (flag1) {
                while (p_146197_3_ && k > 0 && this.text.charAt(k - 1) == 32) {
                    --k;
                }

                while (k > 0 && this.text.charAt(k - 1) != 32) {
                    --k;
                }
            } else {
                int j1 = this.text.length();
                k = this.text.indexOf(32, k);

                if (k == -1) {
                    k = j1;
                } else {
                    while (p_146197_3_ && k < j1 && this.text.charAt(k) == 32) {
                        ++k;
                    }
                }
            }
        }

        return k;
    }

    @Override
    public void moveCursorBy(int p_146182_1_) {
        this.setCursorPosition(this.selectionEnd + p_146182_1_);
    }

    @Override
    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        int j = this.text.length();

        if (this.cursorPosition < 0) {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > j) {
            this.cursorPosition = j;
        }

        this.setSelectionPos(this.cursorPosition);
    }

    @Override
    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    @Override
    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    @Override
    public boolean textboxKeyTyped(char word, int code) {
        if (!this.isFocused) {
            return false;
        }

        switch (word) {
            case 1:
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
                return true;

            case 3:
                GuiScreen.setClipboardString(this.getSelectedText());
                return true;

            case 22:
                if (this.isEnabled) {
                    this.writeText(GuiScreen.getClipboardString());
                }
                return true;

            case 24:
                GuiScreen.setClipboardString(this.getSelectedText());
                if (this.isEnabled) {
                    this.writeText("");
                }
                return true;

            default:
                switch (code) {
                    case Keyboard.KEY_BACK:
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (this.isEnabled) {
                                this.deleteWords(-1);
                            }
                        } else if (this.isEnabled) {
                            this.deleteFromCursor(-1);
                        }
                        return true;

                    case Keyboard.KEY_HOME:
                        if (GuiScreen.isShiftKeyDown()) {
                            this.setSelectionPos(0);
                        } else {
                            this.setCursorPositionZero();
                        }
                        return true;

                    case Keyboard.KEY_LEFT:
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                            } else {
                                this.setSelectionPos(this.getSelectionEnd() - 1);
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(-1));
                        } else {
                            this.moveCursorBy(-1);
                        }
                        return true;

                    case Keyboard.KEY_RIGHT:
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                            } else {
                                this.setSelectionPos(this.getSelectionEnd() + 1);
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(1));
                        } else {
                            this.moveCursorBy(1);
                        }
                        return true;

                    case Keyboard.KEY_END:
                        if (GuiScreen.isShiftKeyDown()) {
                            this.setSelectionPos(this.text.length());
                        } else {
                            this.setCursorPositionEnd();
                        }
                        return true;

                    case Keyboard.KEY_DELETE:
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (this.isEnabled) {
                                this.deleteWords(1);
                            }
                        } else if (this.isEnabled) {
                            this.deleteFromCursor(1);
                        }
                        return true;

                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(word)) {
                            if (this.isEnabled) {
                                this.writeText(Character.toString(word));
                            }
                            return true;
                        } else {
                            return false;
                        }
                }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        boolean flag = x >= this.xPosition && x < this.xPosition + this.width && y >= this.yPosition && y < this.yPosition + this.height;

        if (this.canLoseFocus) {
            this.setFocused(flag);
        }

        if (this.isFocused && button == 0) {
            int l = x - this.xPosition;

            if (this.enableBackgroundDrawing) {
                l -= 4;
            }

            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(this.fontRenderer.trimStringToWidth(s, l).length() + this.lineScrollOffset);
        }
    }

    public void drawTextBox(int mouseX, int mouseY) {
        this.drawTextBox();

        boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        if (hovered && !this.tips.isEmpty() && this.visible) {
            GuiScreenCustom.drawHoveringTextS(this.tips, mouseX, mouseY, this.screen);
        }
    }

    @Override
    public void drawTextBox() {
        if (this.isDisplayMode) {
            this.drawString(this.fontRenderer, this.text, this.xPosition, this.yPosition + (this.height - 8) / 2, this.enabledColor);
        } else {
            if (this.getVisible()) {
                if (this.getEnableBackgroundDrawing()) {
                    drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, 0xFFA0A0A0);
                    drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0xFF000000);
                }

                int color = this.isEnabled ? this.enabledColor : this.disabledColor;
                int j = this.cursorPosition - this.lineScrollOffset;
                int k = this.selectionEnd - this.lineScrollOffset;
                String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
                boolean flag = j >= 0 && j <= s.length();
                boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
                int x = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
                int y = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
                int xEnd = x;

                if (k > s.length()) {
                    k = s.length();
                }

                if (s.length() > 0) {
                    String s1 = flag ? s.substring(0, j) : s;
                    xEnd = this.fontRenderer.drawStringWithShadow(s1, x, y, color);
                }

                boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
                int x2 = xEnd;

                if (!flag) {
                    x2 = j > 0 ? x + this.width : x;
                } else if (flag2) {
                    x2 = xEnd - 1;
                    --xEnd;
                }

                if (s.length() > 0 && flag && j < s.length()) {
                    this.fontRenderer.drawStringWithShadow(s.substring(j), xEnd, y, color);
                }

                if (flag1) {
                    if (flag2) {
                        Gui.drawRect(x2, y - 1, x2 + 1, y + 1 + this.fontRenderer.FONT_HEIGHT, 0xFFD0D0D0);
                    } else {
                        this.fontRenderer.drawStringWithShadow("_", x2, y, color);
                    }
                }

                if (k != j) {
                    int strW = x + this.fontRenderer.getStringWidth(s.substring(0, k));
                    this.drawCursorVertical(x2, y - 1, strW - 1, y + 1 + this.fontRenderer.FONT_HEIGHT);
                }
            }
        }
    }

    private void drawCursorVertical(int par1, int par2, int par3, int par4) {
        int i1;

        if (par1 < par3) {
            i1 = par1;
            par1 = par3;
            par3 = i1;
        }

        if (par2 < par4) {
            i1 = par2;
            par2 = par4;
            par4 = i1;
        }

        if (par3 > this.xPosition + this.width) {
            par3 = this.xPosition + this.width;
        }

        if (par1 > this.xPosition + this.width) {
            par1 = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        tessellator.startDrawingQuads();
        tessellator.addVertex(par1, par4, 0.0D);
        tessellator.addVertex(par3, par4, 0.0D);
        tessellator.addVertex(par3, par2, 0.0D);
        tessellator.addVertex(par1, par2, 0.0D);
        tessellator.draw();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;

        if (this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }
    }

    @Override
    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    @Override
    public int getCursorPosition() {
        return this.cursorPosition;
    }

    @Override
    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    @Override
    public void setEnableBackgroundDrawing(boolean p_146185_1_) {
        this.enableBackgroundDrawing = p_146185_1_;
    }

    @Override
    public void setTextColor(int p_146193_1_) {
        this.enabledColor = p_146193_1_;
    }

    @Override
    public void setDisabledTextColour(int p_146204_1_) {
        this.disabledColor = p_146204_1_;
    }

    @Override
    public void setFocused(boolean par1) {
        if (!this.isDisplayMode) {
            this.setCursorPositionZero();
            this.setSelectionPos(this.getCursorPosition());
            if (par1 && !this.isFocused) {
                this.cursorCounter = 0;
            }
            this.isFocused = par1;
        }
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public void setEnabled(boolean p_146184_1_) {
        this.isEnabled = p_146184_1_;
    }

    @Override
    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    @Override
    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    @Override
    public void setSelectionPos(int p_146199_1_) {
        int j = this.text.length();

        if (p_146199_1_ > j) {
            p_146199_1_ = j;
        }

        if (p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        this.selectionEnd = p_146199_1_;

        if (this.fontRenderer != null) {
            if (this.lineScrollOffset > j) {
                this.lineScrollOffset = j;
            }

            int k = this.getWidth();
            String s = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), k);
            int l = s.length() + this.lineScrollOffset;

            if (p_146199_1_ == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(this.text, k, true).length();
            }

            if (p_146199_1_ > l) {
                this.lineScrollOffset += p_146199_1_ - l;
            } else if (p_146199_1_ <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
            }

            if (this.lineScrollOffset < 0) {
                this.lineScrollOffset = 0;
            }

            if (this.lineScrollOffset > j) {
                this.lineScrollOffset = j;
            }
        }
    }

    @Override
    public void setCanLoseFocus(boolean p_146205_1_) {
        this.canLoseFocus = p_146205_1_;
    }

    @Override
    public boolean getVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean p_146189_1_) {
        this.visible = p_146189_1_;
    }

    public GuiTextFieldCustom addTips(String par1) {
        this.tips.add(par1);
        return this;
    }
}