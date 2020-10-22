package jp.ngt.rtm.entity.train.util;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BogieController {
	private EntityBogie[] bogies = new EntityBogie[2];

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

	public void setBogie(int bogieId, EntityBogie bogie) {
		this.bogies[bogieId] = bogie;
	}

	public void updateBogies() {
		this.getBogie(0).updateBogie();
		this.getBogie(1).updateBogie();
	}

	public void setDead() {
		this.getBogie(0).setDead();
		this.getBogie(1).setDead();
	}

	public void moveTrainWithBogie(EntityTrainBase train, float speed) {
		if (speed == 0.0F) {
			/**停車中に台車位置を調整*/
			this.updateBogiePos(train, 0, false);
			this.updateBogiePos(train, 0, false);
			return;
		}

		EntityBogie frontBogie = this.bogies[0].isFront() ? this.bogies[0] : this.bogies[1];
		EntityBogie backBogie = !this.bogies[0].isFront() ? this.bogies[0] : this.bogies[1];
		float[][] pos = train.getModelSet().getConfig().getBogiePos();
		float lengthF = pos[0][2];
		float lengthB = pos[1][2];
		float trainLength = MathHelper.abs(lengthF - lengthB);

		if (frontBogie.updateBogiePos(speed, 0.0F, null)) {
			if (backBogie.updateBogiePos(speed, trainLength, frontBogie)) {
				this.updateTrainPos(train, lengthF, lengthB);
			}
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

		//double disF = this.getBogie(0).getDistance(fp[0], fp[1], fp[2]);
		//double disB = this.getBogie(1).getDistance(bp[0], bp[1], bp[2]);
		//NGTLog.debug("F:%4.3f, B:%4.3f, s:%4.3f", disF, disB, train.getSpeed());
		train.setPositionAndRotation(x, y, z, yaw, pitch);

		this.updateBogiePos(train, 0, true);
		this.updateBogiePos(train, 1, true);
	}

	/**
	 * 車体位置を元に台車位置を更新<br>
	 * 台車位置を再計算することで、車体とのずれを解消する
	 */
	public void updateBogiePos(EntityTrainBase train, int bogieIndex, boolean updateRotation) {
		float[][] pos = train.getModelSet().getConfig().getBogiePos();
		Vec3 v31 = Vec3.createVectorHelper(pos[bogieIndex][0], pos[bogieIndex][1], pos[bogieIndex][2]);
		v31.rotateAroundX(NGTMath.toRadians(train.rotationPitch));
		v31.rotateAroundY(NGTMath.toRadians(train.rotationYaw));
		this.getBogie(bogieIndex).moveBogie(train.posX + v31.xCoord, train.posY + v31.yCoord, train.posZ + v31.zCoord, updateRotation);
	}
}