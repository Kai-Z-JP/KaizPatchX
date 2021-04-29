package jp.ngt.rtm.render;

import jp.ngt.ngtlib.renderer.model.MCModel;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient;
import net.minecraft.entity.Entity;

public class MCModelRenderer extends VehiclePartsRenderer {
    private MCModel model;
    private boolean light;
    private boolean alphaBlend;

    public MCModelRenderer(String... par1) {
        super(par1);
    }

    @Override
    public void init(ModelSetVehicleBaseClient par1, ModelObject par2) {
        this.model = (MCModel) par2.model;
        this.light = par2.light;
        this.alphaBlend = par2.alphaBlend;
    }

    @Override
    public void render(Entity entity, int pass, float par3) {
        if ((!this.light && pass >= 2) || (!this.alphaBlend && pass == 1)) {
            return;
        }

        this.model.renderAll(false);
    }
}