package jp.ngt.ngtlib.util;

public final class ColorUtil {
	public static int getR(int color) {
		return (color >> 16) & 0xFF;
	}

	public static int getG(int color) {
		return (color >> 8) & 0xFF;
	}

	public static int getB(int color) {
		return (color) & 0xFF;
	}

	public static int encode(int r, int g, int b) {
		return (r << 16) | (g << 8) | b;
	}

	public static String toString(int color) {
		String s = Integer.toHexString(color);
		if (color <= 0xFF) {
			s = "0000" + s;
		} else if (color <= 0xFFFF) {
			s = "00" + s;
		}
		return s;
	}

	public static int toInteger(String color) {
		if (!color.startsWith("0x")) {
			color = "0x" + color;
		}
		return Integer.decode(color);
	}

	/**
	 * 乗算
	 */
	public static int multiplicating(int src, int dst) {
		int r = multiplicating2(getR(src), getR(dst));
		int g = multiplicating2(getG(src), getG(dst));
		int b = multiplicating2(getB(src), getB(dst));
		return encode(r, g, b);
	}

	private static int multiplicating2(int src, int dst) {
		return (src * dst) / 0xFF;
	}

	/**
	 * @return {color, alpha}
	 */
	public static int[] alphaBlending(int srcColor, int srcAlpha, int dstColor, int dstAlpha) {
		if (srcAlpha == 0xFF || dstAlpha == 0) {
			return new int[]{srcColor, srcAlpha};
		}
		if (srcAlpha == 0) {
			return new int[]{dstColor, dstAlpha};
		}

		int dtms = dstAlpha * (0xFF - srcAlpha);
		int alpha = correct(srcAlpha + (dtms / 0xFF));
		//前景
		int srcR = getR(srcColor);
		int srcG = getG(srcColor);
		int srcB = getB(srcColor);
		//背景
		int dstR = getR(dstColor);
		int dstG = getG(dstColor);
		int dstB = getB(dstColor);
		int r = correct((srcR * srcAlpha + ((dstR * dtms) / 0xFF)) / alpha);
		int g = correct((srcG * srcAlpha + ((dstG * dtms) / 0xFF)) / alpha);
		int b = correct((srcB * srcAlpha + ((dstB * dtms) / 0xFF)) / alpha);
		return new int[]{encode(r, g, b), alpha};
	}

	private static int correct(int p) {
		return p > 0xFF ? 0xFF : p;
	}
}