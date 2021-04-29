package jp.ngt.rtm.modelpack.cfg;


public class ConnectorConfig extends ModelConfig implements IConfigWithType {
    public static final String TYPE = "ModelConnector";

    private String name;
    public ModelSource model;
    /**
     * Relay, Input, Output
     */
    public String connectorType;
    /**
     * {x, y, z}
     */
    public float[] wirePos;

    @Override
    public void init() {
        super.init();

        if (this.wirePos == null) {
            this.wirePos = new float[]{0.0F, 0.0F, 0.0F};
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModelType() {
        return TYPE;
    }

    @Override
    public String getSubType() {
        return this.connectorType;
    }

    public static ConnectorConfig getDummy() {
        ConnectorConfig cfg = new ConnectorConfig();
        cfg.name = "dummy";
        cfg.connectorType = "N";
        cfg.init();
        return cfg;
    }
}