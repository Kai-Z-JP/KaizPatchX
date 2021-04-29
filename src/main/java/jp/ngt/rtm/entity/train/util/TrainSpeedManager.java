package jp.ngt.rtm.entity.train.util;

import jp.ngt.rtm.modelpack.cfg.TrainConfig;

public final class TrainSpeedManager {
    private static final float[] BRAKE = {-0.0005F, -0.001F, -0.0015F, -0.002F, -0.0025F, -0.003F, -0.0035F, -0.01F};

    public static float getAcceleration(int notch, float prevSpeed, TrainConfig cfg) {
        if (notch == 0) {
            return 0.0F;
        } else if (notch > 0) {
            --notch;
            if (prevSpeed >= cfg.maxSpeed[Math.min(cfg.maxSpeed.length - 1, notch)]) {
                return 0.0F;
            } else {
                return cfg.accelerateions[Math.min(cfg.accelerateions.length - 1, notch)];
            }
        } else {
            notch = -(notch + 1);
            float decceleration = cfg.deccelerations[Math.min(cfg.deccelerations.length - 1, notch + 1)];
            float absSpeed = Math.abs(prevSpeed);
            if (absSpeed + decceleration < 0.0F) {
                return -absSpeed;
            }
            return decceleration;
        }
    }

    public static float getMaxSpeed(TrainConfig cfg) {
        return cfg.maxSpeed[cfg.maxSpeed.length - 1];
    }
}