package jp.ngt.rtm.modelpack.cfg;

import jp.ngt.rtm.modelpack.state.DataMap;

import java.util.Arrays;

/**
 * 全てのコンフィグの親, JSONからの読み込みに使用
 */
public abstract class ResourceConfig {
    /**
     * name重複時の優先度決定用
     */
    public short version;

    public boolean useCustomColor;

    /**
     * 検索用タグ
     */
    public String tags;

    /**
     * DataMapのデフォルト値
     */
    public DMInitValue[] defaultValues;
    @Deprecated
    public String defaultData;

    /**
     * Resource名, 重複不可
     */
    public abstract String getName();

    /**
     * Configの初期化時に呼ばれる
     */
    public void init() {
        if (this.defaultValues == null && this.defaultData != null) {
            String[][] array = DataMap.convertArg(this.defaultData);
            this.defaultValues = Arrays.stream(array).map(data -> {
                DMInitValue dmiv = new DMInitValue();
                dmiv.key = data[0];
                dmiv.type = data[1];
                dmiv.value = data[2];
                return dmiv;
            }).toArray(DMInitValue[]::new);
        }
    }

    protected String fixSoundPath(String path) {
        if (path == null || path.length() == 0) {
            return null;
        } else if (!path.contains(":")) {
            return "rtm:" + path;//ドメイン未指定のファイルパスがminecraftドメインと解釈されないように
        }
        return path;
    }

    public static class DMInitValue {
        public String type;
        public String key;
        public String value;
        /**
         * 入力候補
         */
        public String[] suggestions;
        /**
         * [min, max]
         */
        public double[] minmax;
        /**
         * [start, contains, end]
         */
        public String[] pattern;
    }
}