package jp.ngt.ngtlib.block;

public enum EnumFace {
	BOTTOM(0, new float[]{0.0F, -1.0F, 0.0F}, new float[]{1.0F, -1.0F, 1.0F}),
	TOP(1, new float[]{0.0F, 1.0F, 0.0F}, new float[]{1.0F, -1.0F, 1.0F}),
	BACK(2, new float[]{0.0F, 0.0F, -1.0F}, new float[]{1.0F, 1.0F, -1.0F}),
	FRONT(3, new float[]{0.0F, 0.0F, 1.0F}, new float[]{1.0F, 1.0F, -1.0F}),
	LEFT(4, new float[]{-1.0F, 0.0F, 0.0F}, new float[]{-1.0F, 1.0F, 1.0F}),
	RIGHT(5, new float[]{1.0F, 0.0F, 0.0F}, new float[]{-1.0F, 1.0F, 1.0F}),
	NONE(-1, null, null);

	public final int dir;
	public final float[] normal;
	public final float[] flip;

	EnumFace(int par1, float[] par2, float[] par3) {
		this.dir = par1;
		this.normal = par2;
		this.flip = par3;
	}

	public static EnumFace get(int par1) {
		if (par1 < 0 || par1 > 5) {
			return NONE;
		}
		return EnumFace.values()[par1];
	}
}