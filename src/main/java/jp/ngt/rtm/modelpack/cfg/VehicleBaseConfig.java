package jp.ngt.rtm.modelpack.cfg;

import java.util.stream.IntStream;

public abstract class VehicleBaseConfig extends ModelConfig {
    /**
     * {width, height}
     */
    private float[] size;

    /**
     * 方向幕のテクスチャのパス
     */
    public String rollsignTexture;
    /**
     * 方向幕の名前
     */
    public String[] rollsignNames;
    public Rollsign[] rollsigns;

    /**
     * [ボタン][ボタンの状態名]
     */
    public String[][] customButtons;

    public VehicleParts[] door_left;
    public VehicleParts[] door_right;
    public VehicleParts[] pantograph_front;
    public VehicleParts[] pantograph_back;

    /**
     * 停車時のサウンド
     */
    public String sound_Stop;
    /**
     * 停車-走行時のサウンド
     */
    public String sound_S_A;
    /**
     * 走行時のサウンド
     */
    public String sound_Acceleration;
    /**
     * 走行時のサウンド
     */
    public String sound_Deceleration;
    /**
     * 走行-停車時のサウンド
     */
    public String sound_D_S;
    /**
     * 警笛のサウンド
     */
    public String sound_Horn;
    public String sound_DoorOpen;
    public String sound_DoorClose;

    /**
     * ATSのサウンド
     */
    public String sound_ATSChime;
    public String sound_ATSBell;

    /**
     * 車内放送 {name, sound}
     */
    public String[][] sound_Announcement;

    public String soundScriptPath;

    /**
     * 煙を出す<br>
     * {float x, float y, float z, String name, int min, int max, float speed}<br>
     */
    public Object[][] smoke;

    /**
     * 前照灯
     */
    public Light[] headLights;
    /**
     * 尾灯
     */
    public Light[] tailLights;
    /**
     * 車内灯
     */
    public Light[] interiorLights;

    /**
     * 座席の位置 {x, y, z, type}<br>
     * <br>
     * typeには座席の種類を指定<br>
     * 0:なし<br>
     * 1:クロスシート（モデル表示あり）<br>
     * 2:クロスシート・ロングシート（モデル表示なし、車両モデルに座席を用意しているのであればこちらを指定する）)<br>
     * 3:寝台(未実装)
     */
    private float[][] slotPos;
    @Deprecated
    private int[][] seatPos;
    /**
     * プレーヤーの座る位置{x, y, z}
     */
    protected float[][] playerPos;

    /**
     * 運転台を画面に表示しない
     */
    public boolean notDisplayCab;

    public float wheelRotationSpeed;

    @Override
    public void init() {
        super.init();

        if (this.size == null) {
            this.size = new float[]{2.75F, 1.25F};
        }

        if (this.slotPos == null) {
            if (this.seatPos != null) {
                this.slotPos = new float[this.seatPos.length][];
                IntStream.range(0, this.slotPos.length).forEach(i -> {
                    float x = (float) this.seatPos[i][0] * 0.0625F;
                    float y = (float) this.seatPos[i][1] * 0.0625F;
                    float z = (float) this.seatPos[i][2] * 0.0625F;
                    float type = (float) this.seatPos[i][3];
                    this.slotPos[i] = new float[]{x, y, z, type};
                });
            } else {
                this.slotPos = new float[][]{};
            }
        }

        if (this.playerPos == null) {
            this.playerPos = new float[][]{{0.8F, 0.0F, 9.187F}, {-0.8F, 0.0F, -9.187F}};
        }

        if (this.wheelRotationSpeed <= 0.0F) {
            this.wheelRotationSpeed = 1.0F;
        }

        if (this.customButtons == null) {
            this.customButtons = new String[][]{};
        }
    }

    public abstract ModelSource getModel();

    public float[] getSize() {
        return this.size;
    }

    public float[][] getSlotPos() {
        return this.slotPos;
    }

    public float[][] getPlayerPos() {
        return this.playerPos;
    }


    /**
     * 方向幕の位置とマッピングを定義するクラス
     */
    public static class Rollsign {
        /**
         * 方向幕のマッピング<br>
         * テクスチャ上で方向幕に使いたい部位(１つだけではなく、全て)を指定<br>
         * {uMin, uMax, vMin, vMax}<br>
         * uvは0.0~1.0の値<br>
         */
        public float[] uv;

        /**
         * 方向幕の位置、複数可<br>
         * テクスチャを貼る面をその頂点4つで指定<br>
         * {{{点1(x,y,z)},{点2},{点3},{点4}}, ...}
         */
        public float[][][] pos;

        /**
         * 方向幕を動かすかどうか
         */
        public boolean doAnimation;
        /**
         * 光らせない
         */
        public boolean disableLighting;
    }

    public static class Light {
        public byte type;
        public int color;
        public float[] pos;
        public float r;
        /**
         * 向き反転
         */
        public boolean reverse;
    }

    public static class VehicleParts extends Parts {
        public VehicleParts[] childParts;
        /**
         * 移動:{x, y, z}<br>
         * 回転:{angle, vecX, vecY, vecZ}
         */
        public float[][] transform;
    }
}