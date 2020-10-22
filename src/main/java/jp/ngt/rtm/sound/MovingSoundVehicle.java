package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundVehicle extends MovingSoundEntity {
	protected boolean changePitch;

	/**
	 * @param train
	 * @param sound
	 * @param par3  リピート
	 * @param par4  ピッチ変更
	 */
	public MovingSoundVehicle(EntityVehicleBase vehicle, ResourceLocation sound, boolean par3, boolean par4) {
		super(vehicle, sound, par3);
		this.changePitch = par4;
	}

	@Override
	public void update() {
		super.update();
	}
}