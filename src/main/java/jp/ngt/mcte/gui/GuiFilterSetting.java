package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.filter.CfgParameter;
import jp.ngt.mcte.editor.filter.Config;
import jp.ngt.mcte.editor.filter.EditFilterBase;
import jp.ngt.mcte.editor.filter.FilterManager;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiTextFieldCustom;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiFilterSetting extends GuiScreenCustom {
    protected static final int FIRST_BUTTON_ID = 200;
    protected static final int BUTTON_GAP_Y = 4;
    protected static final int FILTER_BUTTON_W = 100;
    protected static final int SETTING_BUTTON_W = 20;
    protected static final int SEPARATION_POS = 10 + FILTER_BUTTON_W + SETTING_BUTTON_W + 10;

    private final GuiEditor parentGui;
    private final List<EditFilterBase> filters = new ArrayList<>();

    private final List<GuiButton> filterButtons;
    private final List<GuiButton> filterSettingButtons;
    private int currentScrollL;
    private final List<GuiTextField> guiElementNames;
    private final List<IGuiElement> guiElements;
    private int currentScrollR;
    private int selectedFilterId;

    public GuiFilterSetting(GuiEditor gui, String selectedFilterName) {
        this.parentGui = gui;
        this.filters.addAll(FilterManager.INSTANCE.getFilters());

        this.filterButtons = new ArrayList<>();
        this.filterSettingButtons = new ArrayList<>();
        this.guiElementNames = new ArrayList<>();
        this.guiElements = new ArrayList<>();

        selectedFilterId = IntStream.range(0, this.filters.size()).filter(i -> this.filters.get(i).getFilterName().equals(selectedFilterName)).findFirst().orElse(this.selectedFilterId);
    }

    @Override
    public void initGui() {
        this.initGuiElements(this.selectedFilterId);

        //this.buttonList.add(new GuiButton(100, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("_", new Object[0])));
        //this.buttonList.add(new GuiButton(101, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
    }

    private void initGuiElements(int index) {
        this.save();

        this.buttonList.clear();
        this.textFields.clear();

        this.initFiltersButton();
        this.selectSetting(index);
        this.updateElementsPos();
    }

    private void initFiltersButton() {
        this.filterButtons.clear();
        this.filterSettingButtons.clear();

        int buttonId = FIRST_BUTTON_ID - 1;
        int halfW = this.width / 2;
        for (EditFilterBase filter : this.filters) {
            this.filterButtons.add(new GuiButton(++buttonId, 10, 0, FILTER_BUTTON_W, 20, filter.getFilterName()));
            this.filterSettingButtons.add(new GuiButton(++buttonId, 10 + FILTER_BUTTON_W, 0, SETTING_BUTTON_W, 20, "⚙"));
        }

        this.buttonList.addAll(this.filterButtons);
        this.buttonList.addAll(this.filterSettingButtons);
    }

    private void selectSetting(int index) {
        this.selectedFilterId = index;
        this.currentScrollR = 0;

        //選択した設定ボタンのみ無効化
        this.filterSettingButtons.forEach(button1 -> button1.enabled = true);
        this.filterSettingButtons.get(index).enabled = false;

        this.guiElementNames.clear();
        this.guiElements.clear();

        EditFilterBase filter = this.filters.get(index);
        Config cfg = filter.getCfg();
        Map<String, CfgParameter> map = cfg.parameters;
        int elementId = 500;
        int x = SEPARATION_POS + 10;
        int x2 = x + 60;
        int y = 0;
        int w = 100;
        int h = 20;
        for (Entry<String, CfgParameter> entry : map.entrySet()) {
            GuiTextFieldCustom field = this.setTextField(x, y, 60, h, entry.getKey());
            field.setDisplayMode(true);
            this.guiElementNames.add(field);
            IGuiElement element;
            switch (entry.getValue().getType()) {
                case BOOLEAN:
                    element = new GuiElementBoolean(elementId, x2, y, w, h, cfg, entry.getKey());
                    break;
                case FLOAT:
                    element = new GuiElementFloat(x2, y, w, h, cfg, entry.getKey());
                    break;
                case INTEGER:
                    element = new GuiElementInt(x2, y, w, h, cfg, entry.getKey());
                    break;
                case STRING:
                    element = new GuiElementString(x2, y, w, h, cfg, entry.getKey());
                    break;
                case STRING_LIST:
                    element = new GuiElementStringList(elementId, x2, y, w, h, cfg, entry.getKey());
                    break;
                default:
                    continue;
            }
            this.guiElements.add(element);
            element.init(this);
            ++elementId;
        }
    }

    protected void save() {
        //フィールド内の値更新
        this.getTextFields().forEach(field -> field.setFocused(false));

        this.filters.forEach(EditFilterBase::save);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 101) {
            this.closeGui();
        } else if (button.id >= FIRST_BUTTON_ID && button.id < FIRST_BUTTON_ID + this.filterButtons.size() * 2) {
            int index = button.id - FIRST_BUTTON_ID;

            if (index % 2 == 0)//フィルタ選択
            {
                this.parentGui.setFilterName(this.filterButtons.get(index / 2).displayString);
                this.closeGui();
            } else//設定メニュー
            {
                this.initGuiElements(index / 2);
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (par2 == Keyboard.KEY_ESCAPE) {
            this.closeGui();
            return;
        }

        super.keyTyped(par1, par2);
    }

    protected void closeGui() {
        this.save();
        this.mc.displayGuiScreen(this.parentGui);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(0x404040);

        tessellator.addVertex(SEPARATION_POS, this.height, this.zLevel);
        tessellator.addVertex(this.width, this.height, this.zLevel);
        tessellator.addVertex(this.width, 0.0D, this.zLevel);
        tessellator.addVertex(SEPARATION_POS, 0.0D, this.zLevel);

        int y = (this.selectedFilterId - this.currentScrollL) * (20 + BUTTON_GAP_Y) + BUTTON_GAP_Y;
        tessellator.addVertex(0.0D, y + 20 + BUTTON_GAP_Y, this.zLevel);
        tessellator.addVertex(SEPARATION_POS, y + 20 + BUTTON_GAP_Y, this.zLevel);
        tessellator.addVertex(SEPARATION_POS, y - (BUTTON_GAP_Y / 2), this.zLevel);
        tessellator.addVertex(0.0D, y - (BUTTON_GAP_Y / 2), this.zLevel);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        super.drawScreen(par1, par2, par3);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scroll = (scroll > 0) ? 1 : -1;
            if (x < (this.width / 2)) {
                this.currentScrollL = this.scroll(this.currentScrollL - scroll, this.filterButtons.size());
            } else {
                this.currentScrollR = this.scroll(this.currentScrollR - scroll, this.guiElements.size());
            }
            this.updateElementsPos();
        }
    }

    private int scroll(int par1, int max) {
        int scroll = par1;

        if (scroll < 0) {
            scroll = 0;
        } else if (scroll >= max) {
            scroll = max - 1;
        }

        return scroll;
    }

    private void updateElementsPos() {
        IntStream.range(0, this.filterButtons.size()).forEach(i -> {
            int yPos = (i - this.currentScrollL) * (20 + BUTTON_GAP_Y) + BUTTON_GAP_Y;
            this.filterButtons.get(i).yPosition = yPos;
            this.filterSettingButtons.get(i).yPosition = yPos;
        });

        IntStream.range(0, this.guiElements.size()).forEach(i -> {
            int yPos = (i - this.currentScrollR) * (20 + BUTTON_GAP_Y) + BUTTON_GAP_Y;
            GuiTextField field = this.guiElementNames.get(i);
            field.yPosition = yPos;
            IGuiElement element = this.guiElements.get(i);
            element.setYPos(yPos);
        });
    }
}