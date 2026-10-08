package jp.ngt.ngtlib.math;

public final class BezierCurve implements ILine {
    public static final int QUANTIZE = 32;

    public final double[] sp;//StartPoint
    public final double[] cpS;//ControlPoint
    public final double[] cpE;//ControlPoint
    public final double[] ep;//EndPoint
    private final CubicBezier2D curve;
    private final BezierArcLength arcLength;

    /**
     * ベジェ曲線
     *
     * @param p1 開始点のX値
     * @param p2 開始点のY値
     * @param p3 制御点1のX値
     * @param p4 制御点1のY値
     * @param p5 制御点2のX値
     * @param p6 制御点2のY値
     * @param p7 終止点のX値
     * @param p8 終止点のY値
     */
    public BezierCurve(double p1, double p2, double p3, double p4, double p5, double p6, double p7, double p8) {
        this.sp = new double[]{p1, p2};
        this.cpS = new double[]{p3, p4};
        this.cpE = new double[]{p5, p6};
        this.ep = new double[]{p7, p8};
        this.curve = new CubicBezier2D(p1, p2, p3, p4, p5, p6, p7, p8);
        this.arcLength = new BezierArcLength(this.curve, QUANTIZE);
    }

    @Override
    public double[] getPoint(int split, int index) {
        return this.getPoint(split == 0 ? 0.0D : (double) index / split);
    }

    @Override
    public double[] getPoint(double ratio) {
        return this.curve.pointAt(this.arcLength.parameterAt(ratio));
    }

    @Override
    public double getSlope(int split, int index) {
        return this.getSlope(split == 0 ? 0.0D : (double) index / split);
    }

    @Override
    public double getSlope(double ratio) {
        return BezierCurveDirection.slopeAt(this.curve, this.arcLength.parameterAt(ratio));
    }

    @Override
    public double getLength() {
        return this.arcLength.getLength();
    }

    @Override
    public int getNearlestPoint(int split, double y, double x) {
        int nearest = -1;
        double shortest = Double.MAX_VALUE;
        for (int index = 0; index < split; index++) {
            double[] point = this.getPoint(split, index);
            double dx = x - point[0];
            double dy = y - point[1];
            double distance = dx * dx + dy * dy;
            if (distance < shortest) {
                shortest = distance;
                nearest = index;
            }
        }
        return nearest;
    }
}
