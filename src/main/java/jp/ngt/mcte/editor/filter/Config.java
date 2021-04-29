package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.filter.CfgParameter.CfgParameterBoolean;
import jp.ngt.mcte.editor.filter.CfgParameter.CfgParameterFloat;
import jp.ngt.mcte.editor.filter.CfgParameter.CfgParameterInt;
import jp.ngt.mcte.editor.filter.CfgParameter.CfgParameterString;
import jp.ngt.ngtlib.io.NGTText;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Config {
    private static final String SEPARATOR = "=";
    /**
     * パラメータは追加順に保持
     */
    public final Map<String, CfgParameter> parameters = new LinkedHashMap<>();

    public Config() {
    }

    private void addParameter(String name, CfgParameter par) {
        this.parameters.put(name, par);
    }

    public void addInt(String name, int value, int min, int max) {
        this.addParameter(name, new CfgParameterInt(value, min, max));
    }

    public int getInt(String name) {
        if (this.parameters.containsKey(name)) {
            return (Integer) this.parameters.get(name).getValue();
        } else {
            throw new IllegalArgumentException("Value Not Found");
        }
    }

    public void setInt(String name, int value) {
        if (this.parameters.containsKey(name)) {
            this.parameters.get(name).setValue(value);
        }
    }

    public void addFloat(String name, float value, float min, float max) {
        this.addParameter(name, new CfgParameterFloat(value, min, max));
    }

    public float getFloat(String name) {
        if (this.parameters.containsKey(name)) {
            return (Float) this.parameters.get(name).getValue();
        } else {
            throw new IllegalArgumentException("Value Not Found");
        }
    }

    public void setFloat(String name, float value) {
        if (this.parameters.containsKey(name)) {
            this.parameters.get(name).setValue(value);
        }
    }

    public void addBoolean(String name, boolean value) {
        this.addParameter(name, new CfgParameterBoolean(value));
    }

    public boolean getBoolean(String name) {
        if (this.parameters.containsKey(name)) {
            return (Boolean) this.parameters.get(name).getValue();
        } else {
            throw new IllegalArgumentException("Value Not Found");
        }
    }

    public void setBoolean(String name, boolean value) {
        if (this.parameters.containsKey(name)) {
            this.parameters.get(name).setValue(value);
        }
    }

    public void addString(String name, String value, String... list) {
        this.addParameter(name, new CfgParameterString(value, list));
    }

    public String getString(String name) {
        if (this.parameters.containsKey(name)) {
            return (String) this.parameters.get(name).getValue();
        } else {
            throw new IllegalArgumentException("Value Not Found");
        }
    }

    public String[] getStringList(String name) {
        if (this.parameters.containsKey(name)) {
            CfgParameter param = this.parameters.get(name);
            if (param instanceof CfgParameterString) {
                return ((CfgParameterString) param).paramList;
            }
        }
        return new String[0];
    }

    public void setString(String name, String value) {
        if (this.parameters.containsKey(name)) {
            this.parameters.get(name).setValue(value);
        }
    }

	/*public Map<String, CfgParameter> getParameters()
	{
		return this.parameters;
	}*/

    /**
     * ファイルから読み込み
     * ※初期化後に読み込むこと
     */
    public void load(File file) {
        this.load(NGTText.readText(file));
    }

    public void load(String[] sa) {
        Arrays.stream(sa).map(s -> s.split(SEPARATOR)).filter(sa2 -> this.parameters.containsKey(sa2[0])).forEach(sa2 -> this.parameters.get(sa2[0]).setRawValue(sa2[1]));
    }

    public void save(File file) {
        NGTText.writeToText(file, this.export());
    }

    public String[] export() {
        String[] sa = new String[this.parameters.size()];
        int index = 0;
        for (Entry<String, CfgParameter> entry : this.parameters.entrySet()) {
            Object value = entry.getValue().getValue();
            sa[index] = entry.getKey() + SEPARATOR + value.toString();
            ++index;
        }
        return sa;
    }
}