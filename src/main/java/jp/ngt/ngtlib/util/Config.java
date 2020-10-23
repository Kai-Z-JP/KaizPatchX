package jp.ngt.ngtlib.util;

import jp.ngt.ngtlib.io.NGTText;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Config {
	private final Map<String, Map<String, String>> itemMap = new HashMap<String, Map<String, String>>();

	public void load(File file) {
		String[] sa = NGTText.readText(file);
		if (sa.length > 0) {
			String category = "";
			for (String s : sa) {
				if (s.startsWith("#")) {
				} else if (s.startsWith("@")) {
					category = s.substring(1);
				} else if (s.contains("=")) {
					String[] sa1 = s.split("=");
					if (sa1.length == 2 && category.length() > 0) {
						this.setProperty(category, sa1[0], sa1[1]);
					}
				}
			}
		}
	}

	public boolean save(File file) {
		StringBuilder sb = new StringBuilder("#NGT_Configuration_File").append("\n");
		for (Entry<String, Map<String, String>> set0 : this.itemMap.entrySet()) {
			String category = set0.getKey();
			Map<String, String> value0 = set0.getValue();
			sb.append("\n").append("@").append(category).append("\n");

			for (Entry<String, String> set1 : value0.entrySet()) {
				String key = set1.getKey();
				String value1 = set1.getValue();
				sb.append(key).append("=").append(value1).append("\n");
			}
		}

		return NGTText.writeToText(file, sb.toString());
	}

	/**
	 * 戻り値nullなし
	 */
	public String getProperty(String category, String key) {
		if (this.itemMap.containsKey(category)) {
			Map<String, String> map = this.itemMap.get(category);
			if (map.containsKey(key)) {
				return map.get(key);
			}
		}
		return "";
	}

	public void setProperty(String category, String key, String value) {
		Map<String, String> map = null;
		if (this.itemMap.containsKey(category)) {
			map = this.itemMap.get(category);
		} else {
			map = new HashMap<String, String>();
		}
		map.put(key, value);
		this.itemMap.put(category, map);
	}

	public boolean containsKey(String category, String key) {
		if (this.itemMap.containsKey(category)) {
			Map<String, String> map = this.itemMap.get(category);
			return map.containsKey(key);
		}
		return false;
	}
}