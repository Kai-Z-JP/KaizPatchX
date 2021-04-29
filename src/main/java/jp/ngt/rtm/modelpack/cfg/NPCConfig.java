package jp.ngt.rtm.modelpack.cfg;

public class NPCConfig extends ModelConfig {
    public static final String TYPE = "ModelNPC";

    private String name;
    public String texture;
    public String lightTexture;
    /**
     * 役割
     */
    public String role;

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

    public static NPCConfig getDummy() {
        NPCConfig cfg = new NPCConfig();
        cfg.name = "dummy";
        return cfg;
    }
}