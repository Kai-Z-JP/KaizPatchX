package jp.ngt.ngtlib.math;

import net.minecraft.util.MathHelper;

public class VecHelper {
	public static float[] rotateAroundX(float x, float y, float z, float rotation) {
		float r = NGTMath.toRadians(rotation);
		float f1 = MathHelper.cos(r);
		float f2 = MathHelper.sin(r);
		float y2 = y * f1 + z * f2;
		float z2 = z * f1 - y * f2;
		return new float[]{x, y2, z2};
	}

	public static float[] rotateAroundY(float x, float y, float z, float rotation) {
		float r = NGTMath.toRadians(rotation);
		float f1 = MathHelper.cos(r);
		float f2 = MathHelper.sin(r);
		float x2 = x * f1 + z * f2;
		float z2 = z * f1 - x * f2;
		return new float[]{x2, y, z2};
	}

	public static float[] rotateAroundZ(float x, float y, float z, float rotation) {
		float r = NGTMath.toRadians(rotation);
		float f1 = MathHelper.cos(r);
		float f2 = MathHelper.sin(r);
		float x2 = x * f1 + y * f2;
		float y2 = y * f1 - x * f2;
		return new float[]{x2, y2, z};
	}
}