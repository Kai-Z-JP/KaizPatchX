package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.texture.ITextureHolder;
import jp.ngt.rtm.modelpack.texture.TextureManager;
import jp.ngt.rtm.modelpack.texture.TextureProperty;
import jp.ngt.rtm.network.PacketTextureHolder;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Mouse;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSelectTexture extends GuiScreenCustom {
    public final ITextureHolder holder;
    private final List<TextureProperty> properties;
    private int currentScroll;
    private int prevScroll;
    private int uCount, vCount;
    private final int SPACING_TEXT_Y = 64;

    public GuiSelectTexture(ITextureHolder par1) {
        this.holder = par1;
        this.properties = TextureManager.INSTANCE.getTextureList(par1.getType());
    }

    @Override
    public void initGui() {
        int x = !this.properties.isEmpty() ? this.properties.get(0).getUWidthInGui() : this.width;
        int y = !this.properties.isEmpty() ? this.properties.get(0).getVHeightInGui() : this.height;

        this.uCount = Math.max(this.width / x, 1);
        this.vCount = Math.max(this.height / (y + SPACING_TEXT_Y), 1);

        int offsetX = (this.width - (x * this.uCount)) / 2;

        this.buttonList.clear();

        int yCount = (this.properties.size() / this.uCount) + 1;

        for (int v = 0; v < yCount; ++v) {
            for (int u = 0; u < this.uCount; ++u) {
                int index = v * this.uCount + u;
                if (index >= this.properties.size()) {
                    break;
                }
                TextureProperty prop = this.properties.get(index);
                float f0;
                if (prop.width > prop.height) {
                    f0 = (float) x / prop.width;
                } else {
                    f0 = (float) y / prop.height;
                }

                int w = (int) (prop.width * f0);
                int h = (int) (prop.height * f0);
                int xPos = x * u + ((x - w) / 2) + offsetX;
                int yPos = (y * v + (v * this.SPACING_TEXT_Y)) + ((y - h) / 2);
                this.buttonList.add(new GuiButtonSelectTexture(index, xPos, yPos, w, h, prop));
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 256) {
            this.mc.displayGuiScreen(null);
        }

        if (guibutton.id < this.properties.size()) {
            String name = ((GuiButtonSelectTexture) guibutton).property.texture;
            RTMCore.NETWORK_WRAPPER.sendToServer(new PacketTextureHolder(name, this.holder));
            this.mc.displayGuiScreen(null);//close
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();

        if (scroll != 0) {
            this.prevScroll = this.currentScroll;
            scroll = Integer.compare(scroll, 0);
            this.currentScroll -= scroll;

            if (this.currentScroll < 0) {
                this.currentScroll = 0;
            }

            int size2 = this.properties.size() / this.uCount;

            if (this.currentScroll >= size2) {
                this.currentScroll = size2 - 1;
            }

            this.renewButton(this.currentScroll);
        }
    }

    protected void renewButton(int scroll) {
        if (this.currentScroll != this.prevScroll) {
            int y = this.height / this.vCount;
            if (this.prevScroll > this.currentScroll) {
                y = -y;
            }

            for (Object o : this.buttonList) {
                ((GuiButtonSelectTexture) o).moveButton(y);
            }
        }
    }
}