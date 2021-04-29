package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.WireConfig;

public class ModelSetWire extends ModelSetBase<WireConfig> {
    public ModelSetWire() {
        super();
    }

    public ModelSetWire(WireConfig cfg) {
        super(cfg);
    }

    @Override
    public WireConfig getDummyConfig() {
        return WireConfig.getDummy();
    }
}