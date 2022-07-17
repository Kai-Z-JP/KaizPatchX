package jp.ngt.rtm.rail.util;

import jp.ngt.rtm.rail.TileEntityLargeRailSlopeBase;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class RailMapSlope extends RailMapBasic {
    private final float dirDeg;
    private final byte slopeType;

    public RailMapSlope(RailPosition par1, RailPosition par2, byte type) {
        super(par1, par2);
        this.dirDeg = MathHelper.wrapAngleTo180_float((float) par1.direction * 45.0F);
        this.slopeType = type;
    }

    @Override
    protected void createLine() {
    }

    @Override
    protected void createRailList(RailProperty prop) {
        int y = this.startRP.blockY;
        int lng = (this.slopeType == 0 ? 16 : (this.slopeType == 1 ? 8 : (this.slopeType == 2 ? 4 : 2)));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < lng; ++j) {
                switch (this.startRP.direction) {
                    case 0:
                        this.rails.add(new int[]{this.startRP.blockX - 1 + i, y, this.startRP.blockZ + j});
                        break;
                    case 2:
                        this.rails.add(new int[]{this.startRP.blockX + j, y, this.startRP.blockZ - 1 + i});
                        break;
                    case 4:
                        this.rails.add(new int[]{this.startRP.blockX - 1 + i, y, this.startRP.blockZ - j});
                        break;
                    case 6:
                        this.rails.add(new int[]{this.startRP.blockX - j, y, this.startRP.blockZ - 1 + i});
                        break;
                }
            }
        }
    }

    @Override
    public void setRail(World world, Block block, int x0, int y0, int z0, RailProperty prop) {
        this.rails.forEach(rail -> {
            int x = rail[0];
            int y = rail[1];
            int z = rail[2];
            world.setBlock(x, y, z, block, this.getHeight(x, z), 2);
            TileEntityLargeRailSlopeBase tile = (TileEntityLargeRailSlopeBase) world.getTileEntity(x, y, z);
            tile.setStartPoint(x0, y0, z0);
        });
        this.rails.clear();
    }

    private int getHeight(int x, int z) {
        int ix1 = Math.abs(x - this.startRP.blockX);
        int iz1 = Math.abs(z - this.startRP.blockZ);
        int ix2 = (this.startRP.direction == 2 || this.startRP.direction == 6) ? 1 : 0;
        int iz2 = (this.startRP.direction == 0 || this.startRP.direction == 4) ? 1 : 0;
        int i = this.slopeType == 0 ? 1 : (this.slopeType == 1 ? 2 : (this.slopeType == 2 ? 4 : 8));
        return ((ix1 * ix2 + iz1 * iz2) & 15) * i;
    }

    public byte getSlopeType() {
        return this.slopeType;
    }

    @Override
    public int getNearlestPoint(int par1, double par2, double par3) {
        int i = 0;
        double pd = 32.0D;

        for (int j = 0; j < par1; ++j) {
            double[] point = this.getRailPos(par1, j);
            double dx = par2 - point[1];
            double dy = par3 - point[0];
            double distance = (dx * dx) + (dy * dy);
            if (pd > distance) {
                pd = distance;
                i = j;
            }
        }

        return pd < 32.0D ? i : -1;
    }

    @Override
    public double[] getRailPos(int par1, int par2) {
        double t = (double) par2 / (double) par1;
        t = t < 0 ? 0.0D : (t > 1 ? 1.0D : t);
        double tp = 1 - t;
        double x = tp * this.startRP.posZ + t * this.endRP.posZ;
        double y = tp * this.startRP.posX + t * this.endRP.posX;
        if (this.startRP.direction == 2 || this.startRP.direction == 6) {
            return new double[]{(double) this.startRP.blockZ + 0.5D, y};
        } else {
            return new double[]{x, (double) this.startRP.blockX + 0.5D};
        }
    }

    @Override
    public double getRailHeight(int par1, int par2) {
        return (double) this.startRP.blockY + ((double) par2 / (double) par1) + 0.0625D;
    }

    @Override
    public float getRailYaw(int par1, int par2) {
        return this.dirDeg;
    }

    @Override
    public float getRailPitch(int par1, int par2) {
        switch (this.slopeType) {
            case 1:
                return 7.125016F;
            case 2:
                return 14.03624F;
            case 3:
                return 26.56505F;
            case 0:
            default:
                return 3.576334F;
        }
    }

    @Override
    public double getLength() {
        switch (this.slopeType) {
            case 1:
                return 8.0D;
            case 2:
                return 4.0D;
            case 3:
                return 2.0D;
            case 0:
            default:
                return 16.0D;
        }
    }
}