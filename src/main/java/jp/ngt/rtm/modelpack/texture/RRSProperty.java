package jp.ngt.rtm.modelpack.texture;

import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;

public class RRSProperty extends TextureProperty {
    public static final RRSProperty DUMMY = new RRSProperty("");

    static {
        DUMMY.texture = "textures/rrs/rrs_01.png";
    }

    @Override
    public void init() {
        super.init();

        this.width = 0.5F;
        this.height = 0.5F;
    }

    public RRSProperty(String name) {
        this.texture = fixName(name);
    }

    @Override
    public TexturePropertyType getType() {
        return TexturePropertyType.RRS;
    }

    public static String fixName(String par1) {
        if (!par1.contains("textures")) {
            return "textures/rrs/" + par1;
        }
        return par1;
    }

    @Override
    public int getUCountInGui() {
        return 8;
    }

    @Override
    public int getUWidthInGui() {
        return 50;
    }

    @Override
    public int getVCountInGui() {
        return 4;
    }

    @Override
    public int getVHeightInGui() {
        return 50;
    }
}