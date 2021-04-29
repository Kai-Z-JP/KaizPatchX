package jp.ngt.rtm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.stream.IntStream;

public class RTMParticles {
    private static RTMParticles instance;
    private static final String[] iconNames = {"rtm:meltedMetal"};
    private IIcon[] icons;

    public static RTMParticles getInstance() {
        if (instance == null) {
            instance = new RTMParticles();
        }
        return instance;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void handleTextureRemap(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) {
            getInstance().registerIIcons(event.map);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIIcons(IIconRegister register) {
        this.icons = new IIcon[iconNames.length];
        IntStream.range(0, this.icons.length).forEach(i -> this.icons[i] = register.registerIcon(iconNames[i]));
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIIcon(int par1) {
        return this.icons[par1];
    }
}