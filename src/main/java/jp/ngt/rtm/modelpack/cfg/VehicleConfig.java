package jp.ngt.rtm.modelpack.cfg;

public class VehicleConfig extends VehicleBaseConfig implements IConfigWithType {
    public static final String TYPE = "ModelVehicle";

    /**
     * 名前(重複不可)
     */
    private String name;
    /**
     * モデル
     */
    public ModelSource model;
    /**
     * Car, Plane, Ship
     */
    public String vehicleType;

    //以下浮動小数の配列{OnGround, InAir}
    /**
     * 滑りやすさ
     */
    private float[] friction;
    /**
     * 加速度
     */
    private float[] acceleration;
    /**
     * 最大速度
     */
    private float[] maxSpeed;
    /**
     * 最大Y軸回転
     */
    private float[] maxYaw;
    /**
     * 係数
     */
    //private float[] speedCoefficient;
    private float[] yawCoefficient;
    private float[] pitchCoefficient;
    private float[] rollCoefficient;
    public boolean changeYawOnStopping;

    @Override
    public void init() {
        super.init();

        if (this.playerPos == null || this.playerPos.length != 1) {
            this.playerPos = new float[][]{{0.0F, 0.0F, 0.0F}};
        }
    }

    @Override
    public ModelSource getModel() {
        return this.model;
    }

    public float getFriction(boolean onGround) {
        if (this.friction == null) {
            return 0.9F;
        }
        return this.friction[onGround ? 0 : 1];
    }

    public float getAcceleration(boolean onGround) {
        if (this.acceleration == null) {
            return 0.0125F;
        }
        return this.acceleration[onGround ? 0 : 1];
    }

    public float getMaxSpeed(boolean onGround) {
        if (this.maxSpeed == null) {
            return 0.8F;
        }
        return this.maxSpeed[onGround ? 0 : 1];
    }

    public float getMaxYaw(boolean onGround) {
        if (this.maxYaw == null) {
            return 15.0F;
        }
        return this.maxYaw[onGround ? 0 : 1];
    }

	/*public float getSpeedCoefficient(boolean onGround)
	{
		if(this.speedCoefficient == null){return 5.0F;}
		return this.speedCoefficient[onGround ? 0 : 1];
	}*/

    public float getYawCoefficient(boolean onGround) {
        if (this.yawCoefficient == null) {
            return 4.5F;
        }
        return this.yawCoefficient[onGround ? 0 : 1];
    }

    public float getPitchCoefficient(boolean onGround) {
        if (this.pitchCoefficient == null) {
            return 2.5F;
        }
        return this.pitchCoefficient[onGround ? 0 : 1];
    }

    public float getRollCoefficient(boolean onGround) {
        if (this.rollCoefficient == null) {
            return 45.0F;
        }
        return this.rollCoefficient[onGround ? 0 : 1];
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModelType() {
        return TYPE;
    }

    public static VehicleConfig getDummy() {
        VehicleConfig cfg = new VehicleConfig();
        cfg.name = "dummy";
        cfg.vehicleType = "N";
        cfg.init();
        return cfg;
    }

    @Override
    public String getSubType() {
        return this.vehicleType;
    }
}