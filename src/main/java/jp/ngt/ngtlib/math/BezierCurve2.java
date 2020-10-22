package jp.ngt.ngtlib.math;

import net.minecraft.util.MathHelper;

//まだ使ってない
public final class BezierCurve2 implements ILine {
	private static final int CV = 10000;
	private static final double CV_R = 1.0D / (double) CV;

	public final double[] sp;//StartPoint
	public final float[] cpS;//ControlPoint-SP
	public final float[] cpE;//ControlPoint-SP
	public final float[] ep;//EndPoint-SP

	private byte nbit = 15;
	private byte n3bit = (byte) (nbit * 3);
	private long N = 1L << nbit;//分割数

	private long[] cpSI;
	private long[] cpEI;
	private long[] epI;

	private long[] nb0 = new long[2];
	private long[] nb1 = new long[2];
	private long[] nb2 = new long[2];

	public BezierCurve2(double p1, double p2, double p3, double p4, double p5, double p6, double p7, double p8) {
		this.sp = new double[]{p1, p2};
		this.cpS = new float[]{(float) (p3 - p1), (float) (p4 - p2)};
		this.cpE = new float[]{(float) (p5 - p1), (float) (p6 - p2)};
		this.ep = new float[]{(float) (p7 - p1), (float) (p8 - p2)};

		double d = (double) CV;
		this.cpSI = new long[]{(long) ((double) this.cpS[0] * d), (long) ((double) this.cpS[1] * d)};
		this.cpEI = new long[]{(long) ((double) this.cpE[0] * d), (long) ((double) this.cpE[1] * d)};
		this.epI = new long[]{(long) ((double) this.ep[0] * d), (long) ((double) this.ep[1] * d)};
	}

	@Override
	public double[] getPoint(int par1, int par2) {
		this.nb0[0] = this.epI[0] + (3 * N - 3) * this.cpEI[0] + (3 * N * N - 6 * N + 3) * this.cpSI[0];
		this.nb0[1] = this.epI[1] + (3 * N - 3) * this.cpEI[1] + (3 * N * N - 6 * N + 3) * this.cpSI[1];

		this.nb1[0] = 6 * this.epI[0] + (6 * N - 18) * this.cpEI[0] + (18 - 12 * N) * this.cpEI[0];
		this.nb1[1] = 6 * this.epI[1] + (6 * N - 18) * this.cpEI[1] + (18 - 12 * N) * this.cpEI[1];

		this.nb2[0] = 6 * this.epI[0] - 18 * this.cpEI[0] + 18 * this.cpSI[0];
		this.nb2[1] = 6 * this.epI[1] - 18 * this.cpEI[1] + 18 * this.cpSI[1];
		return null;
	}

	private int[] getPointFromParameter(int par1, int par2) {
		int t = par2 < 0 ? 0 : (par2 > par1 ? par1 : par2);
		int tp = par1 - par2;
		int i0 = t * t * t;
		int i1 = 3 * t * t * tp;
		int i2 = 3 * t * tp * tp;
		int x = 0;//i0*epI[0] + i1*cpEI[0] + i2*cpSI[0];
		int y = 0;//i0*epI[1] + i1*cpEI[1] + i2*cpSI[1];
		return new int[]{x, y};
	}

	@Override
	public int getNearlestPoint(int par1, double par2, double par3) {
		return 0;
	}

	@Override
	public double getSlope(int par1, int par2) {
		return 0;
	}

	@Override
	public double getLength() {
		return 0;
	}

	private void setSpliteValue(int par1) {
		double d0 = (double) par1;
		this.cpSI[0] = MathHelper.floor_double((double) this.cpS[0] * d0);
		this.cpSI[1] = MathHelper.floor_double((double) this.cpS[1] * d0);
		this.cpEI[0] = MathHelper.floor_double((double) this.cpE[0] * d0);
		this.cpEI[1] = MathHelper.floor_double((double) this.cpE[1] * d0);
		this.epI[0] = MathHelper.floor_double((double) this.ep[0] * d0);
		this.epI[1] = MathHelper.floor_double((double) this.ep[1] * d0);
	}
}