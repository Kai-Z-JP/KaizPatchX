package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.VehicleConfig;

public class ModelSetVehicle extends ModelSetVehicleBase<VehicleConfig> {
	public ModelSetVehicle() {
		super();
	}

	public ModelSetVehicle(VehicleConfig par1) {
		super(par1);
	}

	@Override
	public VehicleConfig getDummyConfig() {
		return VehicleConfig.getDummy();
	}
}