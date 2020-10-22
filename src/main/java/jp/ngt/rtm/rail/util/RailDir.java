package jp.ngt.rtm.rail.util;

public enum RailDir {
	/**
	 * =-1
	 */
	RIGHT(-1),
	/**
	 * =1
	 */
	LEFT(1),
	/**
	 * =0
	 */
	NONE(0);

	public final byte id;

	private RailDir(int par1) {
		this.id = (byte) par1;
	}

	/**
	 * 向きを反転
	 */
	public RailDir invert() {
		return (this == RIGHT) ? LEFT : (this == LEFT) ? RIGHT : NONE;
	}
}