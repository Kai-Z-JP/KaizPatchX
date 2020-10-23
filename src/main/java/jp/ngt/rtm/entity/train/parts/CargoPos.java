package jp.ngt.rtm.entity.train.parts;

public enum CargoPos {
	C12FT(0.0F, 4.0F, new float[]{8.0F, 4.0F, 0.0F, -4.0F, -8.0F}),
	C20FT(4.0F, 6.6F, new float[]{20.0F, 6.6F, 0.0F, -6.6F, 20.0F}),
	C24FT(6.6F, 8.0F, new float[]{20.0F, 6.0F, 20.0F, -6.0F, 20.0F}),
	C30FT(8.0F, 10.0F, new float[]{20.0F, 5.0F, 0.0F, -5.0F, 20.0F}),
	C40FT(10.0F, 20.0F, new float[]{20.0F, 20.0F, 0.0F, 20.0F, 20.0F});

	public final float min;
	public final float max;
	public final float[] zPos;

	CargoPos(float p1, float p2, float[] p3) {
		this.min = p1;
		this.max = p2;
		this.zPos = p3;
	}

	public static CargoPos getCargoPos(float size) {
		for (CargoPos cp : CargoPos.values()) {
			if (size > cp.min && size <= cp.max) {
				return cp;
			}
		}
		return C40FT;
	}
}