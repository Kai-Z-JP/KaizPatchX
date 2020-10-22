package jp.ngt.rtm.entity.vehicle;

import jp.ngt.rtm.RTMItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityCar extends EntityVehicle {
	public EntityCar(World world) {
		super(world);
		this.stepHeight = 2.0F;
	}

	@Override
	public String getDefaultName() {
		return "CV33";
	}

	@Override
	protected ItemStack getVehicleItem() {
		return new ItemStack(RTMItem.itemVehicle, 1, 0);
	}
}