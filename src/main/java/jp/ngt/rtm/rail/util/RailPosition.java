package jp.ngt.rtm.rail.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * レールの曲線を構成する点
 */
public class RailPosition {
	/**
	 * 補正値、デフォルト長=これ*マーカー間距離最小値
	 */
	protected static final float Anchor_Correction_Value = 0.55228475F;//(√(2)-1)*4/3
	/**
	 * 向きごとのベジェ曲線開始位置への補正値
	 */
	public static final float[][] REVISION = new float[][]{{0.0F, -0.5F}, {-0.5F, -0.5F},
			{-0.5F, 0.0F}, {-0.5F, 0.499999F},
			{0.0F, 0.499999F}, {0.499999F, 0.499999F},
			{0.499999F, 0.0F}, {0.499999F, -0.5F}};

	public int blockX;
	public int blockY;
	public int blockZ;
	/**
	 * 0~7
	 */
	public final byte direction;
	/**
	 * 0~15
	 */
	public byte height;
	public float anchorDirection;
	public float anchorLength;
	/**
	 * 0:通常, 1:分岐
	 */
	public final byte switchType;

	public double posX;
	public double posY;
	public double posZ;

	public RailPosition(int x, int y, int z, byte dir) {
		this(x, y, z, dir, (byte) 0, MathHelper.wrapAngleTo180_float((float) dir * 45.0F), -1.0F, (byte) 0);
	}

	public RailPosition(int x, int y, int z, byte dir, byte type) {
		this(x, y, z, dir, (byte) 0, MathHelper.wrapAngleTo180_float((float) dir * 45.0F), -1.0F, type);
	}

	public RailPosition(int x, int y, int z, byte dir, byte h, float dir2, float anchor, byte type) {
		this.blockX = x;
		this.blockY = y;
		this.blockZ = z;
		this.direction = dir;
		this.height = h;
		this.anchorDirection = dir2;
		this.anchorLength = anchor;
		this.switchType = type;

		this.posX = (double) x + 0.5D + (double) REVISION[dir][0];
		this.posY = (double) y + (double) (h + 1) * 0.0625D;
		this.posZ = (double) z + 0.5D + (double) REVISION[dir][1];
	}

	public static RailPosition readFromNBT(NBTTagCompound nbt) {
		int[] pos = nbt.getIntArray("BlockPos");
		byte b0 = nbt.getByte("Direction");
		byte b1 = nbt.getByte("Height");
		float f0 = nbt.getFloat("A_Direction");
		float f1 = nbt.getFloat("A_Length");
		byte b2 = nbt.getByte("SwitchType");
		return new RailPosition(pos[0], pos[1], pos[2], b0, b1, f0, f1, b2);
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setIntArray("BlockPos", new int[]{this.blockX, this.blockY, this.blockZ});
		nbt.setByte("Direction", this.direction);
		nbt.setByte("Height", this.height);
		nbt.setFloat("A_Direction", this.anchorDirection);
		nbt.setFloat("A_Length", this.anchorLength);
		nbt.setByte("SwitchType", this.switchType);
		return nbt;
	}

	public void setHeight(byte par1) {
		this.height = par1;
		this.posY = (double) this.blockY + (double) (par1 + 1) * 0.0625D;
	}

	/**
	 * 与えられた距離だけ平行移動
	 */
	public void movePos(int x, int y, int z) {
		this.blockX += x;
		this.blockY += y;
		this.blockZ += z;
		this.posX += (double) x;
		this.posY += (double) y;
		this.posZ += (double) z;
	}

	/**
	 * p1に対してp2がどっちの向きか
	 */
	public RailDir getDir(RailPosition p1, RailPosition p2) {
		Vec3 vec1 = Vec3.createVectorHelper(p1.posX - this.posX, p1.posY - this.posY, p1.posZ - this.posZ);
		Vec3 vec2 = Vec3.createVectorHelper(p2.posX - this.posX, p2.posY - this.posY, p2.posZ - this.posZ);
		double d0 = vec1.zCoord * vec2.xCoord - vec1.xCoord * vec2.zCoord;
		return d0 > 0.0D ? RailDir.LEFT : (d0 < 0.0D ? RailDir.RIGHT : RailDir.NONE);
	}

	public boolean checkRSInput(World world) {
		return world.isBlockIndirectlyGettingPowered(this.blockX, this.blockY, this.blockZ);
	}
}