package jp.ngt.ngtlib.math;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public final class NGTMath {
    public static final Random RANDOM = new Random();

    public static final float PI;
    private static final double TO_RAD;
    private static final double TO_DEG;
    private static final double[] RAND_TABLE = new double[65536];
    private static int RAND_COUNT = 0;

    static {
        PI = (float) Math.PI;
        TO_RAD = Math.PI / 180.0D;
        TO_DEG = 180.0D / Math.PI;

        Arrays.setAll(RAND_TABLE, i -> RANDOM.nextDouble());
    }

    /**
     * ラジアンから度
     */
    public static float toDegrees(float par1) {
        return par1 * (float) TO_DEG;
    }

    /**
     * 度からラジアン
     */
    public static float toRadians(float par1) {
        return par1 * (float) TO_RAD;
    }

    public static double toDegrees(double par1) {
        return par1 * TO_DEG;
    }

    public static double toRadians(double par1) {
        return par1 * TO_RAD;
    }

    /**
     * 0 ~ 360
     */
    public static double normalizeAngle(double par1) {
        double d0 = par1;

        while (d0 >= 360.0D) {
            d0 -= 360.0D;
        }

        while (d0 < 0.0D) {
            d0 += 360.0D;
        }
        return d0;
    }

    /**
     * @param random
     * @param sd     : 分散
     */
    public static double getGaussian(Random random, double sd) {
        double z = Math.sqrt(-2.0D * StrictMath.log(random.nextDouble())) * Math.sin(2.0D * Math.PI * random.nextDouble());
        return sd * z;

	    /*double z = Math.sqrt(-2.0D * StrictMath.log(random.nextDouble())) * Math.sin(2.0D * Math.PI * random.nextDouble());
	    return ex + sd*z;*/
        //ex : 平均
    }

    public static int getRandomInt(int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");
        RAND_COUNT = (RAND_COUNT + 1) % RAND_TABLE.length;
        return (int) ((float) RAND_TABLE[RAND_COUNT] * (float) n);
    }

    /**
     * @param par1
     * @param par2 Min
     * @param par3 Max
     * @param par4 Default
     */
    public static int getIntFromString(String par1, int par2, int par3, int par4) {
        if (par1 == null) {
            return par4;
        }

        try {
            int num = Integer.parseInt(par1);
            if (num < par2) {
                return par2;
            } else if (num > par3) {
                return par3;
            }
            return num;
        } catch (NumberFormatException e) {
            return par4;
        }
    }

    /**
     * @param par1
     * @param par2 Min
     * @param par3 Max
     * @param par4 Default
     */
    public static float getFloatFromString(String par1, float par2, float par3, float par4) {
        if (par1 == null) {
            return par4;
        }

        try {
            float num = Float.parseFloat(par1);
            if (num < par2) {
                return par2;
            } else if (num > par3) {
                return par3;
            }
            return num;
        } catch (NumberFormatException e) {
            return par4;
        }
    }

    public static double pow(double par1, int par2) {
        if (par2 <= 0) {
            return 1.0D;
        } else {
            return IntStream.range(0, par2).mapToDouble(i -> par1).reduce(1.0D, (a, b) -> a * b);
        }
    }

    public static boolean isVecEquals(Vec3 v1, Vec3 v2) {
        return v1.xCoord == v2.xCoord && v1.yCoord == v2.yCoord && v1.zCoord == v2.zCoord;
    }

    /**
     * @return radian
     */
    public static double getAngle(Vec3 v1, Vec3 v2) {
        double d0 = (v1.xCoord * v2.xCoord + v1.yCoord * v2.yCoord + v1.zCoord * v2.zCoord) / (v1.lengthVector() * v2.lengthVector());
        return Math.acos(d0);
    }

    public static double getAngle(double sX, double sY, double eX, double eY) {
        double dX = eX - sX;
        double dY = eY - sY;
        return Math.atan(dY / dX);
    }

    /**
     * 小数をある程度の精度で誤差を無視して比較
     *
     * @param p1
     * @param p2
     * @param p3 小数点以下の桁数
     */
    public static boolean compare(double p1, double p2, int p3) {
        p1 *= pow(10.0D, p3);
        p2 *= pow(10.0D, p3);
        long i0 = Math.round(p1);//四捨五入
        long i1 = Math.round(p2);
        return i0 == i1;
    }

    /**
     * JavaScriptからの利用向け("sin"だとJava8では動かない)
     */
    public static float getSin(float par1) {
        return MathHelper.sin(par1);
    }

    /**
     * JavaScriptからの利用向け("cos"だとJava8では動かない)
     */
    public static float getCos(float par1) {
        return MathHelper.cos(par1);
    }

    /**
     * 単位:度
     */
    public static float sin(float par1) {
        return MathHelper.sin(toRadians(par1));
    }

    /**
     * 単位:度
     */
    public static float cos(float par1) {
        return MathHelper.cos(toRadians(par1));
    }

    public static Random getRandom() {
        return RANDOM;
    }

    public static float wrapAngle(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public static double getDistanceSq(double sX, double sY, double eX, double eY) {
        double dx = eX - sX;
        double dy = eY - sY;
        return dx * dx + dy * dy;
    }

    public static double firstSqrt(double value) {
        return sqrtX(value, value, 4);
    }

    private static double sqrtX(double value, double x0, int n) {
        if (n > 0) {
            value = (value + x0 / value) * 0.5D;
            return sqrtX(value, x0, --n);
        }
        return value;
    }

    public static double sigmoid(double x, double c) {
        if (x <= 0.0D)
            return 0.0D;
        if (x >= 1.0D)
            return 1.0D;
        double f0 = (x - 0.5D) * c;
        double f1 = f0 / Math.sqrt(1.0D + f0 * f0);
        return (f1 + 1.0D) * 0.5D;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public static byte clamp(byte value, byte min, byte max) {
        return (byte) NGTMath.clamp((int) value, min, max);
    }
}