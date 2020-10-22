package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TextureCoordinate {
	public static TextureCoordinate create(float u, float v, VecAccuracy par3) {
		switch (par3) {
			case LOW:
				return new TexCoordinateShort(u, v);
			case MEDIUM:
				return new TexCoordinateFloat(u, v);
			default:
				return new TexCoordinateFloat(u, v);
		}
	}

	public abstract float getU();

	public abstract float getV();

	public abstract TextureCoordinate copy();

	private static final class TexCoordinateFloat extends TextureCoordinate {
		private final float u, v;

		public TexCoordinateFloat(float u, float v) {
			this.u = u;
			this.v = v;
		}

		@Override
		public float getU() {
			return this.u;
		}

		@Override
		public float getV() {
			return this.v;
		}

		@Override
		public TextureCoordinate copy() {
			return new TexCoordinateFloat(this.getU(), this.getV());
		}
	}

	private static final class TexCoordinateShort extends TextureCoordinate {
		private final short u, v;

		public TexCoordinateShort(float u, float v) {
			this.u = this.encode(u);
			this.v = this.encode(v);
		}

		@Override
		public float getU() {
			return this.decode(this.u);
		}

		@Override
		public float getV() {
			return this.decode(this.v);
		}

		private short encode(float par1) {
			return (short) (par1 * 2000.0F);
		}

		private float decode(short par1) {
			return (float) par1 * 0.0005F;
		}

		@Override
		public TextureCoordinate copy() {
			return new TexCoordinateShort(this.getU(), this.getV());
		}
	}
}