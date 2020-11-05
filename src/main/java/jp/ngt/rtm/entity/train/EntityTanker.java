package jp.ngt.rtm.entity.train;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTanker extends EntityTrainBase {
	public EntityTanker(World world) {
		super(world);
	}

	public EntityTanker(World world, String s) {
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
}