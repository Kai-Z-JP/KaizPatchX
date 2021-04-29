package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.IModelSetClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class GuiButtonSelectModel extends GuiButton {
    private final IModelSetClient modelSet;
    private final GuiSelectModel parentGui;
    public boolean notCheckPos;
    /**
     * 画面開く前に、このモデルが選択されていたか
     */
    public boolean isSelected = false;

    public GuiButtonSelectModel(int par1, int par2, int par3, IModelSetClient par4, String name, GuiSelectModel par5) {
        super(par1, par2, par3, 160, 32, name);
        this.modelSet = par4;
        this.parentGui = par5;
    }

    public GuiButtonSelectModel(int par1, int par2, int par3, IModelSetClient par4, String name) {
        this(par1, par2, par3, par4, name, null);
        this.notCheckPos = true;
    }

    @Override
    public void drawButton(Minecraft par1, int par2, int par3) {
        if (this.visible && (this.notCheckPos || (this.yPosition >= -20 && this.yPosition < this.parentGui.height + 20))) {
            this.modelSet.renderSelectButton(this, par1, par2, par3);
        }
    }
}