package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.util.ResourceLocation;

public class ModelSetTrain extends ModelSetVehicleBase<TrainConfig> {
    public final String sound_brakeRelease_s;
    public final String sound_brakeRelease_w;

    public ModelSetTrain() {
        super();
        this.sound_brakeRelease_s = null;
        this.sound_brakeRelease_w = null;

    }

    public ModelSetTrain(TrainConfig par1) {
        super(par1);
        ResourceLocation s0 = this.getSoundResource(par1.sound_BrakeRelease);
        this.sound_brakeRelease_s = s0 == null ? null : s0.getResourceDomain() + ":" + s0.getResourcePath();
        ResourceLocation s1 = this.getSoundResource(par1.sound_BrakeRelease2);
        this.sound_brakeRelease_w = s1 == null ? null : s1.getResourceDomain() + ":" + s1.getResourcePath();
    }

    @Override
    public TrainConfig getDummyConfig() {
        return TrainConfig.getDummyConfig();
    }
}