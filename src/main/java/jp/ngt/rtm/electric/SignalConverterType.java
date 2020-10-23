package jp.ngt.rtm.electric;

public enum SignalConverterType {
	RSIn(0),
	RSOut(1),
	Increment(2),
	Decrement(3),
	Wireless(4);

	public final byte id;

	SignalConverterType(int p1) {
		this.id = (byte) p1;
	}

	public static SignalConverterType getType(int p1) {
		if (p1 < 0 || p1 > SignalConverterType.values().length) {
			return SignalConverterType.RSIn;
		}
		return SignalConverterType.values()[p1];
	}
}