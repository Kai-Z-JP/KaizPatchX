package jp.ngt.rtm.gui;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class RTMConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return RTMConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class RTMConfigGui extends GuiConfig {
        public RTMConfigGui(GuiScreen parentScreen) {
            super(parentScreen,
                    RTMConfig.cfg.getCategoryNames().stream()
                            .map(categoryName -> new ConfigElement<>(RTMConfig.cfg.getCategory(categoryName)).getChildElements())
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()),
                    RTMCore.MODID, false, false, RTMCore.NAME);
        }
    }
}
