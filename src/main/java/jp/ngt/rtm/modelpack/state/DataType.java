package jp.ngt.rtm.modelpack.state;

public enum DataType {
	BOOLEAN("Boolean"),
	DOUBLE("Double"),
	INT("Int"),
	STRING("String"),
	VEC("Vec"),
	HEX("Hex"),
	;

	public final String key;

	private DataType(String par1) {
		this.key = par1;
	}

	public static DataType getType(String s) {
		for (DataType type : DataType.values()) {
			if (type.key.equals(s)) {
				return type;
			}
		}
		return null;
	}

	/*public interface DataEntryConverter
	{
		DataEntry toEntry(String type, String data, int flag);
	}*/
}