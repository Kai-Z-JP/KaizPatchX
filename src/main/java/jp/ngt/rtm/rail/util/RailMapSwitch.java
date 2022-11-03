package jp.ngt.rtm.rail.util;

public class RailMapSwitch extends RailMapBasic {
    public final RailDir startDir, endDir;
    private boolean isOpen;

    /**
     * @deprecated use {@link #RailMapSwitch(RailPosition, RailPosition, RailDir, RailDir, int)}
     */
    public RailMapSwitch(RailPosition par1, RailPosition par2, RailDir sDir, RailDir eDir) {
        this(par1, par2, sDir, eDir, 0);
    }

    public RailMapSwitch(RailPosition par1, RailPosition par2, RailDir sDir, RailDir eDir, int version) {
        super(par1, par2, version);
        this.startDir = sDir;
        this.endDir = eDir;
    }

    public RailMapSwitch setState(boolean par1) {
        this.isOpen = par1;
        return this;
    }
}