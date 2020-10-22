package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.BezierCurve;
import jp.ngt.ngtlib.math.ILine;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.StraightLine;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.modelpack.modelset.ModelSetRail;
import jp.ngt.rtm.rail.BlockLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RailMap {
	protected ILine line;
	protected RailPosition startRP;
	protected RailPosition endRP;

	protected final List<int[]> rails = new ArrayList<int[]>();
	protected double length;

	public RailMap(RailPosition par1, RailPosition par2) {
		this.startRP = par1;
		this.endRP = par2;
		this.createLine();
	}

	public void rebuild() {
		this.createLine();
	}

	protected void createLine() {
		double z0 = this.startRP.posZ;
		double x0 = this.startRP.posX;
		double z1 = this.endRP.posZ;
		double x1 = this.endRP.posX;

		boolean flag1 = (this.endRP.direction - this.startRP.direction) % 4 == 0;
		boolean flag2 = (z0 == z1 || x0 == x1);//直角
		boolean flag3 = (Math.abs(z0 - z1) == Math.abs(x0 - x1) && this.startRP.direction % 2 != 0 && this.endRP.direction % 2 != 0);//45度
		if (flag1 && (flag2 || flag3)) {
			this.line = new StraightLine(z0, x0, z1, x1);
		} else {
			double ddz = Math.abs(z1 - z0);
			double ddx = Math.abs(x1 - x0);
			double max = ddz >= ddx ? ddz : ddx;
			double min = ddz <= ddx ? ddz : ddx;
			if (this.startRP.anchorLength <= 0.0F) {
				boolean b0 = this.startRP.direction % 2 == 0;//true:まっすぐ, false:斜め
				double d1 = b0 ? max : min;
				this.startRP.anchorLength = (float) (d1 * RailPosition.Anchor_Correction_Value);
			}

			if (this.endRP.anchorLength <= 0.0F) {
				boolean b0 = this.endRP.direction % 2 == 0;
				double d1 = b0 ? max : min;
				this.endRP.anchorLength = (float) (d1 * RailPosition.Anchor_Correction_Value);
			}

			double d1 = MathHelper.cos(NGTMath.toRadians(this.startRP.anchorDirection)) * this.startRP.anchorLength;
			double d2 = MathHelper.sin(NGTMath.toRadians(this.startRP.anchorDirection)) * this.startRP.anchorLength;
			double d3 = MathHelper.cos(NGTMath.toRadians(this.endRP.anchorDirection)) * this.endRP.anchorLength;
			double d4 = MathHelper.sin(NGTMath.toRadians(this.endRP.anchorDirection)) * this.endRP.anchorLength;
			this.line = new BezierCurve(z0, x0, z0 + d1, x0 + d2, z1 + d3, x1 + d4, z1, x1);
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
		int split = (int) (this.line.getLength() * 4.0D);
		for (int j = 0; j < split; ++j) {
			double[] point = this.line.getPoint(split, j);
			double x = point[1];
			double z = point[0];
			double slope = this.line.getSlope(split, j);
			double height = this.getRailHeight(split, j);
			int y = (int) height;
			/*if(height < 0.0625)
			{
				y -= 1;
			}*/
			int x0 = MathHelper.floor_double(x);
			int z0 = MathHelper.floor_double(z);
			for (int i = 1; i <= width; ++i) {
				double d0 = (double) i;
				int x1 = MathHelper.floor_double(x + Math.sin(slope + Math.PI * 0.5D) * d0);
				int z1 = MathHelper.floor_double(z + Math.cos(slope + Math.PI * 0.5D) * d0);
				int x2 = MathHelper.floor_double(x + Math.sin(slope - Math.PI * 0.5D) * d0);
				int z2 = MathHelper.floor_double(z + Math.cos(slope - Math.PI * 0.5D) * d0);
				this.addRailBlock(x1, y, z1);
				this.addRailBlock(x2, y, z2);
			}
			this.addRailBlock(x0, y, z0);
		}
	}

	private void addRailBlock(int x, int y, int z) {
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
		this.rails.add(new int[]{x, y, z});
	}

	/**
	 * ブロックの設置
	 */
	public void setRail(World world, Block block, int x0, int y0, int z0, RailProperty prop) {
		this.createRailList(prop);
		for (int i = 0; i < this.rails.size(); ++i) {
			int x = this.rails.get(i)[0];
			int y = this.rails.get(i)[1];
			int z = this.rails.get(i)[2];
			Block block2 = world.getBlock(x, y, z);
			if (!(block2 instanceof BlockLargeRailBase) || block2 == block)//異なる種類のレールを上書きしない
			{
				world.setBlock(x, y, z, block, 0, 2);
				TileEntityLargeRailBase tile = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
				if (tile != null) {
					tile.setStartPoint(x0, y0, z0);
				}
			}
		}
		this.rails.clear();
	}

	/**
	 * ブロックの破壊
	 */
	public void breakRail(World world, RailProperty prop, TileEntityLargeRailCore core) {
		this.createRailList(prop);
		for (int i = 0; i < this.rails.size(); ++i) {
			int x = this.rails.get(i)[0];
			int y = this.rails.get(i)[1];
			int z = this.rails.get(i)[2];
			if (world.getBlock(x, y, z) instanceof BlockLargeRailBase) {
				TileEntityLargeRailBase rail = (TileEntityLargeRailBase) world.getTileEntity(x, y, z);
				if (rail.getRailCore() == core) {
					world.setBlockToAir(x, y, z);
				}
			}
		}
		this.rails.clear();
	}

	public boolean canPlaceRail(World world, boolean isCreative, RailProperty prop) {
		this.createRailList(prop);
		boolean flag = true;
		for (int i = 0; i < this.rails.size(); ++i) {
			int x = this.rails.get(i)[0];
			int y = this.rails.get(i)[1];
			int z = this.rails.get(i)[2];
			Block block = world.getBlock(x, y, z);
			boolean b0 = world.isAirBlock(x, y, z) || block == RTMBlock.marker || block == RTMBlock.markerSwitch || block == RTMBlock.markerSlope || (block instanceof BlockLargeRailBase && !((BlockLargeRailBase) block).isCore());
			if (!isCreative && !b0) {
				NGTLog.sendChatMessageToAll("message.rail.obstacle", new Object[]{":" + x + "," + y + "," + z});
				return false;
			}
			flag = b0 && flag;
		}
		return isCreative || flag;
	}

	public List<int[]> getRailBlockList(RailProperty prop) {
		this.createRailList(prop);
		return new ArrayList<int[]>(this.rails);
	}

	public int getNearlestPoint(int par1, double par2, double par3) {
		return this.line.getNearlestPoint(par1, par2, par3);
	}

	/**
	 * @param par1 分割数
	 * @param par2 位置
	 * @return {z, x}
	 */
	public double[] getRailPos(int par1, int par2) {
		return this.line.getPoint(par1, par2);
	}

	/**
	 * @param par1 分割数
	 * @param par2 位置
	 * @return y
	 */
	public double getRailHeight(int par1, int par2) {
		double height = this.endRP.posY - this.startRP.posY;
		if (height == 0.0D) {
			return this.startRP.posY;
		}
		return this.startRP.posY + ((height * (double) par2) / (double) par1);
	}

	/**
	 * @return 0~360
	 */
	public float getRailRotation(int par1, int par2) {
		return NGTMath.toDegrees((float) this.line.getSlope(par1, par2));
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

	public double getLength() {
		if (this.length <= 0.0D) {
			double height = this.endRP.posY - this.startRP.posY;
			if (height == 0.0D) {
				this.length = this.line.getLength();
			} else {
				double d0 = this.line.getLength();
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
			if (this.startRP.blockX == rm.startRP.blockX && this.startRP.blockY == rm.startRP.blockY && this.startRP.blockZ == rm.startRP.blockZ) {
				return true;
			}
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
				double[] p0 = this.line.getPoint(10, i * 10);
				double[] p1 = railMap.line.getPoint(10, j * 10);
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