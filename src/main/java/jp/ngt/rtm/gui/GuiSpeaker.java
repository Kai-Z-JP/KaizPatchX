package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.rtm.electric.TileEntitySpeaker;
import jp.ngt.rtm.sound.RTMSoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiSpeaker extends GuiScreenCustom {
    private GuiTextField searchField;

    private String searchText = "";

    private final List<GuiButton> selectButtons = new ArrayList<>();

    private final List<GuiButton> soundButtons = new ArrayList<>();

    private int currentScrollMain;

    private int currentScrollSub;

    private int chooseSoundId;

    private TileEntitySpeaker tile;

    public GuiSpeaker(TileEntitySpeaker par1) {
        this.tile = par1;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 + 20, this.height - 28, 120, 20, I18n.format("gui.done")));
        this.searchField = this.setTextField(this.width / 2 - 140, this.height - 28, 120, 20, this.searchText);
        int selWidth = 30;
        int sndWidth = 200;
        this.selectButtons.clear();
        for (int i = 0; i < 64; i++) {
            int y = (i - this.currentScrollMain) * 20;
            int x = this.width - selWidth;
            GuiButton button = new GuiButton(100 + i, x, y, selWidth, 20, "select");
            this.buttonList.add(button);
            this.selectButtons.add(button);
            if (i == this.chooseSoundId - 1) {
                button.enabled = false;
            }
        }
        initSoundList();
    }

    private void initSoundList() {
        int selWidth = 30;
        int sndWidth = 200;
        this.currentScrollSub = 0;
        if (!this.soundButtons.isEmpty()) {
            this.buttonList.removeAll(this.soundButtons);
        }
        this.soundButtons.clear();
        if (this.chooseSoundId > 0) {
            int i = 0;
            for (String fileName : RTMSoundHandler.ALL_OGG_FILES.stream().filter(s -> s.contains(this.searchText)).collect(Collectors.toList())) {
                int y = (i - this.currentScrollSub) * 20;
                int x = this.width - selWidth - sndWidth;
                GuiButton button = new GuiButton(500 + i, x, y, sndWidth, 20, fileName);
                this.buttonList.add(button);
                this.soundButtons.add(button);
                i++;
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);
        for (int i = 0; i < this.selectButtons.size(); i++) {
            int posY = (i - this.currentScrollMain) * 20 + 4;
//            String s = String.format("%d : %s", i + 1, SpeakerSounds.getInstance(false).getSound(i + 1));
            String s = String.format("%d : %s", i + 1, this.tile.getSound(i + 1));
            drawString(this.fontRendererObj, s, 20, posY, 16777215);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        } else if (button.id >= 100 && button.id < 164) {
            int id = button.id - 100;
            this.chooseSoundId = id + 1;
            initGui();
        } else if (button.id >= 500 && button.id < 500 + RTMSoundHandler.ALL_OGG_FILES.size()) {
//            SpeakerSounds.getInstance(false).setSound(this.chooseSoundId, button.displayString, true);
            this.tile.setSound(this.chooseSoundId, button.displayString);
            this.chooseSoundId = 0;
//            initGui();
            this.mc.displayGuiScreen(null);
        }
        super.actionPerformed(button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int i0 = Mouse.getEventDWheel();
        if (i0 != 0) {
            i0 = (i0 > 0) ? 1 : -1;
            scroll(i0);
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);
        if (this.currentTextField != null) {
            this.searchText = this.currentTextField.getText();
        }
        initSoundList();
    }

    private void scroll(int par1) {
        if (this.chooseSoundId > 0) {
            this.currentScrollSub = scroll(this.currentScrollSub - par1, this.soundButtons.size());
        } else {
            this.currentScrollMain = scroll(this.currentScrollMain - par1, this.selectButtons.size());
        }
        resetButtonPos();
    }

    private int scroll(int scroll, int max) {
        if (scroll < 0)
            return 0;
        if (scroll >= max)
            return max - 1;
        return scroll;
    }

    private void resetButtonPos() {
        if (this.chooseSoundId > 0) {
            for (int i = 0; i < this.soundButtons.size(); i++) {
                this.soundButtons.get(i).yPosition = (i - this.currentScrollSub) * 20;
            }
        } else {
            for (int i = 0; i < this.selectButtons.size(); i++) {
                this.selectButtons.get(i).yPosition = (i - this.currentScrollMain) * 20;
            }
        }
    }
}
