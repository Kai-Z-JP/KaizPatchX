package jp.ngt.rtm.rail;

import jp.ngt.rtm.rail.util.RailMap;

public class TileEntityLargeRailNormalCore extends TileEntityLargeRailCore {
    @Override
    public String getRailShapeName() {
        RailMap map = this.getRailMap(null);
        return "Type:Normal, " +
                "X:" + (map.getEndRP().blockX - map.getStartRP().blockX) + ", " +
                "Y:" + (map.getEndRP().blockY - map.getStartRP().blockY) + ", " +
                "Z:" + (map.getEndRP().blockZ - map.getStartRP().blockZ);
    }
}