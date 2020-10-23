package jp.ngt.ngtlib.util;

public final class ObjectPool<T> {
	private final T[][] pool;
	private final int[] index = {0, 0};

	public ObjectPool(T[][] array) {
		this.pool = array;
	}

	public T get() {
		int i0 = NGTUtil.isServer() ? 0 : 1;
		int i1 = this.index[i0];
		this.index[i0] = (i1 + 1) % this.pool[i0].length;
		return this.pool[i0][i1];
	}
}
