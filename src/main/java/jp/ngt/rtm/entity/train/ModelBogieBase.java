package jp.ngt.rtm.entity.train;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.MCModel;

@SideOnly(Side.CLIENT)
public abstract class ModelBogieBase extends MCModel {
    public ModelBogieBase() {
        this(256, 256);
    }

    public ModelBogieBase(int width, int height) {
        this.init();
    }

    public abstract void init();
}