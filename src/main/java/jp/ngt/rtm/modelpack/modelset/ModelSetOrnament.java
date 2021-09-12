package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.OrnamentConfig;

public class ModelSetOrnament extends ModelSetBase<OrnamentConfig> {
    public ModelSetOrnament() {
        super();
    }

    public ModelSetOrnament(OrnamentConfig par1) {
        super(par1);
    }

    @Override
    public OrnamentConfig getDummyConfig() {
        return OrnamentConfig.getDummy();
    }
}