package jp.ngt.rtm.modelpack.texture;

import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;

public interface ITextureHolder<T> {
	T getProperty();

	void setTexture(String name);

	TexturePropertyType getType();
}