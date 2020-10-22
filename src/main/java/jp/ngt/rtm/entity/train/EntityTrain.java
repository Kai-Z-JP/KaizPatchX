package jp.ngt.rtm.entity.train;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTrain extends EntityTrainBase {
	public EntityTrain(World world) {
		super(world);
	}

	public EntityTrain(World world, String s) {
		super(world, s);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}
}