package jp.ngt.rtm.rail.util;

import net.minecraft.world.World;

import java.util.stream.IntStream;

public class RailMapSwitch extends RailMap {
    public final RailDir startDir, endDir;
    private final int[] count = new int[2];
    private boolean isOpen;
    public static final int MAX_COUNT = 80;

    public RailMapSwitch(RailPosition par1, RailPosition par2, RailDir sDir, RailDir eDir) {
        super(par1, par2);
        this.startDir = sDir;
        this.endDir = eDir;
    }

    @Deprecated
    //TileEntity.updateEntity()のタイミングで呼ばれる
    public void onUpdate(World world) {
        if (world.isRemote) {
            IntStream.range(0, 2).filter(i -> (i != 0 || this.startDir != RailDir.NONE) && (i != 1 || this.endDir != RailDir.NONE)).forEach(i -> {
                if (this.isOpen) {
                    if (this.count[i] > 0) {
                        --this.count[i];
                    }
                } else {
                    if (this.count[i] < MAX_COUNT) {
                        ++this.count[i];
                    }
                }
            });
        }
    }

    public RailMapSwitch setState(boolean par1) {
        this.isOpen = par1;
        return this;
    }

    public float getStartMovement() {
        return (float) this.count[0] / (float) MAX_COUNT;
    }

    public float getEndMovement() {
        return (float) this.count[1] / (float) MAX_COUNT;
    }

    public boolean shouldRenderRSide() {
        return (this.startDir != RailDir.RIGHT) && (this.endDir != RailDir.RIGHT);
    }

    public boolean shouldRenderLSide() {
        return (this.startDir != RailDir.LEFT) && (this.endDir != RailDir.LEFT);
    }
}