package jp.ngt.rtm.rail;

import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.entity.Entity;

public interface ILargeRail {
    RailMap getRailMap(Entity entity);
}