package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.modelpack.modelset.ModelSetRail;
import jp.ngt.rtm.rail.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class RailMap {
    protected final List<int[]> rails = new ArrayList<>();

    public boolean equals(Object obj) {
        if (obj instanceof RailMap) {
            RailMap rm = (RailMap) obj;
            return getStartRP().equals(rm.getStartRP());
        }
        return false;
    }

    public abstract RailPosition getStartRP();

    public abstract RailPosition getEndRP();

    public abstract double getLength();

    public abstract int getNearlestPoint(int paramInt, double paramDouble1, double paramDouble2);

    public abstract double[] getRailPos(int paramInt1, int paramInt2);

    public abstract double getRailHeight(int paramInt1, int paramInt2);

    public abstract float getRailYaw(int paramInt1, int paramInt2);

    @Deprecated
    public final float getRailRotation(int split, int index) {
        return getRailYaw(split, index);
    }

    public abstract float getRailPitch(int paramInt1, int paramInt2);

    public abstract float getRailRoll(int paramInt1, int paramInt2);

    @Deprecated
    public final float getCant(int split, int index) {
        return getRailRoll(split, index);
    }


    /**
     * RailMapの端同士が繋げられるかどうか(=連続した曲線になるか)<br>
     * 同一RailMapの場合はtrue
     *
     * @param railMap null可
     */
    public boolean canConnect(RailMap railMap) {
        if (railMap == null) {
            return false;
        }
        if (equals(railMap)) {
            return true;
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                double[] p0 = getRailPos(10, i * 10);
                double[] p1 = railMap.getRailPos(10, j * 10);
                if (NGTMath.compare(p0[0], p1[0], 5) && NGTMath.compare(p0[1], p1[1], 5)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 道床ブロックのリストを作成<br>
     * レールの生成時と破壊時に呼ばれる
     */
    protected void createRailList(RailProperty prop) {
        ModelSetRail modelSet = prop.getModelSet();
        int width = modelSet.getConfig().ballastWidth >> 1;

        this.rails.clear();
        int split = (int) (this.getLength() * 4.0D);
        double halfPi = Math.PI * 0.5D;
        for (int j = 0; j < split; j++) {
            double[] point = this.getRailPos(split, j);
            double x = point[1];
            double z = point[0];
            double slope = Math.toRadians(this.getRailYaw(split, j));
            double height = this.getRailHeight(split, j);
            int y = (int) height;
            int x0 = MathHelper.floor_double(x);
            int z0 = MathHelper.floor_double(z);

            double sinSlopePlus = Math.sin(slope + halfPi);
            double cosSlopePlus = Math.cos(slope + halfPi);
            double sinSlopeMinus = Math.sin(slope - halfPi);
            double cosSlopeMinus = Math.cos(slope - halfPi);

            for (int i = 1; i <= width; i++) {
                int x1 = MathHelper.floor_double(x + sinSlopePlus * i);
                int z1 = MathHelper.floor_double(z + cosSlopePlus * i);
                int x2 = MathHelper.floor_double(x + sinSlopeMinus * i);
                int z2 = MathHelper.floor_double(z + cosSlopeMinus * i);
                this.addRailBlock(x1, y, z1);
                this.addRailBlock(x2, y, z2);
            }
            this.addRailBlock(x0, y, z0);
        }
    }

    protected void addRailBlock(int x, int y, int z) {
        for (int i = 0; i < this.rails.size(); i++) {
            int[] ia = this.rails.get(i);
            if (ia[0] == x && ia[2] == z) {
                if (ia[1] <= y) {
                    return;
                } else {
                    this.rails.remove(i);
                    --i;
                }
            }
        }
        int[] pos = new int[]{x, y, z};
        if (!Arrays.equals(pos, this.getStartRP().getNeighborPos()) && !Arrays.equals(pos, this.getEndRP().getNeighborPos())) {
            this.rails.add(new int[]{x, y, z});//始点と終点に接する位置にはブロック生成しないように
        }
    }

    /**
     * ブロックの設置
     */
    public void setRail(World world, Block block, int x0, int y0, int z0, RailProperty prop) {
        this.createRailList(prop);
        TileEntity tile = world.getTileEntity(x0, y0, z0);
        if (tile instanceof TileEntityMarker && ((TileEntityMarker) tile).getState(MarkerState.LINE2)) {
            this.setBaseBlock(world, x0, y0, z0);
        }
        this.rails.forEach(rail -> {
            int x = rail[0];
            int y = rail[1];
            int z = rail[2];
            Block block2 = world.getBlock(x, y, z);
            if (!(block2 instanceof BlockLargeRailBase) || block2 == block)//異なる種類のレールを上書きしない
            {
                world.setBlock(x, y, z, block, 0, 2);
                TileEntityLargeRailBase railBase = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
                if (railBase != null) {
                    railBase.setStartPoint(x0, y0, z0);
                }
            }
        });
        this.rails.clear();
    }

    private void setBaseBlock(World world, int x0, int y0, int z0) {
        int split = (int) (this.getLength() * 4.0D);
        RailPosition rp = this.getStartRP();
        int minWidth = MathHelper.floor_float(rp.constLimitWN + 0.5F);
        int maxWidth = MathHelper.floor_float(rp.constLimitWP + 0.5F);
        int minHeight = MathHelper.floor_float(rp.constLimitHN);
        int maxHeight = MathHelper.floor_float(rp.constLimitHP);
        Block[][] blocks = new Block[maxHeight - minHeight + 1][maxWidth - minWidth + 1];
        int[][] metas = new int[maxHeight - minHeight + 1][maxWidth - minWidth + 1];
        for (int k = 0; k < split - 1; k++) {
            double[] point = this.getRailPos(split, k);
            double x = point[1];
            double z = point[0];
            double y = this.getRailHeight(split, k);
            float yaw = MathHelper.wrapAngleTo180_float(this.getRailYaw(split, k));
            for (int i = 0; i < blocks.length; i++) {
                int h = minHeight + i;
                for (int j = 0; j < (blocks[i]).length; j++) {
                    int w = minWidth + j;
                    Vec3 vec = new Vec3(w, h, 0.0D);
                    vec = vec.rotateAroundY(yaw);
                    int[] pos = new int[]{MathHelper.floor_double(x + vec.getX()), MathHelper.floor_double(y + vec.getY()), MathHelper.floor_double(z + vec.getZ())};
                    Block block = world.getBlock(pos[0], pos[1], pos[2]);
                    int meta = world.getBlockMetadata(pos[0], pos[1], pos[2]);
                    if (k == 0) {
                        if (!(block instanceof BlockMarker) && !(block instanceof BlockLargeRailBase)) {
                            blocks[i][j] = block;
                            metas[i][j] = meta;
                        }
                    } else if (blocks[i][j] != null) {
                        if (!(block instanceof BlockLargeRailBase)) {
                            world.setBlock(pos[0], pos[1], pos[2], blocks[i][j], metas[i][j], 3);
                        }
                    }
                }
            }
        }
    }

    /**
     * ブロックの破壊
     */
    public void breakRail(World world, RailProperty prop, TileEntityLargeRailCore core) {
        this.createRailList(prop);
        List<int[]> posList = new ArrayList<>();
        this.rails.forEach(anInt -> {
            int x = anInt[0];
            int y = anInt[1];
            int z = anInt[2];
            TileEntity rail = world.getTileEntity(x, y, z);
            if (rail instanceof TileEntityLargeRailBase) {
                if (rail == core) {
                    return;
                }

                //重なっている他レールを破壊しないように
                //coreが既に破壊さている場合は続行
                TileEntityLargeRailCore core2 = ((TileEntityLargeRailBase) rail).getRailCore();
                if (core2 == null || core2 == core) {
                    posList.add(new int[]{x, y, z});
                    ((List<TileEntity>) world.loadedTileEntityList).remove(rail);

                }
            }
        });
        posList.forEach(pos -> {
            world.setBlockToAir(pos[0], pos[1], pos[2]);
            world.removeTileEntity(pos[0], pos[1], pos[2]);
        });
        world.setBlockToAir(core.xCoord, core.yCoord, core.zCoord);
        world.removeTileEntity(core.xCoord, core.yCoord, core.zCoord);
        ((List<TileEntity>) world.loadedTileEntityList).remove(core);

        this.rails.clear();
    }

    public boolean canPlaceRail(World world, boolean isCreative, RailProperty prop) {
        this.createRailList(prop);
        boolean flag = true;
        for (int[] rail : this.rails) {
            int x = rail[0];
            int y = rail[1];
            int z = rail[2];
            Block block = world.getBlock(x, y, z);
            boolean b0 = world.isAirBlock(x, y, z) || block == RTMBlock.marker || block == RTMBlock.markerSwitch || /*block == RTMBlock.markerSlope ||*/ (block instanceof BlockLargeRailBase && !((BlockLargeRailBase) block).isCore());
            if (!isCreative && !b0) {
                NGTLog.sendChatMessageToAll("message.rail.obstacle", ":" + x + "," + y + "," + z);
                return false;
            }
            flag = b0 && flag;
        }
        return true;
    }

    public List<int[]> getRailBlockList(RailProperty prop) {
        this.createRailList(prop);
        return new ArrayList<>(this.rails);
    }

    public void showRailProp() {
        NGTLog.sendChatMessageToAll(String.format("SP X%5.1f Z%5.1f", (this.getStartRP()).posX, (this.getStartRP()).posZ));
        NGTLog.sendChatMessageToAll(String.format("SA L%5.1f Y%5.1f", (this.getStartRP()).anchorLengthHorizontal, (this.getStartRP()).anchorYaw));
        NGTLog.sendChatMessageToAll(String.format("EP X%5.1f Z%5.1f", (this.getEndRP()).posX, (this.getEndRP()).posZ));
        NGTLog.sendChatMessageToAll(String.format("EA L%5.1f Y%5.1f", (this.getEndRP()).anchorLengthHorizontal, (this.getEndRP()).anchorYaw));
    }
}
