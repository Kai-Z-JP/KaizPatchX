package jp.ngt.ngtlib.math;

import net.minecraft.util.MathHelper;

public final class StraightLine implements ILine {
    public final double startX;
    public final double startY;
    public final double endX;
    public final double endY;

    private final double length;
    private final double slopeAngle;
    /**
     * 傾き
     */
    private final double slope;
    /**
     * 切片
     */
    private final double intercept;

    public StraightLine(double p1, double p2, double p3, double p4) {
        this.startX = p1;
        this.startY = p2;
        this.endX = p3;
        this.endY = p4;

        double dx = p3 - p1;
        double dy = p4 - p2;
        if (dx == 0.0D) {
            this.slope = Double.NaN;
            this.intercept = p1;
        } else {
            this.slope = dy / dx;
            this.intercept = p2 - this.slope * p1;
        }
        this.length = Math.sqrt(dx * dx + dy * dy);
        this.slopeAngle = Math.atan2(dy, dx);
    }

    @Override
    public double[] getPoint(int par1, int par2) {
        int i0 = par2 < 0 ? 0 : (Math.min(par2, par1));
        double d0 = (double) i0 / (double) par1;
        double x = this.startX + ((this.endX - this.startX) * d0);
        double y = this.startY + ((this.endY - this.startY) * d0);
        return new double[]{x, y};
    }

    @Override
    public int getNearlestPoint(int par1, double y, double x) {
        double t;
        if (Double.isNaN(this.slope)) {
            t = (y - this.startY) / (this.endY - this.startY);
            int n = 0;
        } else {
            double a21 = 1.0D / (this.slope * this.slope + 1.0D);
            double x0 = (x + this.slope * y - this.slope * this.intercept) * a21;
            //double y0 = (this.slope * x + this.slope * this.slope * y + this.intercept) * a21;
            t = (x0 - this.startX) / (this.endX - this.startX);
        }
        return MathHelper.floor_double(t * (double) par1);
    }

    @Override
    public double getSlope(int par1, int par2) {
        return this.slopeAngle;
    }

    @Override
    public double getLength() {
        return this.length;
    }
}