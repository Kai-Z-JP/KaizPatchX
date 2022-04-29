package jp.ngt.rtm.modelpack.state;

import jp.ngt.ngtlib.io.NGTLog;
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
        IDataFilter filter;

        if ((type == DataType.INT || type == DataType.HEX) && val.minmax != null) {
            filter = data -> {
                int iData = data instanceof String ? Integer.parseInt((String) data) : (int) data;
                return iData >= val.minmax[0] && iData <= val.minmax[1];
            };
        } else if (type == DataType.DOUBLE && val.minmax != null) {
            filter = data -> {
                double dData = data instanceof String ? Double.parseDouble((String) data) : (double) data;
                return dData >= val.minmax[0] && dData <= val.minmax[1];
            };
        } else if (type == DataType.STRING && val.pattern != null) {
            filter = data -> {
                String s0 = (String) data;
                String start = val.pattern[0];
                String contains = val.pattern[1];
                String end = val.pattern[2];
                return (start.isEmpty() || s0.startsWith(start)) && (contains.isEmpty() || s0.contains(contains)) && (end.isEmpty() || s0.endsWith(end));
            };
        } else {
            filter = null;
        }

        if (filter != null) {
            this.filterMap.put(val.key, filter);
        }

        if (type == DataType.BOOLEAN) {
            val.suggestions = new String[]{String.valueOf(false), String.valueOf(true)};
        } else {
            if (val.suggestions != null) {
                /*if (val.suggestions[0].equals("-file")) {
                    if (filter != null) {
                        List<File> list = ModelPackManager.INSTANCE.fileCache;
                        val.suggestions = list.stream().map(File::getAbsolutePath).filter(filter::check).map(DataFormatter::pathToRL).toArray(String[]::new);
                    }
                } else */
                if (val.suggestions[0].equals("-value")) {
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

        Arrays.stream(this.initValues).forEach(val -> {
            String key = val.key;
            String value = String.format("(%s)%s", val.type, val.value);
            if (!dm.contains(key)) {
                if (!dm.set(key, value, DataMap.SYNC_FLAG | DataMap.SAVE_FLAG)) {
                    NGTLog.debug("Failed to set value : %s=%s", key, value);
                }
            }
        });
    }

    public boolean check(String key, DataEntry value) {
        IDataFilter filter = this.filterMap.get(key);
        return filter == null || filter.check(value.get());
    }

    public IDataFilter getFilter(String key) {
        return this.filterMap.get(key);
    }

    public String[] getSuggestions(String key) {
        return this.suggestionMap.get(key);
    }
}
