package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.StraightLine;
import jp.ngt.ngtlib.math.Vec3;

public final class RailMapTurntable extends RailMapBasic {
    public final int centerX, centerY, centerZ, radius;
    private float rotation;

    /**
     * @deprecated use {@link #RailMapTurntable(RailPosition, RailPosition, int, int, int, int, int)}
     */
    public RailMapTurntable(RailPosition par1, RailPosition par2, int x, int y, int z, int r) {
        this(par1, par2, x, y, z, r, 0);
    }

    public RailMapTurntable(RailPosition par1, RailPosition par2, int x, int y, int z, int r, int version) {
        super(par1, par2, version);
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.radius = r;
    }

    @Override
    protected void createLine() {
        double x0 = this.startRP.posX;
        double y0 = this.startRP.posY;
        double z0 = this.startRP.posZ;
        double x1 = this.endRP.posX;
        double y1 = this.endRP.posY;
        double z1 = this.endRP.posZ;

        this.lineHorizontal = new StraightLine(z0, x0, z1, x1);
        this.lineVertical = new StraightLine(0.0D, y0, this.radius * 2 + 1, y1);
    }

    private void recreateLine() {
        double cx = this.centerX + 0.5D;
        double cz = this.centerZ + 0.5D;
        double sx = this.startRP.posX - cx;
        double sz = this.startRP.posZ - cz;
        double ex = this.endRP.posX - cx;
        double ez = this.endRP.posZ - cz;
        double gain = 0.5D;
        //見た目より若干長めにすることで斜め時に隣接RMと隙間が空くの回避
        if (this.startRP.blockX == this.endRP.blockX) {
            sz += (sz > ez) ? gain : -gain;
            ez += (ez > sz) ? gain : -gain;
        } else {
            sx += (sx > ex) ? gain : -gain;
            ex += (ex > sx) ? gain : -gain;
        }
        Vec3 vStart = PooledVec3.create(sx, 0.0D, sz);
        Vec3 vEnd = PooledVec3.create(ex, 0.0D, ez);
        vStart = vStart.rotateAroundY(this.rotation);
        vEnd = vEnd.rotateAroundY(this.rotation);
        this.lineHorizontal = new StraightLine(vStart.getZ() + cz, vStart.getX() + cx, vEnd.getZ() + cz, vEnd.getX() + cx);
    }

    @Override
    protected void createRailList(RailProperty prop) {
        this.rails.clear();
        for (int i = -this.radius; i < this.radius + 1; ++i) {
            for (int j = -this.radius; j < this.radius + 1; ++j) {
                double radSq = i * i + j * j;
                //端の接続辺を幅3以上にするため
                if (radSq <= (this.radius + 0.4999F) * (this.radius + 0.4999F)) {
                    this.addRailBlock(this.centerX + i, this.centerY, this.centerZ + j);
                }
            }
        }
    }

    @Override
    public int getNearlestPoint(int par1, double par2, double par3) {
        this.recreateLine();
        return super.getNearlestPoint(par1, par2, par3);
    }

    @Override
    public double[] getRailPos(int par1, int par2) {
        this.recreateLine();
        return super.getRailPos(par1, par2);
    }

    @Override
    public float getRailYaw(int par1, int par2) {
        this.recreateLine();
        return super.getRailYaw(par1, par2);
    }

    @Override
    public double getLength() {
        return this.radius * 2 + 1;
    }

    @Override
    public boolean canConnect(RailMap railMap) {
        this.recreateLine();
        return super.canConnect(railMap);
    }

    public void setRotation(float par1) {
        this.rotation = par1;
    }
}