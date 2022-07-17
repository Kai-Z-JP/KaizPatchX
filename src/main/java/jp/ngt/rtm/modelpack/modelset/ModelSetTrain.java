package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.TrainConfig;

public class ModelSetTrain extends ModelSetVehicleBase<TrainConfig> {

    public ModelSetTrain() {
        super();

    }

    public ModelSetTrain(TrainConfig par1) {
        super(par1);
    }

    @Override
    public TrainConfig getDummyConfig() {
        return TrainConfig.getDummyConfig();
    }
}