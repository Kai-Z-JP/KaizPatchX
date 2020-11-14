package jp.ngt.ngtlib.math;

import jp.ngt.ngtlib.util.ObjectPool;
import net.minecraft.util.MathHelper;

import java.util.stream.IntStream;

/**
 * メモリ使用削減
 */
public final class PooledVec3 extends Vec3 {
	private static final ObjectPool<PooledVec3> POOL;

	static {
		final int size = 32;
		PooledVec3[] array1 = new PooledVec3[size];
		PooledVec3[] array2 = new PooledVec3[size];
		IntStream.range(0, size).forEach(i -> {
			array1[i] = new PooledVec3(0.0D, 0.0D, 0.0D);
			array2[i] = new PooledVec3(0.0D, 0.0D, 0.0D);
		});
		POOL = new ObjectPool<>(new PooledVec3[][]{array1, array2});
	}

	private PooledVec3(double x, double y, double z) {
		super(x, y, z);
	}

	public static Vec3 create(double x, double y, double z) {
		/*Vec3 vec = POOL.get();
		vec.set(x, y, z);
		return vec;*/
		return new Vec3(x, y, z);
	}

	/**
	 * @param par1 度
	 */
	@Override
	public Vec3 rotateAroundX(float par1) {
		float rad = NGTMath.toRadians(par1);
		float f1 = MathHelper.cos(rad);
		float f2 = MathHelper.sin(rad);
		double d0 = this.getX();
		double d1 = this.getY() * (double) f1 + this.getZ() * (double) f2;
		double d2 = this.getZ() * (double) f1 - this.getY() * (double) f2;
		return create(d0, d1, d2);
	}

	/**
	 * @param par1 度
	 */
	@Override
	public Vec3 rotateAroundY(float par1) {
		float rad = NGTMath.toRadians(par1);
		float f1 = MathHelper.cos(rad);
		float f2 = MathHelper.sin(rad);
		double d0 = this.getX() * (double) f1 + this.getZ() * (double) f2;
		double d1 = this.getY();
		double d2 = this.getZ() * (double) f1 - this.getX() * (double) f2;
		return create(d0, d1, d2);
	}

	/**
	 * @param par1 度
	 */
	@Override
	public Vec3 rotateAroundZ(float par1) {
		float rad = NGTMath.toRadians(par1);
		float f1 = MathHelper.cos(rad);
		float f2 = MathHelper.sin(rad);
		double d0 = this.getX() * (double) f1 + this.getY() * (double) f2;
		double d1 = this.getY() * (double) f1 - this.getX() * (double) f2;
		double d2 = this.getZ();
		return create(d0, d1, d2);
	}

	@Override
	public Vec3 add(double x, double y, double z) {
		return create(this.getX() + x, this.getY() + y, this.getZ() + z);
	}

	@Override
	public Vec3 multi(double num) {
		return create(this.getX() * num, this.getY() * num, this.getZ() * num);
	}

	/**
	 * 外積
	 */
	@Override
	public Vec3 crossProduct(Vec3 par1) {
		return create(
				this.getY() * par1.getZ() - this.getZ() * par1.getY(),
				this.getZ() * par1.getX() - this.getX() * par1.getZ(),
				this.getX() * par1.getY() - this.getY() * par1.getX()
		);
	}

	@Override
	public Vec3 normalize() {
		double length = this.length();
		if (length < 1.0E-4D) {
			return create(0.0D, 0.0D, 0.0D);
		}
		double d1 = 1.0D / length;
		return create(this.getX() * d1, this.getY() * d1, this.getZ() * d1);
	}
}