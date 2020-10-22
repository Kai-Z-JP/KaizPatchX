package jp.ngt.rtm.modelpack.state;

public final class DataEntryHex extends DataEntryInt {
	public DataEntryHex(Integer value, int flag) {
		super(value, flag);
	}

	@Override
	public DataType getType() {
		return DataType.HEX;
	}

	@Override
	public String toString() {
		return "0x" + Integer.toHexString(this.data);
	}
}