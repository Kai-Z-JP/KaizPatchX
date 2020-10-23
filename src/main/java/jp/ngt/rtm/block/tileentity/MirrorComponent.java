package jp.ngt.rtm.block.tileentity;

import jp.ngt.ngtlib.block.EnumFace;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.BlockMirror.MirrorType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector3f;

/**
 * ブロック1つの鏡面
 */
public class MirrorComponent {
	public MirrorObject mirrorObject;
	public final int blockX;
	public final int blockY;
	public final int blockZ;
	public final double mirrorX;
	public final double mirrorY;
	public final double mirrorZ;

	/**
	 * プレーヤーから鏡中心への視線
	 */
	private final Vector3f lookVec = new Vector3f();
	public float uMin;
	public float uMax;
	public float vMin;
	public float vMax;

	public boolean calledFromRenderer;
	private boolean skipRender = false;

	private final float[] whPos = new float[4];

	public MirrorComponent(int x, int y, int z, MirrorType type, EnumFace face) {
		this.blockX = x;
		this.blockY = y;
		this.blockZ = z;

		//鏡面の中心
		float f0 = type == MirrorType.Mono_Panel ? -0.4375F : 0.5F;
		this.mirrorX = (double) this.blockX + 0.5D + (double) (face.normal[0] * f0);
		this.mirrorY = (double) this.blockY + 0.5D + (double) (face.normal[1] * f0);
		this.mirrorZ = (double) this.blockZ + 0.5D + (double) (face.normal[2] * f0);
	}

	public Vec3 getMirrorPos(EnumFace face) {
		double x = this.mirrorX * (double) Math.abs(face.normal[0]);
		double y = this.mirrorY * (double) Math.abs(face.normal[1]);
		double z = this.mirrorZ * (double) Math.abs(face.normal[2]);
		return Vec3.createVectorHelper(x, y, z);
	}

	public void update(MirrorObject obj, EntityLivingBase viewer) {
		EnumFace face = obj.face;

		//視線ベクトル
		float vx = (float) (this.mirrorX - viewer.posX);
		float vy = (float) (this.mirrorY - viewer.posY - (double) MirrorObject.getEyeHeight(viewer));
		float vz = (float) (this.mirrorZ - viewer.posZ);
		this.lookVec.set(vx, vy, vz);

		this.skipRender = !(this.canLook(face) && this.calledFromRenderer);
		this.calledFromRenderer = false;

		if (obj.type == MirrorType.Hexa_Cube) {
			Block block = viewer.worldObj.getBlock(this.blockX + (int) face.normal[0], this.blockY + (int) face.normal[1], this.blockZ + (int) face.normal[2]);
			this.skipRender |= (block.isNormalCube() || block == RTMBlock.mirrorCube);
		}

		if (this.skipRender) {
			return;
		}

		//反転した視線ベクトル(相対座標)
		float fx = vx * face.flip[0];
		float fy = vy * face.flip[1];
		float fz = vz * face.flip[2];

		float xn = fx - 0.5F;
		float xp = fx + 0.5F;
		float yn = fy - 0.5F;
		float yp = fy + 0.5F;
		float zn = fz - 0.5F;
		float zp = fz + 0.5F;

		switch (face) {
			case BOTTOM:
				this.setupUV(obj, -zp, -zn, xn, xp, fy);
				break;
			case TOP:
				this.setupUV(obj, zn, zp, xn, xp, fy);
				break;
			case BACK:
				this.setupUV(obj, -yp, -yn, -xp, -xn, fz);
				break;
			case FRONT:
				this.setupUV(obj, -yp, -yn, xn, xp, fz);
				break;
			case LEFT:
				this.setupUV(obj, -yp, -yn, zn, zp, fx);
				break;
			case RIGHT:
				this.setupUV(obj, -yp, -yn, -zp, -zn, fx);
				break;
			default:
				break;
		}
	}

	private void setupUV(MirrorObject obj, float hNeg, float hPos, float wNeg, float wPos, float d) {
		/*float hNegA = Math.abs(hNeg);
		float hPosA = Math.abs(hPos);
		float wNegA = Math.abs(wNeg);
		float wPosA = Math.abs(wPos);
		float viewWidth = wNegA > wPosA ? wNegA : wPosA;
		float viewHeight = hNegA > hPosA ? hNegA : hPosA;
		obj.setWidthAndHeight(viewWidth, viewHeight);*/

		obj.setSize(-hNeg, -hPos, -wPos, -wNeg);

		this.whPos[0] = wPos;
		this.whPos[1] = wNeg;
		this.whPos[2] = hPos;
		this.whPos[3] = hNeg;

		/*this.whPos[0] = -wNeg;
		this.whPos[1] = -wPos;
		this.whPos[2] = -hNeg;
		this.whPos[3] = -hPos;*/
	}

	/*public void updateUV(float w, float h)
	{
		double f0 = 0.5D / (double)w;
		double f1 = 0.5D / (double)h;
		this.uMin = 0.5F - (float)((double)this.whPos[0] * f0);
		this.uMax = 0.5F - (float)((double)this.whPos[1] * f0);
		this.vMin = 0.5F - (float)((double)this.whPos[2] * f1);
		this.vMax = 0.5F - (float)((double)this.whPos[3] * f1);
	}*/

	public void updateFrustrum(float t, float b, float l, float r) {
		double w = 1.0D / (double) (r - l);
		double h = 1.0D / (double) (t - b);
		this.uMin = (float) ((double) (-l - this.whPos[0]) * w);
		this.uMax = (float) ((double) (-l - this.whPos[1]) * w);
		this.vMin = (float) ((double) (-b - this.whPos[2]) * h);
		this.vMax = (float) ((double) (-b - this.whPos[3]) * h);
	}

	public boolean skipRender() {
		return this.skipRender;
	}

	/**
	 * 鏡面がプレーヤーから見えてるかどうか
	 */
	private boolean canLook(EnumFace face) {
		float f0 = this.lookVec.x * face.normal[0] + this.lookVec.y * face.normal[1] + this.lookVec.z * face.normal[2];
		return f0 <= 0.0F;
	}
}