package jp.ngt.rtm.render;

public enum RenderPass {
    NORMAL(0),
    TRANSPARENT(1),
    LIGHT(2),
    LIGHT_FRONT(3),
    LIGHT_BACK(4),
    OUTLINE(253),
    GUI(254),
    PICK(255);

    public final int id;

    RenderPass(int par1) {
        this.id = par1;
    }
}
