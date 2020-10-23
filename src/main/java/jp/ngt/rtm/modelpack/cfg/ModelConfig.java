package jp.ngt.rtm.modelpack.cfg;


public abstract class ModelConfig {
	/**
	 * 選択ボタンのテクスチャのパス
	 */
	public String buttonTexture;

	/**
	 * 検索用タグ
	 */
	public String tags;

	/**
	 * ngto専用
	 */
	public float scale;
	public float[] offset;

	/**
	 * スムージング
	 */
	public boolean smoothing;
	/**
	 * 片面表示を行う(Obj, MqoではONのほうが軽くなる)
	 */
	public boolean doCulling;
	/**
	 * ポリゴンの精度<br>
	 * "LOW":+-16.000の範囲のみ正常に描画される<br>
	 * "MEDIUM":通常<br>
	 */
	public String accuracy;

	public String serverScriptPath;

	public float[] renderAABB;

	/**
	 * Configの初期化時に呼ばれる
	 */
	public void init() {
		if (this.tags == null) {
			this.tags = "";
		}

		if (this.scale <= 0.0F) {
			this.scale = 1.0F;
		}

		if (this.offset == null || this.offset.length != 3) {
			this.offset = new float[3];
		}

		if (this.renderAABB == null || this.renderAABB.length != 6) {
			this.renderAABB = new float[]{0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F};
		}
	}

	/**
	 * 他のConfigと重複しない名前
	 */
	public abstract String getName();

	/**
	 * ModelSetのタイプ
	 */
	public abstract String getModelType();

	public class Parts {
		/**
		 * パーツを構成するオブジェクトの名前
		 */
		public String[] objects;
		/**
		 * 中心座標
		 */
		public float[] pos;

		public void initParts() {
			if (this.objects == null) {
				this.objects = new String[0];
			}

			if (this.pos == null || this.pos.length != 3) {
				this.pos = new float[3];
			}
		}
	}

	public class ModelSource {
		/**
		 * モデルファイルのパス(拡張子つきで)
		 */
		public String modelFile;

		/**
		 * マテリアル名と、それに対応するテクスチャファイルのパス<br>
		 * {マテリアル名, テクスチャパス, ("Light", "AlphaBlend")}<br>
		 * xxx_light0.png(消灯), xxx_light1.png(前照灯), xxx_light2.png（尾灯）
		 */
		public String[][] textures;

		public String rendererPath;
	}
}