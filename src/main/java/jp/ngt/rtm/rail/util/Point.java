package jp.ngt.rtm.rail.util;

import net.minecraft.world.World;

/**
 * 分岐レールの各構成点
 */
public class Point {
    /**
     * 分岐切替速度(tick)
     */
    private static final int MAX_COUNT = 4 * 20;

    /**
     * 分岐の根本
     */
    public final RailPosition rpRoot;
    /**
     * 本線
     */
    public final RailMapSwitch rmMain;
    /**
     * 支線
     */
    public final RailMapSwitch rmBranch;
    /**
     * rmMainに対するrmBranchの分岐方向, ->DIR_C,DIR_L,DIR_R
     */
    public final RailDir branchDir;
    /**
     * rpRootがRailMapの始点かどうか
     */
    public final boolean mainDirIsPositive, branchDirIsPositive;

    /**
     * 動き具合(本線視点)
     */
    private int moveCount;

    /**
     * 分岐あり点
     */
    public Point(RailPosition railPos, RailMapSwitch rms1, RailMapSwitch rms2) {
        this.rpRoot = railPos;
        boolean b0 = rms1.getLength() <= rms2.getLength();//短い方を本線、長い方を支線に
        this.rmMain = b0 ? rms1 : rms2;
        this.rmBranch = b0 ? rms2 : rms1;
        this.branchDir = this.getDir(this.rpRoot, this.rmMain, this.rmBranch);

        this.mainDirIsPositive = (this.rmMain.getStartRP() == this.rpRoot);
        this.branchDirIsPositive = (this.rmBranch.getStartRP() == this.rpRoot);
    }

    /**
     * 分岐なし点
     */
    public Point(RailPosition railPos, RailMapSwitch rms1) {
        this.rpRoot = railPos;
        this.rmMain = rms1;
        this.rmBranch = null;
        this.branchDir = RailDir.NONE;

        this.mainDirIsPositive = (rms1.getStartRP() == railPos);
        this.branchDirIsPositive = false;
    }

    /**
     * rms1に対するrms2の向き
     */
    private RailDir getDir(RailPosition rpr, RailMapSwitch rms1, RailMapSwitch rms2) {
        RailPosition rp1 = (rms1.startRP == rpr) ? rms1.endRP : rms1.startRP;
        RailPosition rp2 = (rms2.startRP == rpr) ? rms2.endRP : rms2.startRP;
        return rpr.getDir(rp1, rp2);
    }

    /**
     * TileEntity.updateEntity()のタイミングで呼ばれる
     */
    public void onUpdate(World world) {
        boolean hasRSInput = this.rpRoot.checkRSInput(world);

        if (hasRSInput) {
            if (this.moveCount < MAX_COUNT) {
                ++this.moveCount;
            }
        } else {
            if (this.moveCount > 0) {
                --this.moveCount;
            }
        }
    }

    public float getMovement() {
        return (float) this.moveCount / (float) MAX_COUNT;
    }

    public RailMap getActiveRailMap(World world) {
        if (this.branchDir == RailDir.NONE) {
            return this.rmMain;
        } else {
            boolean hasRSInput = this.rpRoot.checkRSInput(world);
            return hasRSInput ? this.rmBranch : this.rmMain;
        }
    }
}