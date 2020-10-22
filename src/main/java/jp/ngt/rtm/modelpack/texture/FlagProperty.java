package jp.ngt.rtm.modelpack.texture;

import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;

public class FlagProperty extends TextureProperty {
	public static final FlagProperty DUMMY = new FlagProperty();

	static {
		DUMMY.texture = "textures/flag/flag_RTM3Anniversary.png";
		DUMMY.width = 1.0F;
		DUMMY.height = 1.0F;
	}

	/**
	 * 縦横の解像度
	 */
	public int resolutionV, resolutionU;

	@Override
	protected void init() {
		super.init();

		if (this.resolutionU <= 0) {
			this.resolutionU = 24;
		}

		if (this.resolutionV <= 0) {
			this.resolutionV = 16;
		}
	}

	@Override
	public TexturePropertyType getType() {
		return TexturePropertyType.Flag;
	}
}