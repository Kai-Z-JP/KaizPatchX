package jp.ngt.rtm.entity.ai;

import jp.ngt.rtm.electric.SignalLevel;
import jp.ngt.rtm.entity.npc.EntityMotorman;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.EnumNotch;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Arrays;

public class EntityAIDrivingWithSignal extends EntityAIBase {
	protected final EntityMotorman motorman;
	protected EntityTrainBase train;

	public EntityAIDrivingWithSignal(EntityMotorman par1) {
		this.motorman = par1;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return this.motorman.isRiding() && this.motorman.ridingEntity instanceof EntityTrainBase;
	}

	@Override
	public void startExecuting() {
		this.train = (EntityTrainBase) this.motorman.ridingEntity;
	}

	@Override
	public boolean continueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void updateTask() {
		int signal = this.train.getSignal();
		float prevSpeed = this.train.getSpeed();
		float targetSpeed = SignalLevel.getSpeed(signal, prevSpeed);
		int notch = this.getSuitableNotch(targetSpeed, prevSpeed).id;
		this.train.setNotch(notch);
		//NGTLog.debug("set notch : " + notch);
	}

	/**
	 * @param par1 : 目標の速度
	 * @param par2 : 現在の速度
	 */
	private EnumNotch getSuitableNotch(float par1, float par2) {
		float gap = par1 - par2;
		if (gap > 0.0F) {
			return Arrays.stream(EnumNotch.values()).filter(notch -> notch.max_speed >= par1).findFirst().orElse(EnumNotch.inertia);
		} else if (gap == 0.0F) {
			return EnumNotch.inertia;
		} else {
			/*int i0 =  (int)(((gap / 1.5F) * 8.0F) - 0.5F);
			if(i0 == 0 && gap < 0.0F)
			{
				return EnumNotch.brake_1;
			}
			return EnumNotch.getNotch(i0);*/
			return EnumNotch.brake_4;
		}
	}
}