package jp.ngt.rtm.modelpack.cfg;

import java.util.Arrays;

public class TrainConfig extends VehicleBaseConfig implements IConfigWithType {
	public static final String TYPE = "ModelTrain";

	/**
	 * 車両名（重複不可）
	 */
	private String trainName;
	/**
	 * 車両のタイプ(EC:電車, DC:気動車, EL:電気機関車, DL:ディーゼル機関車, SL:蒸気機関車, CC:コンテナ車, TC:タンク車, N:なし)
	 */
	private String trainType;

	/**
	 * 車体モデル
	 */
	private ModelSource trainModel2;
	/**
	 * 台車モデル {front, back}
	 */
	private ModelSource[] bogieModel3;

	@Deprecated
	private ModelSource bogieModel2;
	@Deprecated
	private String trainModel;
	@Deprecated
	private String bogieModel;
	@Deprecated
	private String trainTexture;
	@Deprecated
	private String bogieTexture;

	/**
	 * ブレーキ緩解音
	 */
	public String sound_BrakeRelease;
	/**
	 * ブレーキ緩解音(弱め)
	 */
	public String sound_BrakeRelease2;
	/**
	 * ジョイント音を鳴らさない
	 */
	public boolean muteJointSound;
	/**
	 * 2回目のジョイント音の遅延具合(m)<br>
	 * [2][1~]
	 */
	public float[][] jointDelay;//3つ以上の車輪対応,台車前後別

	/**
	 * 単行かどうか
	 */
	public boolean isSingleTrain;

	/**
	 * 台車の位置{x, y, z}
	 */
	private float[][] bogiePos;
	/**
	 * 車体長の半分
	 */
	public float trainDistance;

	/**
	 * 加速度<br>
	 * N km/h/s -> N x 0.0006944
	 */
	public float accelerateion;

	/**
	 * 加速度ノッチ段数と同一<br>
	 * N km/h/s -> N x 0.0006944
	 */
	public float[] accelerateions;
	/**
	 * ノッチごとの速度上限(1~5段)
	 */
	public float[] maxSpeed;
	/**
	 * GUIオフ時は7段でデフォルトブレーキ段数固定
	 */
	public float[] deccelerations;

	/**
	 * カーブでの傾き具合(0.0~1.0)
	 */
	public float rolling;

	public float rollSpeedCoefficient;
	public float rollVariationCoefficient;
	public float rollWidthCoefficient;

	@Override
	public void init() {
		super.init();

		if (this.trainModel2 == null) {
			this.trainModel2 = new ModelSource();
			this.trainModel2.modelFile = this.trainModel;
			this.trainModel2.textures = new String[][]{{"default", this.trainTexture}};
		}

		if (this.bogieModel3 == null) {
			this.bogieModel3 = new ModelSource[2];
			if (this.bogieModel2 != null) {
				this.bogieModel3[0] = this.bogieModel3[1] = this.bogieModel2;
			} else {
				ModelSource model = new ModelSource();
				model.modelFile = this.bogieModel;
				model.textures = new String[][]{{"default", this.bogieTexture}};
				this.bogieModel3[0] = this.bogieModel3[1] = model;
			}
		}

		if (this.bogiePos == null) {
			this.bogiePos = new float[][]{{0.0F, 0.0F, 7.125F}, {0.0F, 0.0F, -7.125F}};
		}

		if (this.trainDistance <= 0.0F) {
			this.trainDistance = 10.125F;
		}

		if (this.accelerateion <= 0.0F) {
			this.accelerateion = 0.001736F;
		}

		if (this.maxSpeed == null || (!this.notDisplayCab && this.maxSpeed.length != 5)) {
			this.maxSpeed = new float[]{0.36F, 0.72F, 1.08F, 1.44F, 1.80F};
		}

		if (this.accelerateions == null || (!this.notDisplayCab && this.accelerateions.length != this.maxSpeed.length)) {
			this.accelerateions = new float[this.maxSpeed.length];
			Arrays.fill(this.accelerateions, this.accelerateion);
		}

		if (this.deccelerations == null || (!this.notDisplayCab && this.deccelerations.length != 9)) {
			this.deccelerations = new float[]{-0.0002F, -0.0005F, -0.001F, -0.0015F, -0.002F, -0.0025F, -0.003F, -0.0035F, -0.01F};
		}


		this.rolling *= 5.0F;

		if (this.jointDelay == null) {
			float f0 = 1.9F;
			this.jointDelay = new float[][]{{0.0F, f0}, {0.0F, f0}};
		}
	}

	@Override
	public String getName() {
		return this.trainName;
	}

	@Override
	public String getModelType() {
		return TYPE;
	}

	@Override
	public ModelSource getModel() {
		return this.trainModel2;
	}

	public ModelSource getBogieModel(int par1) {
		return this.bogieModel3[par1];
	}

	public float[][] getBogiePos() {
		return this.bogiePos;
	}

	/**
	 * モデル製作者の方へ<br>
	 * ここは無視してください
	 */
	public static TrainConfig getDummyConfig() {
		TrainConfig config = new TrainConfig();
		config.trainName = "Dummy";
		config.trainType = "N";
		config.init();
		return config;
	}

	@Override
	public String getSubType() {
		return this.trainType;
	}
}