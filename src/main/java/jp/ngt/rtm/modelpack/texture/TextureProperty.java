package jp.ngt.rtm.modelpack.texture;

import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;
import net.minecraft.util.ResourceLocation;

public abstract class TextureProperty {
    /**
     * 使用する画像のパス
     */
    public String texture;
    /**
     * マイクラ内での大きさ
     */
    public float height, width, depth;

    protected ResourceLocation resource;

    protected void init() {
    }

    public ResourceLocation getTexture() {
        if (this.resource == null) {
            this.resource = new ResourceLocation(this.texture);
        }
        return this.resource;
    }

    public abstract TexturePropertyType getType();

    public int getUCountInGui() {
        return 4;
    }

    public int getVCountInGui() {
        return 2;
    }
}