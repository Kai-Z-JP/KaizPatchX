package jp.ngt.rtm.modelpack.cfg;


public class RailConfig extends ModelConfig {
	public static final String TYPE = "ModelRail";

	/**
	 * 名前(重複不可)
	 */
	private String railName;
	/**
	 * モデル
	 */
	public ModelSource model;

	/**
	 * 道床幅, 1以上の奇数
	 */
	public int ballastWidth;
	/**
	 * 動物とかが線路を横切れるかどうか
	 */
	public boolean allowCrossing;
	/**
	 * クリエイティブタブに追加する際の道床ブロック
	 */
	public BallastSet[] defaultBallast;

	@Deprecated
	public String railModel;
	@Deprecated
	public String railTexture;

	@Override
	public void init() {
		super.init();

		if (this.model == null) {
			this.model = new ModelSource();
			this.model.modelFile = this.railModel;
			this.model.textures = new String[][]{{"default", this.railTexture}};
			this.model.rendererPath = null;
		}

		if (this.ballastWidth <= 0) {
			this.ballastWidth = 3;
		} else if ((this.ballastWidth & 1) == 0) {
			++this.ballastWidth;
		}
	}

	@Override
	public String getName() {
		return this.railName;
	}

	@Override
	public String getModelType() {
		return TYPE;
	}

	public static RailConfig getDummy() {
		RailConfig cfg = new RailConfig();
		cfg.railName = "dummy";
		cfg.railModel = "1067mm_Wood";
		return cfg;
	}

	public class BallastSet {
		public String blockName;
		public int blockMetadata;
		public float height;
	}
}