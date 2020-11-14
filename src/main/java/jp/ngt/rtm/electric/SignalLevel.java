package jp.ngt.rtm.electric;

import java.util.Arrays;

public enum SignalLevel {
    /**
     * 高速進行
     */
    HIGH_SPEED_PROCEED(6, 1.2F, 1.5F),
    /**
     * 進行
     */
    PROCEED(5, 0.9F, 1.2F),
	/**
	 * 減速
	 */
	SLOW_DOWN(4, 0.6F, 0.9F),
	/**
	 * 注意
	 */
	CAUTION(3, 0.3F, 0.6F),
	/**
	 * 警戒
	 */
	VIGILANCE(2, 0.3F, 0.6F),
	/**
	 * 停止
	 */
	STOP(1, 0.0F, 0.0F);

	public final int level;
	/**
	 * =時速/72
	 */
	public final float speedLowerLimit, speedUpperLimit;

	SignalLevel(int par1, float par2, float par3) {
		this.level = par1;
		this.speedLowerLimit = par2;
		this.speedUpperLimit = par3;
	}

	public static SignalLevel getSignal(int par1) {
        return Arrays.stream(SignalLevel.values()).filter(signal -> signal.level == par1).findFirst().orElse(STOP);
	}

	/**
	 * @param par1 : 信号の強度
	 * @param par2 : 変更前の速度
	 */
	public static float getSpeed(int par1, float par2) {
		SignalLevel signal = getSignal(par1);
		if (par2 > signal.speedUpperLimit) {
			return signal.speedUpperLimit;
		} else if (par2 < signal.speedLowerLimit) {
			return signal.speedLowerLimit;
		}
		return par2;
	}
}