package jp.ngt.rtm.modelpack.cfg;

public class NPCConfig extends ModelConfig {
    public static final String TYPE = "ModelNPC";

    private String name;
    public ModelSource model;
    public String texture;
    public String lightTexture;
    /**
     * 役割
     */
    public String role;

    public float health;

    public float speed;

    public float damage;

    @Override
    public void init() {
        super.init();
        if (this.health <= 0.0F) {
            this.health = 40.0F;
        }
        if (this.speed <= 0.0F) {
            this.speed = 0.45F;
        }
        if (this.damage <= 0.0F) {
            this.damage = 1.0F;
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

    public static NPCConfig getDummy() {
        NPCConfig cfg = new NPCConfig();
        cfg.name = "dummy";
        return cfg;
    }
}