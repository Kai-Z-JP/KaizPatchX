package jp.ngt.rtm.rail.util;

public class RailMapSwitch extends RailMapBasic {
    public final RailDir startDir, endDir;
    private boolean isOpen;

    public RailMapSwitch(RailPosition par1, RailPosition par2, RailDir sDir, RailDir eDir) {
        super(par1, par2);
        this.startDir = sDir;
        this.endDir = eDir;
    }

    public RailMapSwitch setState(boolean par1) {
        this.isOpen = par1;
        return this;
    }
}