package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.rtm.gui.GuiButtonSelectModel;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public interface IModelSetClient {
    void renderSelectButton(GuiButtonSelectModel par1, Minecraft par2, int par3, int par4);

    void renderModelInGui(Minecraft par1);

    IModelNGT getModelObject();
}