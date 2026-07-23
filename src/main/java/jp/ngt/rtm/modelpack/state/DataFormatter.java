package jp.ngt.rtm.modelpack.state;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.cfg.ResourceConfig;
import jp.ngt.rtm.modelpack.cfg.ResourceConfig.DMInitValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public final class DataFormatter {
    private final Map<String, IDataFilter> filterMap = new HashMap<>();
    private final Map<String, String[]> suggestionMap = new HashMap<>();
    private final Map<String, DataType> typeMap = new HashMap<>();
    private final Map<String, DMInitValue> definitionMap = new HashMap<>();
    private final DMInitValue[] initValues;

    public DataFormatter(@Nullable ResourceConfig cfg) {
        if (cfg == null || cfg.defaultValues == null) {
            this.initValues = new DMInitValue[0];
        } else {
            this.initValues = cfg.defaultValues;

            for (DMInitValue val : cfg.defaultValues) {
                DataType type = DataType.getType(val.type);
                if (type != null) {
                    this.addValue(type, val);
                }
            }
        }
    }

    private void addValue(DataType type, DMInitValue val) {
        this.typeMap.put(val.key, type);
        this.definitionMap.put(val.key, val);
        this.filterMap.put(val.key, data -> DataTypeHandlers.validateCandidate(type, data, val));

        DataType suggestionType = type == DataType.LIST
                ? DataEntryList.supportedElementType(val.elementType)
                : type;
        if (suggestionType == DataType.BOOLEAN) {
            val.suggestions = new String[]{String.valueOf(false), String.valueOf(true)};
        } else {
            if (val.suggestions != null && val.suggestions.length > 0) {
                /*if (val.suggestions[0].equals("-file")) {
                    if (filter != null) {
                        List<File> list = ModelPackManager.INSTANCE.fileCache;
                        val.suggestions = list.stream().map(File::getAbsolutePath).filter(filter::check).map(DataFormatter::pathToRL).toArray(String[]::new);
                    }
                } else */
                if (val.suggestions[0].equals("-value") && val.minmax != null
                        && val.minmax.length >= 2 && val.minmax[1] >= val.minmax[0]) {
                    int min = (int) val.minmax[0];
                    int max = (int) val.minmax[1];
                    val.suggestions = new String[max - min + 1];
                    IntStream.range(0, val.suggestions.length).forEach(i -> val.suggestions[i] = String.valueOf(i + min));
                }
            }
        }

        if (val.suggestions != null) {
            this.suggestionMap.put(val.key, val.suggestions);
        }
    }

    /**
     * ファイルパスをResourceLocationの書式に変換
     */
    private static String pathToRL(String path) {
        path = path.replace('\\', '/');
        int i0 = path.indexOf("assets") + 7;
        String s0 = path.substring(i0);
        int i1 = s0.indexOf('/');
        String domain = s0.substring(0, i1);
        String path2 = s0.substring(i1 + 1);
        return domain + ":" + path2;
    }

    public void initDataMap(DataMap dm) {
        dm.setFormatter(this);

        int flag = DataMap.SAVE_FLAG | (NGTUtil.isServer() ? DataMap.SYNC_FLAG : 0);

        Arrays.stream(this.initValues).forEach(val -> {
            String key = val.key;
            if (!dm.contains(key)) {
                DataType type = DataType.getType(val.type);
                if (type == null) {
                    NGTLog.debug("Failed to set value with invalid type : %s", key);
                    return;
                }
                try {
                    DataEntry<?> entry = DataTypeHandlers.createDefault(type, val, flag);
                    dm.setEntry(key, entry, flag);
                } catch (RuntimeException e) {
                    NGTLog.debug("Failed to set value : %s=%s", key, val.value);
                }
            }
        });
    }

    public boolean check(String key, DataEntry value) {
        DataType type = this.typeMap.get(key);
        DMInitValue definition = this.definitionMap.get(key);
        return type == null || definition == null || DataTypeHandlers.validateEntry(type, value, definition) == null;
    }

    public DataTypeValidationResult parseAndValidate(String key, String rawValue, int flag) {
        DataType type = this.typeMap.get(key);
        DMInitValue definition = this.definitionMap.get(key);
        if (type == null || definition == null) {
            return new DataTypeValidationResult(null, "Missing data definition");
        }
        return DataTypeHandlers.parseAndValidate(type, rawValue, definition, flag);
    }

    public IDataFilter getFilter(String key) {
        return this.filterMap.get(key);
    }

    public String[] getSuggestions(String key) {
        return this.suggestionMap.get(key);
    }
}
