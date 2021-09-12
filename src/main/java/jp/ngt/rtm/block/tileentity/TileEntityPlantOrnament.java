package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.block.OrnamentType;

public class TileEntityPlantOrnament extends TileEntityOrnament {
    @Override
    public OrnamentType getOrnamentType() {
        return OrnamentType.Plant;
    }

    @Override
    protected String getDefaultName() {
        return "Tree01";
    }
}
