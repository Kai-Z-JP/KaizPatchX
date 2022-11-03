package jp.ngt.rtm.rail.util;

import jp.kaiz.kaizpatch.fixrtm.rtm.rail.util.SwitchTypeSingleCrossFixRTMV1;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RailMaker {
    // see RailMapBasic.fixRTMRailMapVersion
    public final int fixRTMRailMapVersion;
    private final World worldObj;
    private final List<RailPosition> rpList;

    /**
     * @deprecated use {@link #RailMaker(World, List, int)}
     */
    public RailMaker(World world, List<RailPosition> par2) {
        this(world, par2, 0);
    }

    public RailMaker(World world, List<RailPosition> par2, int fixRTMRailMapVersion) {
        this.worldObj = world;
        this.rpList = par2;
        this.fixRTMRailMapVersion = fixRTMRailMapVersion;
    }

    public RailMaker(World world, RailPosition[] par2) {
        this(world, par2, 0);
    }

    public RailMaker(World world, RailPosition[] par2, int fixRTMRailMapVersion) {
        this(world, new ArrayList<>(Arrays.asList(par2)), fixRTMRailMapVersion);
    }


    private SwitchType getSwitchType() {
        if (this.rpList.size() == 3) {
            int i0 = this.rpList.stream().mapToInt(rp -> (rp.switchType == 1) ? 1 : 0).sum();

            if (i0 == 1) {
                return new SwitchType.SwitchBasic();
            }
        } else if (this.rpList.size() == 4) {
            int i0 = this.rpList.stream().mapToInt(rp -> (rp.switchType == 1) ? 1 : 0).sum();

            if (i0 == 2) {
                if (fixRTMRailMapVersion >= 1) {
                    return new SwitchTypeSingleCrossFixRTMV1();
                } else {
                    return new SwitchType.SwitchSingleCross();
                }
            } else if (i0 == 4) {
                for (int i = 0; i < this.rpList.size(); ++i) {
                    for (int j = i + 1; j < this.rpList.size(); ++j)//全組み合わせ(重複なし)
                    {
                        if (this.rpList.get(i).direction == this.rpList.get(j).direction) {
                            return new SwitchType.SwitchScissorsCross();
                        }
                    }
                }
                return new SwitchType.SwitchDiamondCross();
            }
        }

        return null;
    }

    public SwitchType getSwitch() {
        SwitchType type = this.getSwitchType();
        if (type != null) {
            List<RailPosition> switchList = new ArrayList<>();//分岐あり
            List<RailPosition> normalList = new ArrayList<>();//分岐なし
            this.rpList.forEach(rp -> (rp.switchType == 1 ? switchList : normalList).add(rp));

            if (type.init(switchList, normalList)) {
                return type;
            }
        }

        return null;
    }
}