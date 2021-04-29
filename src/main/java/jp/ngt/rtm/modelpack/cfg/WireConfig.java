package jp.ngt.rtm.modelpack.cfg;


public class WireConfig extends ModelConfig {
    public static final String TYPE = "ModelWire";

    private String name;
    public ModelSource model;
    /**
     * たわみ係数
     */
    public float deflectionCoefficient;
    /**
     * 長さ係数
     */
    public float lengthCoefficient;
    public float sectionLength;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModelType() {
        return TYPE;
    }

    public static WireConfig getDummy() {
        WireConfig cfg = new WireConfig();
        cfg.name = "dummy";
        cfg.init();
        return cfg;
    }
}