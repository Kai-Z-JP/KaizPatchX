package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.block.OrnamentType;

public class TileEntityScaffoldStairs extends TileEntityScaffold {
    @Override
    public OrnamentType getOrnamentType() {
        return OrnamentType.Stair;
    }

    @Override
    protected String getDefaultName() {
        return "ScaffoldStair01";
    }
}