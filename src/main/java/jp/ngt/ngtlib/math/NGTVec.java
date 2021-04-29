package jp.ngt.ngtlib.math;

import net.minecraft.util.Vec3;

/**
 * 一部メソッドで自身を再利用する
 */
public class NGTVec extends Vec3 {
    public NGTVec(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3 setValue(double x, double y, double z) {
        return this.setComponents(x, y, z);
    }

    public Vec3 setValue(Vec3 par1) {
        return this.setComponents(par1.xCoord, par1.yCoord, par1.zCoord);
    }

    @Override
    public Vec3 addVector(double x, double y, double z) {
        return this.setValue(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    public Vec3 addVector(Vec3 par1) {
        return this.addVector(par1.xCoord, par1.yCoord, par1.zCoord);
    }

    @Override
    public Vec3 crossProduct(Vec3 par1) {
        return this.setValue(this.yCoord * par1.zCoord - this.zCoord * par1.yCoord,
                this.zCoord * par1.xCoord - this.xCoord * par1.zCoord,
                this.xCoord * par1.yCoord - this.yCoord * par1.xCoord);
    }

    @Override
    public Vec3 normalize() {
        double length = this.lengthVector();
        if (length < 1.0E-4D) {
            return this.setValue(0.0D, 0.0D, 0.0D);
        }
        double d1 = 1.0D / length;
        return this.setValue(this.xCoord * d1, this.yCoord * d1, this.zCoord * d1);
    }

    public float getYaw() {
        return (float) NGTMath.toDegrees(Math.atan2(this.xCoord, this.zCoord));
    }

    public float getPitch() {
        double xz = Math.sqrt(this.xCoord * this.xCoord + this.zCoord * this.zCoord);
        return (float) NGTMath.toDegrees(Math.atan2(this.yCoord, xz));
    }

    public double getX() {
        return this.xCoord;
    }

    public double getY() {
        return this.yCoord;
    }

    public double getZ() {
        return this.zCoord;
    }
}