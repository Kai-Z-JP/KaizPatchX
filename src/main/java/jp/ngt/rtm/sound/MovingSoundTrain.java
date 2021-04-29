package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundTrain extends MovingSoundVehicle {
    public MovingSoundTrain(EntityTrainBase train, ResourceLocation sound, boolean par3, boolean par4) {
        super(train, sound, par3, par4);
    }

    @Override
    public void update() {
        super.update();

        if (this.changePitch) {
            EntityTrainBase train = (EntityTrainBase) this.entity;
            TrainConfig cfg = train.getModelSet().getConfig();
            float f0 = cfg.maxSpeed[0];
            this.field_147663_c = (Math.abs(train.getSpeed()) - f0) / (cfg.maxSpeed[cfg.maxSpeed.length - 1] - f0) + 1.0F;
        }
    }
}