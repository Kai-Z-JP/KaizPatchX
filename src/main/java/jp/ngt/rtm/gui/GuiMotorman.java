package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiSelect;
import jp.ngt.ngtlib.gui.GuiSlotCustom.SlotElement;
import jp.ngt.rtm.entity.npc.EntityMotorman;
import jp.ngt.rtm.entity.npc.macro.MacroRecorder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiMotorman extends GuiSelect {
    public static GuiMotorman getGui(final EntityMotorman entity) {
        File macroFolder = MacroRecorder.INSTANCE.getMacroFolder();
        List<File> list = Arrays.stream(macroFolder.listFiles()).filter(child -> child.isFile() && child.getName().endsWith(".txt")).collect(Collectors.toList());

        if (list.isEmpty()) {
            return null;
        }

        ItemStack icon = new ItemStack(Items.book);
        SlotElement[] elements = new SlotElement[list.size()];
        IntStream.range(0, elements.length).forEach(i -> {
            File file = list.get(i);
            elements[i] = new SlotElementItem<>(par1 -> entity.setMacro((File) par1), file, file.getName(), icon);
        });
        return new GuiMotorman(entity, elements);
    }

    private GuiMotorman(EntityMotorman entity, SlotElement[] par2) {
        super(null, par2);
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

    @Override
    protected void closeScreen() {
        this.mc.displayGuiScreen(null);
    }
}