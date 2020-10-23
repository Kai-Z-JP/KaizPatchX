package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public abstract class Vertex {
	public static Vertex create(Vec3 vec, VecAccuracy accuracy) {
		return create((float) vec.getX(), (float) vec.getY(), (float) vec.getZ(), accuracy);
	}

	public static Vertex create(float x, float y, float z, VecAccuracy par4) {
		switch (par4) {
			case LOW:
				return new VertexShort(x, y, z);
			case MEDIUM:
				return new VertexFloat(x, y, z);
			default:
				return new VertexFloat(x, y, z);
		}
	}

	public Vec3 toVec() {
		return PooledVec3.create(this.getX(), this.getY(), this.getZ());
	}

	public abstract float getX();

	public abstract float getY();

	public abstract float getZ();

	public abstract void setVec(float x, float y, float z);

	public Vertex add(Vertex vertex) {
		this.setVec(getX() + vertex.getX(), getY() + vertex.getY(), getZ() + vertex.getZ());
		return this;
	}

	public Vertex expand(float par1) {
		this.setVec(getX() * par1, getY() * par1, getZ() * par1);
		return this;
	}

	public void normalize() {
		double dx = this.getX();
		double dy = this.getY();
		double dz = this.getZ();
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		double l0 = 1.0D / length;
		this.setVec((float) (dx * l0), (float) (dy * l0), (float) (dz * l0));
	}

	public Vertex copy(VecAccuracy par1) {
		return create(this.getX(), this.getY(), this.getZ(), par1);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object instanceof Vertex) {
			Vertex v = (Vertex) object;
			return this.getX() == v.getX() && this.getY() == v.getY() && this.getZ() == v.getZ();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int ix = Math.abs((int) (this.getX() * 10.0F));
		int iy = Math.abs((int) (this.getY() * 10.0F));
		int iz = Math.abs((int) (this.getZ() * 10.0F));
		return ((ix & 0xFF) << 16) | ((iy & 0xFF) << 8) | ((iz & 0xFF));
	}

	private static final class VertexFloat extends Vertex {
		private float x, y, z;

		public VertexFloat(float x, float y, float z) {
			this.setVec(x, y, z);
		}

		@Override
		public float getX() {
			return this.x;
		}

		@Override
		public float getY() {
			return this.y;
		}

		@Override
		public float getZ() {
			return this.z;
		}

		@Override
		public void setVec(float p1, float p2, float p3) {
			this.x = p1;
			this.y = p2;
			this.z = p3;
		}
	}

	/**
	 * +-16.000の範囲まで
	 */
	private static final class VertexShort extends Vertex {
		private short x, y, z;

		public VertexShort(float x, float y, float z) {
			this.setVec(x, y, z);
		}

		@Override
		public float getX() {
			return this.decode(this.x);
		}

		@Override
		public float getY() {
			return this.decode(this.y);
		}

		@Override
		public float getZ() {
			return this.decode(this.z);
		}

		@Override
		public void setVec(float p1, float p2, float p3) {
			this.x = this.encode(p1);
			this.y = this.encode(p2);
			this.z = this.encode(p3);
		}

		private short encode(float par1) {
			return (short) MathHelper.floor_float(par1 * 2000.0F);
		}

		private float decode(short par1) {
			return (float) par1 * 0.0005F;
		}
	}
}