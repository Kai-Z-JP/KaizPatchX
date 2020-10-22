package jp.ngt.rtm.modelpack.cfg;

public class ContainerConfig extends ModelConfig {
	public static final String TYPE = "ModelContainer";

	/**
	 * 名前(重複不可)
	 */
	private String containerName;
	/**
	 * モデル
	 */
	public String containerModel;
	/**
	 * テクスチャ
	 */
	public String containerTexture;

	/**
	 * 当たり判定に使用
	 */
	public float containerWidth;
	/**
	 * 当たり判定に使用
	 */
	public float containerHeight;
	/**
	 * 貨車上での位置調整に使用
	 */
	public float containerLength;

	@Override
	public void init() {
		super.init();

		if (this.containerWidth <= 0.0F) {
			this.containerWidth = 1.0F;
		}

		if (this.containerHeight <= 0.0F) {
			this.containerHeight = 1.0F;
		}

		if (this.containerLength <= 0.0F) {
			this.containerLength = 1.0F;
		}
	}

	@Override
	public String getName() {
		return this.containerName;
	}

	@Override
	public String getModelType() {
		return TYPE;
	}

	public static ContainerConfig getDummy() {
		ContainerConfig cfg = new ContainerConfig();
		cfg.containerName = "dummy";
		return cfg;
	}
}