package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.NGTVec;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.*;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachineClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public class MachinePartsRenderer extends TileEntityPartsRenderer<ModelSetMachineClient> {
	public MachinePartsRenderer(String... par1) {
		super(par1);
	}

	public float getMovingCount(TileEntity par1) {
		if (par1 != null) {
			if (par1.getBlockType() == RTMBlock.crossingGate) {
				TileEntityCrossingGate gate = (TileEntityCrossingGate) par1;
				return (float) gate.barMoveCount / 90.0F;
			} else if (par1.getBlockType() == RTMBlock.turnstile) {
				TileEntityTurnstile turnstile = (TileEntityTurnstile) par1;
				return turnstile.canThrough() ? 0.0F : 1.0F;
			} else if (par1.getBlockType() == RTMBlock.point) {
				TileEntityPoint point = (TileEntityPoint) par1;
				return point.isActivated() ? 1.0F : 0.0F;
			}
		}
		return 0.0F;
	}

	/**
	 * -1:OFF, 0 or 1:ON
	 */
	public int getLightState(TileEntity par1) {
		if (par1 != null) {
			if (par1.getBlockType() == RTMBlock.crossingGate) {
				TileEntityCrossingGate gate = (TileEntityCrossingGate) par1;
				return gate.lightCount;
			} else if (par1.getBlockType() == RTMBlock.light) {
				TileEntityLight light = (TileEntityLight) par1;
				return light.isGettingPower ? 1 : -1;
			}
		}
		return -1;
	}

	public int getLodState(TileEntity par1) {
		if (par1 != null) {
			if (par1.getBlockType() == RTMBlock.point) {
				TileEntityPoint point = (TileEntityPoint) par1;
				return point.getMove() > 0.0F ? 1 : -1;
			}
		}
		return 0;
	}

	public int getTick(TileEntity par1) {
		return par1 == null ? 0 : ((TileEntityMachineBase) par1).tick;
	}

	public float getPitch(TileEntity par1) {
		if (par1 != null) {
			TileEntityMachineBase machine = (TileEntityMachineBase) par1;
			return machine.getPitch();
		}
		return 0.0F;
	}

	public float getYaw(TileEntity par1) {
		if (par1 != null) {
			TileEntityMachineBase machine = (TileEntityMachineBase) par1;
			return machine.getRotation();
		}
		return 0.0F;
	}

	private static final Vector3f VEC3F_TMP = new Vector3f();
	private static final NGTVec VEC_TMP = new NGTVec(0.0D, 0.0D, 0.0D);

	public Vector3f getNormal(TileEntity par1, float x, float y, float z, float pitch, float yaw) {
		if (par1 != null) {
			TileEntityMachineBase machine = (TileEntityMachineBase) par1;
			Vec3 vec = machine.getNormal(x, y, z, pitch, yaw);
			VEC3F_TMP.set((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord);
		}
		return VEC3F_TMP;
	}

	public double[] getLightPos(TileEntity par1, float x, float y, float z, float pitch, float yaw) {
		if (par1 == null) {
			return new double[3];
		}

		TileEntityMachineBase machine = (TileEntityMachineBase) par1;
		VEC_TMP.setValue(x, y, z);
		rotateVec(VEC_TMP, machine.getBlockMetadata(), pitch, yaw);
		double posX = (double) par1.xCoord + 0.5D + VEC_TMP.xCoord;
		double posY = (double) par1.yCoord + 0.5D + VEC_TMP.yCoord;
		double posZ = (double) par1.zCoord + 0.5D + VEC_TMP.zCoord;
		return new double[]{posX, posY, posZ};
	}

	public static void rotateVec(Vec3 vec, int dir, float pitch, float yaw) {
		switch (dir) {
			case 0://-y
				vec.rotateAroundX(NGTMath.toRadians(pitch));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				break;
			case 1://+y
				vec.rotateAroundX(NGTMath.toRadians(-pitch));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				break;
			case 2://z
				vec.rotateAroundX(NGTMath.toRadians(-pitch + 90.0F));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				vec.rotateAroundX(NGTMath.toRadians(90.0F));
				break;
			case 3://z
				vec.rotateAroundX(NGTMath.toRadians(-pitch + 90.0F));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				vec.rotateAroundX(NGTMath.toRadians(-90.0F));
				break;
			case 4://x
				vec.rotateAroundX(NGTMath.toRadians(-pitch + 90.0F));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				vec.rotateAroundZ(NGTMath.toRadians(-90.0F));
				break;
			case 5://x
				vec.rotateAroundX(NGTMath.toRadians(-pitch + 90.0F));
				vec.rotateAroundY(NGTMath.toRadians(yaw));
				vec.rotateAroundZ(NGTMath.toRadians(90.0F));
				break;
		}
	}
}