package jp.ngt.rtm.modelpack.cfg;

public class SignalConfig extends ModelConfig {
	public static final String TYPE = "ModelSignal";

	/**
	 * 名前(重複不可)
	 */
	private String signalName;
	/**
	 * モデル
	 */
	public ModelSource model;
	/**
	 * 本体の回転
	 */
	public boolean rotateBody;

	@Deprecated
	public String signalModel;
	@Deprecated
	public String signalTexture;
	@Deprecated
	public String lightTexture;
	@Deprecated
	public Parts modelPartsFixture;//パーツ,固定具
	@Deprecated
	public Parts modelPartsBody;//パーツ,本体
	/**
	 * ライト<br>
	 * "S(<どの信号で点灯するか:1~1024>) I(<点滅間隔:0~1200>) P(partsA partsB ...)"<br>
	 * パーツ名はスペースで分けること
	 */
	@Deprecated
	public String[] lights;

	@Override
	public void init() {
		super.init();

		if (this.model == null) {
			this.model = new ModelSource();
			this.model.modelFile = this.signalModel;
			this.model.textures = new String[][]{
					{"default", this.signalTexture, "Light", this.lightTexture}};
		}

		if (this.modelPartsFixture == null) {
			this.modelPartsFixture = new Parts();
		}

		if (this.modelPartsBody == null) {
			this.modelPartsBody = new Parts();
		}

		this.modelPartsFixture.initParts();
		this.modelPartsBody.initParts();
	}

	@Override
	public String getName() {
		return this.signalName;
	}

	@Override
	public String getModelType() {
		return TYPE;
	}

	public static SignalConfig getDummyConfig() {
		SignalConfig config = new SignalConfig();
		config.signalName = "DummySignal";
		config.init();
		return config;
	}
}