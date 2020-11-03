package jp.ngt.rtm.entity.train.util;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BogieController {
	private final EntityBogie[] bogies = new EntityBogie[2];

	public void createBogie(World world, EntityTrainBase train) {
		this.bogies[0] = new EntityBogie(world, (byte) 0);
		this.bogies[1] = new EntityBogie(world, (byte) 1);
	}

	public void spawnBogies(World world, EntityTrainBase train) {
		if (!world.isRemote) {
			for (int i = 0; i < 2; ++i) {
				EntityBogie bogie = this.getBogie(i);

				if (world.spawnEntityInWorld(bogie)) {
					bogie.setFront(true);
					bogie.setTrain(train);
					train.setBogie(i, bogie);
				} else {
					NGTLog.debug("[RTM] Can't spawn bogie " + i);
					return;
				}
			}
		}
	}

	public void setupBogiePos(EntityTrainBase train) {
		float ro0 = MathHelper.wrapAngleTo180_float(train.rotationYaw);
		float[][] bPosArray = train.getModelSet().getConfig().getBogiePos();

		float bPos = bPosArray[0][2];
		this.getBogie(0).setPositionAndRotation(
				train.posX + NGTMath.sin(ro0) * bPos, train.posY, train.posZ + NGTMath.cos(ro0) * bPos,
				ro0, 0.0F);

		bPos = bPosArray[1][2];
		this.getBogie(1).setPositionAndRotation(
				train.posX + NGTMath.sin(ro0) * bPos, train.posY, train.posZ + NGTMath.cos(ro0) * bPos,
				MathHelper.wrapAngleTo180_float(ro0 + 180.0F), 0.0F);
	}

	public EntityBogie getBogie(int bogieId) {
		return this.bogies[bogieId];
	}

	private EntityBogie getBogie(boolean isFront) {
		return this.getBogie(0).isFront() == isFront ? this.getBogie(0) : this.getBogie(1);
	}

	public void setBogie(int bogieId, EntityBogie bogie) {
		this.bogies[bogieId] = bogie;
	}

	public void updateBogies() {
		this.getBogie(0).updateBogie();
		this.getBogie(1).updateBogie();
	}

	public void setDead() {
        EntityBogie bogieF = this.getBogie(0);
        if (bogieF != null) {
            bogieF.setDead();
        }
        EntityBogie bogieB = this.getBogie(1);
        if (bogieB != null) {
            bogieB.setDead();
        }
    }

//	public void moveTrainWithBogie(EntityTrainBase train, float speed) {
//		if (speed == 0.0F) {
//			/*停車中に台車位置を調整*/
//			this.updateBogiePos(train, 0, UpdateFlag.NONE);
//			this.updateBogiePos(train, 0, UpdateFlag.NONE);
//			return;
//		}
//
//		EntityBogie frontBogie = this.bogies[0].isFront() ? this.bogies[0] : this.bogies[1];
//		EntityBogie backBogie = !this.bogies[0].isFront() ? this.bogies[0] : this.bogies[1];
//		float[][] pos = train.getModelSet().getConfig().getBogiePos();
//		float lengthF = pos[0][2];
//		float lengthB = pos[1][2];
//		float trainLength = MathHelper.abs(lengthF - lengthB);
//
//		if (frontBogie.updateBogiePos(speed, 0.0F, null)) {
//			if (backBogie.updateBogiePos(speed, trainLength, frontBogie)) {
//				this.updateTrainPos(train, lengthF, lengthB);
//			}
//		}
//	}

	public void moveTrainWithBogie(EntityTrainBase train, EntityTrainBase prevTrain, float speed, boolean forceMove) {
		if (speed == 0.0F && !forceMove) {
			this.updateBogiePos(train, 0, UpdateFlag.NONE);
			this.updateBogiePos(train, 1, UpdateFlag.NONE);
			return;
		}
		EntityBogie frontBogie = getBogie(true);
		EntityBogie backBogie = getBogie(false);
		float[][] bogiePos = train.getModelSet().getConfig().getBogiePos();
		float lengthF = bogiePos[0][2];
		float lengthB = bogiePos[1][2];
		float trainLength = MathHelper.abs(lengthF - lengthB);
		boolean flag;
		if (prevTrain == null) {
			flag = frontBogie.updateBogiePos(speed, 0.0F, null);
		} else {
			float[][] bogiePos2 = prevTrain.getModelSet().getConfig().getBogiePos();
			double disTrain = train.getDefaultDistanceToConnectedTrain(prevTrain);
			double lenBF = MathHelper.abs(bogiePos2[1 - prevTrain.getTrainDirection()][2]);
			double lenBB = MathHelper.abs(bogiePos[train.getTrainDirection()][2]);
			float disBogie = (float) (disTrain - lenBF - lenBB);
			EntityBogie prevBogie = prevTrain.getBogie(1 - prevTrain.getTrainDirection());
			flag = frontBogie.updateBogiePos(speed, disBogie, prevBogie);
		}
		if (flag && backBogie.updateBogiePos(speed, trainLength, frontBogie)) {
			this.updateTrainPos(train, lengthF, lengthB);
		}
	}


	/**
	 * 台車2つを元に車体位置を更新
	 */
	private void updateTrainPos(EntityTrainBase train, float lf, float lb) {
		//車体長分の先頭側台車の位置
		double d0 = Math.abs(lf) / (Math.abs(lf - lb));
		double[] fp = this.getBogie(0).getPosBuf();
		double[] bp = this.getBogie(1).getPosBuf();

		double x = fp[0] + (bp[0] - fp[0]) * d0;
		double y = (fp[1] + bp[1]) * 0.5D;
		double z = fp[2] + (bp[2] - fp[2]) * d0;

		double x0 = fp[0] - bp[0];
		double y0 = fp[1] - bp[1];
		double z0 = fp[2] - bp[2];
		float yaw = (float) MathHelper.wrapAngleTo180_double(NGTMath.toDegrees(Math.atan2(x0, z0)));
		float pitch = (float) MathHelper.wrapAngleTo180_double(NGTMath.toDegrees(Math.atan2(y0, Math.sqrt(x0 * x0 + z0 * z0))));
		float roll = (this.getBogie(0).rotationRoll + -(this.getBogie(1).rotationRoll)) * 0.5F;

		//カント分車体をずらす
		Vec3 vec = Vec3.createVectorHelper(0.0D, EntityTrainBase.TRAIN_HEIGHT, 0.0D);
		vec.rotateAroundZ(NGTMath.toRadians(-roll));
		//vec = vec.rotateAroundX(NGTMath.toRadians(pitch));
		vec.rotateAroundY(NGTMath.toRadians(yaw));
		x += vec.xCoord;
		y += vec.yCoord - EntityTrainBase.TRAIN_HEIGHT;
		z += vec.zCoord;

		train.setPositionAndRotation(x, y, z, yaw, pitch);
		train.updateRoll(roll);

		this.updateBogiePos(train, 0, UpdateFlag.ALL);
		this.updateBogiePos(train, 1, UpdateFlag.ALL);
	}

	/**
	 * 車体位置を元に台車位置を更新<br>
	 * 台車位置を再計算することで、車体とのずれを解消する
	 */
	public void updateBogiePos(EntityTrainBase train, int bogieIndex, UpdateFlag flag) {
		float[][] pos = train.getModelSet().getConfig().getBogiePos();
		Vec3 v31 = Vec3.createVectorHelper(pos[bogieIndex][0], pos[bogieIndex][1], pos[bogieIndex][2]);
		v31.rotateAroundX(NGTMath.toRadians(train.rotationPitch));
		v31.rotateAroundY(NGTMath.toRadians(train.rotationYaw));
		this.getBogie(bogieIndex).moveBogie(train, train.posX + v31.xCoord, train.posY + v31.yCoord, train.posZ + v31.zCoord, flag);
	}

	public enum UpdateFlag {
		/**
		 * 全Rotationを更新
		 */
		ALL,
		/**
		 * Yawのみ更新、転車台で使用
		 */
		YAW,
		/**
		 * Rotation更新なし
		 */
		NONE
	}
}