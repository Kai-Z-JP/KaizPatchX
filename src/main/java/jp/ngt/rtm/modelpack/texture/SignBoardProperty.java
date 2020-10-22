package jp.ngt.rtm.modelpack.texture;

import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;
import net.minecraft.util.ResourceLocation;

public class SignBoardProperty extends TextureProperty {
	public static final SignBoardProperty DUMMY = new SignBoardProperty();

	static {
		DUMMY.texture = "textures/signboard/default.png";
		DUMMY.width = 1.0F;
		DUMMY.height = 1.0F;
	}

	/**
	 * 0:裏表同じ, 1:裏表別, 2:裏テクスチャなし(側面の色と同じ)
	 */
	public int backTexture;
	/**
	 * フレーム数
	 */
	public int frame;
	/**
	 * アニメーションの間隔
	 */
	public int animationCycle;
	/**
	 * 側面の色
	 */
	public int color;
	/**
	 * 明るさ<br>
	 * 0~15:通常<br>
	 * -15~-1:RSオン時のみ明るくなる<br>
	 * -16:点滅(ランダム)<br>
	 */
	public int lightValue;

	@Override
	protected void init() {
		super.init();

		this.texture = fixName(this.texture);

		if (this.frame <= 0) {
			this.frame = 1;
		}

		if (this.animationCycle <= 0) {
			this.animationCycle = 1;
		}

		if (this.color < 0) {
			this.color = 0x101010;
		}
	}

	@Override
	public ResourceLocation getTexture() {
		if (this.resource == null) {
			if (this.texture.contains("textures")) {
				this.resource = new ResourceLocation(this.texture);
			} else {
				//1.7.10.31互換性
				this.resource = new ResourceLocation("textures/signboard/" + this.texture + ".png");
			}
		}
		return this.resource;
	}

	@Override
	public TexturePropertyType getType() {
		return TexturePropertyType.SignBoard;
	}

	public static String fixName(String par1) {
		if (!par1.contains("textures")) {
			return "textures/signboard/" + par1 + ".png";
		}
		return par1;
	}
}