package jp.ngt.rtm.modelpack.state;

import java.util.Arrays;

public enum DataType {
	BOOLEAN("Boolean"),
	DOUBLE("Double"),
	INT("Int"),
	STRING("String"),
	VEC("Vec"),
	HEX("Hex"),
	;

	public final String key;

	DataType(String par1) {
		this.key = par1;
	}

	public static DataType getType(String s) {
		return Arrays.stream(DataType.values()).filter(type -> type.key.equals(s)).findFirst().orElse(null);
	}

	/*public interface DataEntryConverter
	{
		DataEntry toEntry(String type, String data, int flag);
	}*/
}