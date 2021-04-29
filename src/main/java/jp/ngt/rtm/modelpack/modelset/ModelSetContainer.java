package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.ContainerConfig;

public class ModelSetContainer extends ModelSetBase<ContainerConfig> {
    public ModelSetContainer() {
        super();
    }

    public ModelSetContainer(ContainerConfig par1) {
        super(par1);
    }

    @Override
    public ContainerConfig getDummyConfig() {
        return ContainerConfig.getDummy();
    }
}