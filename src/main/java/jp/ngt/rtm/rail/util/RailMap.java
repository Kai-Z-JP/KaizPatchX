package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.BezierCurve;
import jp.ngt.ngtlib.math.ILine;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.StraightLine;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.modelpack.modelset.ModelSetRail;
import jp.ngt.rtm.rail.BlockLargeRailBase;
import jp.ngt.rtm.rail.BlockMarker;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class RailMap {
    /**
     * 水平方向の線形
     */
    protected ILine lineHorizontal;
    /**
     * 垂直方向の線形, 高さとPitch取得用のためXZは相対的な長さで計算
     */
    protected ILine lineVertical;

    protected RailPosition startRP;
    protected RailPosition endRP;

    protected final List<int[]> rails = new ArrayList<>();
    protected double length;

    public RailMap(RailPosition par1, RailPosition par2) {
        this.startRP = par1;
        this.endRP = par2;
        if (this.startRP.cantEdge * this.startRP.cantCenter < 0) {
            this.startRP.cantCenter = -this.startRP.cantCenter;
        } else if (this.startRP.cantEdge * this.startRP.cantCenter == 0 && this.endRP.cantEdge * this.startRP.cantCenter > 0) {
            this.startRP.cantCenter = -this.startRP.cantCenter;
        }
        this.endRP.cantCenter = this.startRP.cantCenter;//中央カント共通化
        this.createLine();
    }

    public void rebuild() {
        this.createLine();
    }

    protected void createLine() {
        double x0 = this.startRP.posX;
        double y0 = this.startRP.posY;
        double z0 = this.startRP.posZ;
        double x1 = this.endRP.posX;
        double y1 = this.endRP.posY;
        double z1 = this.endRP.posZ;

        boolean flag1 = (this.startRP.anchorYaw + this.endRP.anchorYaw) % 180.0F == 0;
//		boolean flag1 = (this.endRP.direction - this.startRP.direction) % 4 == 0;
        boolean flag2 = (z0 == z1 || x0 == x1);//直角
        boolean flag3 = (Math.abs(z0 - z1) == Math.abs(x0 - x1) && this.startRP.direction % 2 != 0 && this.endRP.direction % 2 != 0);//45度
        if (flag1 && (flag2 || flag3)) {
            this.lineHorizontal = new StraightLine(z0, x0, z1, x1);
        } else {
            double ddz = Math.abs(z1 - z0);
            double ddx = Math.abs(x1 - x0);
            double max = Math.max(ddz, ddx);
            double min = Math.min(ddz, ddx);
            if (this.startRP.anchorLengthHorizontal <= 0.0F) {
                boolean b0 = this.startRP.direction % 2 == 0;//true:まっすぐ, false:斜め
                double d1 = b0 ? max : min;
                this.startRP.anchorLengthHorizontal = (float) (d1 * RailPosition.Anchor_Correction_Value);
            }

            if (this.endRP.anchorLengthHorizontal <= 0.0F) {
                boolean b0 = this.endRP.direction % 2 == 0;
                double d1 = b0 ? max : min;
                this.endRP.anchorLengthHorizontal = (float) (d1 * RailPosition.Anchor_Correction_Value);
            }

            double d1 = NGTMath.cos(this.startRP.anchorYaw) * this.startRP.anchorLengthHorizontal;
            double d2 = NGTMath.sin(this.startRP.anchorYaw) * this.startRP.anchorLengthHorizontal;
            double d3 = NGTMath.cos(this.endRP.anchorYaw) * this.endRP.anchorLengthHorizontal;
            double d4 = NGTMath.sin(this.endRP.anchorYaw) * this.endRP.anchorLengthHorizontal;
            this.lineHorizontal = new BezierCurve(z0, x0, z0 + d1, x0 + d2, z1 + d3, x1 + d4, z1, x1);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////

        double lenXZ = Math.sqrt(NGTMath.pow(x1 - x0, 2) + NGTMath.pow(z1 - z0, 2));

        boolean flagV1 = (this.startRP.anchorLengthVertical == 0.0F && this.endRP.anchorLengthVertical == 0.0F);
        //boolean flagV2 = (this.startRP.anchorPitch + this.endRP.anchorPitch == 0.0F);
        if (flagV1) {
            this.lineVertical = new StraightLine(0.0D, y0, lenXZ, y1);
        } else {
            double d1 = NGTMath.cos(this.startRP.anchorPitch) * this.startRP.anchorLengthVertical;
            double d2 = NGTMath.sin(this.startRP.anchorPitch) * this.startRP.anchorLengthVertical;
            double d3 = NGTMath.cos(this.endRP.anchorPitch) * this.endRP.anchorLengthVertical;
            double d4 = NGTMath.sin(this.endRP.anchorPitch) * this.endRP.anchorLengthVertical;
            //"lenXZ - d3"のみLineHと異なるので注意
            this.lineVertical = new BezierCurve(0.0D, y0, d1, y0 + d2, lenXZ - d3, y1 + d4, lenXZ, y1);
        }
    }

    /**
     * 道床ブロックのリストを作成<br>
     * レールの生成時と破壊時に呼ばれる
     */
    protected void createRailList(RailProperty prop) {
        ModelSetRail modelSet = prop.getModelSet();
        int width = modelSet.getConfig().ballastWidth >> 1;

        this.rails.clear();
        int split = (int) (this.lineHorizontal.getLength() * 4.0D);
		/*if(height < 0.0625)
			{
				y -= 1;
			}*/
        IntStream.range(0, split).forEach(j -> {
            double[] point = this.lineHorizontal.getPoint(split, j);
            double x = point[1];
            double z = point[0];
            double slope = this.lineHorizontal.getSlope(split, j);
            double height = this.getRailHeight(split, j);
            int y = (int) height;
            int x0 = MathHelper.floor_double(x);
            int z0 = MathHelper.floor_double(z);
            IntStream.rangeClosed(1, width).forEach(i -> {
                int x1 = MathHelper.floor_double(x + Math.sin(slope + Math.PI * 0.5D) * (double) i);
                int z1 = MathHelper.floor_double(z + Math.cos(slope + Math.PI * 0.5D) * (double) i);
                int x2 = MathHelper.floor_double(x + Math.sin(slope - Math.PI * 0.5D) * (double) i);
                int z2 = MathHelper.floor_double(z + Math.cos(slope - Math.PI * 0.5D) * (double) i);
                this.addRailBlock(x1, y, z1);
                this.addRailBlock(x2, y, z2);
            });
            this.addRailBlock(x0, y, z0);
        });
    }

    protected void addRailBlock(int x, int y, int z) {
        for (int i = 0; i < this.rails.size(); ++i) {
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
//		setBaseBlock(world, x0, y0, z0);
        this.rails.forEach(rail -> {
            int x = rail[0];
            int y = rail[1];
            int z = rail[2];
            Block block2 = world.getBlock(x, y, z);
            if (!(block2 instanceof BlockLargeRailBase) || block2 == block)//異なる種類のレールを上書きしない
            {
                world.setBlock(x, y, z, block, 0, 2);
                TileEntityLargeRailBase tile = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
                if (tile != null) {
                    tile.setStartPoint(x0, y0, z0);
                }
            }
        });
        this.rails.clear();
    }

    private void setBaseBlock(World world, int x0, int y0, int z0) {
        int split = (int) (this.lineHorizontal.getLength() * 4.0D);
        RailPosition rp = getStartRP();
        int minWidth = MathHelper.floor_float(rp.constLimitWN + 0.5F);
        int maxWidth = MathHelper.floor_float(rp.constLimitWP + 0.5F);
        int minHeight = MathHelper.floor_float(rp.constLimitHN);
        int maxHeight = MathHelper.floor_float(rp.constLimitHP);
        Block[][] blocks = new Block[maxHeight - minHeight + 1][maxWidth - minWidth + 1];
        int[][] metas = new int[maxHeight - minHeight + 1][maxWidth - minWidth + 1];
        for (int k = 0; k < split - 1; k++) {
            double[] point = this.lineHorizontal.getPoint(split, k);
            double x = point[1];
            double z = point[0];
            double y = getRailHeight(split, k);
            double slope = this.lineHorizontal.getSlope(split, k);
            float yaw = MathHelper.wrapAngleTo180_float((float) NGTMath.toDegrees(slope));
            for (int i = 0; i < blocks.length; i++) {
                int h = minHeight + i;
                for (int j = 0; j < (blocks[i]).length; j++) {
                    int w = minWidth + j;
                    Vec3 vec = Vec3.createVectorHelper(w, h, 0.0D);
                    vec.rotateAroundY(yaw);
                    int[] pos = new int[]{(int) (x + vec.xCoord), (int) (y + vec.yCoord), (int) (z + vec.zCoord)};
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

    public int getNearlestPoint(int par1, double par2, double par3) {
        return this.lineHorizontal.getNearlestPoint(par1, par2, par3);
    }

    /**
     * @param par1 分割数
     * @param par2 位置
     * @return {z, x}
     */
    public double[] getRailPos(int par1, int par2) {
        return this.lineHorizontal.getPoint(par1, par2);
    }

    /**
     * @param par1 分割数
     * @param par2 位置
     * @return y
     */
    public double getRailHeight(int par1, int par2) {
        float railWidth = 3.0F;//本来はRailConfigから取得すべし
        double height = this.lineVertical.getPoint(par1, par2)[1];
        float cant = this.getCant(par1, par2);
        if (cant != 0.0F) {
            double h2 = Math.abs(NGTMath.sin(cant) * railWidth * 0.5F);
            height += h2;
        }
        return height;
    }

    /**
     * @return 0~360
     */
    public float getRailRotation(int par1, int par2) {
        return NGTMath.toDegrees((float) this.lineHorizontal.getSlope(par1, par2));
    }

    /**
     * @param par1 分割数
     * @param par2 位置
     * @return 0~360
     */
    public float getRailPitch(int par1, int par2) {
        return NGTMath.toDegrees((float) this.lineVertical.getSlope(par1, par2));
    }

    /**
     * @return 0~360
     */
    public float getRailPitch() {
        double height = this.endRP.posY - this.startRP.posY;
        if (height == 0.0D) {
            return 0.0F;
        }
        double length = this.getLength();
        return NGTMath.toDegrees((float) Math.atan2(height, length));
    }

    /**
     * @return カント
     */
    public float getCant(int split, int t) {
		/*float yawStart = NGTMath.toDegrees((float)this.lineHorizontal.getSlope(split, 0));
		float yawEnd = NGTMath.toDegrees((float)this.lineHorizontal.getSlope(split, split));
		float yawT = NGTMath.toDegrees((float)this.lineHorizontal.getSlope(split, t));
		float yawDifA = Math.abs(MathHelper.wrapDegrees(yawEnd - yawStart));
		if(yawDifA == 0.0F)
		{
			return 0.0F;//始点と終点の向きが正反対、S字も含むので下記と別処理を要検討
		}
		else
		{
			float yawT2 = yawT - yawStart;
			float yawT2A = Math.abs(yawT2);
			float yawH = yawDifA * 0.5F;
			if(yawT2A > yawH)
			{
				yawT2A = yawDifA - yawT2A;
			}
			float yawRes = yawT2A / yawH;//0.0~1.0
			if(yawT2 > 0.0F)
			{
				yawRes = -yawRes;
			}
			return yawRes;
		}*/

        float ft = 2.0F * (float) t / (float) split;
        //endのcantは反転させる
        float c1 = (ft <= 1.0F) ? (1.0F - ft) * this.startRP.cantEdge : (ft - 1.0F) * -this.endRP.cantEdge;
        //cuntCenterは共通
        float c2 = (ft <= 1.0F) ? ft * this.startRP.cantCenter : (2.0F - ft) * this.startRP.cantCenter;
        float cunt = (c1 + c2);

        float rand = 0.0F;
        if (this.startRP.cantRandom > 0.0F) {
            float x = (float) (this.getLength() * (float) (t) / (float) (split)) * this.startRP.cantRandom;
            float scale = 3.0F;
            rand = NGTMath.getSin(x) + NGTMath.getSin(x * 0.51F) + NGTMath.getSin(x * 0.252F) + NGTMath.getSin(x * 0.1253F) * 0.25F * scale;
        }
        return cunt + rand;
    }


    public double getLength() {
        if (this.length <= 0.0D) {
            double height = this.endRP.posY - this.startRP.posY;
            if (height == 0.0D) {
                this.length = this.lineHorizontal.getLength();
            } else {
                double d0 = this.lineHorizontal.getLength();
                this.length = Math.sqrt(d0 * d0 + height * height);
            }
        }
        return this.length;
    }

    public boolean hasPoint(int x, int z) {
        boolean flag1 = this.startRP.blockX == x && this.startRP.blockZ == z;
        boolean flag2 = this.endRP.blockX == x && this.endRP.blockZ == z;
        return flag1 || flag2;
    }

    /**
     * 両端にRS入力されてるか
     */
    public boolean isGettingPowered(World world) {
        return this.startRP.checkRSInput(world) && this.endRP.checkRSInput(world);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RailMap) {
            RailMap rm = (RailMap) obj;
            return this.startRP.blockX == rm.startRP.blockX && this.startRP.blockY == rm.startRP.blockY && this.startRP.blockZ == rm.startRP.blockZ;
        }
        return false;
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

        if (this.equals(railMap)) {
            return true;
        }

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                double[] p0 = this.lineHorizontal.getPoint(10, i * 10);
                double[] p1 = railMap.lineHorizontal.getPoint(10, j * 10);
                if (NGTMath.compare(p0[0], p1[0], 5) && NGTMath.compare(p0[1], p1[1], 5)) {
                    return true;
                }
            }
        }

        return false;
    }

    public RailPosition getStartRP() {
        return this.startRP;
    }

    public RailPosition getEndRP() {
        return this.endRP;
    }
}