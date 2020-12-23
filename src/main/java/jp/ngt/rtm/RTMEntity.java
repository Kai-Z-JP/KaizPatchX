package jp.ngt.rtm;

import cpw.mods.fml.common.registry.EntityRegistry;
import jp.ngt.rtm.entity.*;
import jp.ngt.rtm.entity.npc.EntityMotorman;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.entity.train.*;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.entity.train.parts.EntityContainer;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.train.parts.EntityTie;
import jp.ngt.rtm.entity.vehicle.EntityCar;
import jp.ngt.rtm.entity.vehicle.EntityPlane;
import jp.ngt.rtm.entity.vehicle.EntityShip;

public final class RTMEntity {
	public static final byte FREQ_VEHICLE = 3;
	private static final byte FREQ_INSTALLED = 10;

	private static short nextId;
	private static final short RANGE = 1024;//trackingRange->EntityTrackerで設定される

	public static void init(Object mod) {
		EntityRegistry.registerModEntity(EntityFloor.class, "RTM.E.Floor", getNextId(), mod, RANGE, FREQ_VEHICLE, false);
		EntityRegistry.registerModEntity(EntityBogie.class, "RTM.E.Bogie", getNextId(), mod, RANGE, FREQ_VEHICLE, false);
		EntityRegistry.registerModEntity(EntityMotorman.class, "RTM.E.Motorman", getNextId(), mod, RANGE, 3, true);
		EntityRegistry.registerModEntity(EntityATC.class, "RTM.E.ATC", getNextId(), mod, 160, FREQ_INSTALLED, false);
		EntityRegistry.registerModEntity(EntityTrainDetector.class, "RTM.E.TrainDetector", getNextId(), mod, 160, FREQ_INSTALLED, false);
		EntityRegistry.registerModEntity(EntityContainer.class, "RTM.E.Container", getNextId(), mod, 160, FREQ_VEHICLE, false);
		EntityRegistry.registerModEntity(EntityArtillery.class, "RTM.E.Artillery", getNextId(), mod, 160, FREQ_VEHICLE, false);
		EntityRegistry.registerModEntity(EntityBullet.class, "RTM.E.Bullet", getNextId(), mod, 256, 3, true);
		EntityRegistry.registerModEntity(EntityBumpingPost.class, "RTM.E.BumpingPost", getNextId(), mod, 160, FREQ_INSTALLED, false);
		EntityRegistry.registerModEntity(EntityTie.class, "RTM.E.Tie", getNextId(), mod, 160, 3, false);
		EntityRegistry.registerModEntity(EntityMMBoundingBox.class, "RTM.E.MMBB", getNextId(), mod, 160, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(EntityCar.class, "RTM.E.Car", getNextId(), mod, 160, FREQ_VEHICLE, true);
		EntityRegistry.registerModEntity(EntityShip.class, "RTM.E.Ship", getNextId(), mod, 160, FREQ_VEHICLE, true);
		EntityRegistry.registerModEntity(EntityPlane.class, "RTM.E.Plane", getNextId(), mod, 160, FREQ_VEHICLE, true);
		EntityRegistry.registerModEntity(EntityNPC.class, "RTM.E.NPC", getNextId(), mod, RANGE, 3, true);

		registerTrain(EntityTrain.class, "RTM.E.Train", mod);
		registerTrain(EntityFreightCar.class, "RTM.E.FreightCar", mod);
		registerTrain(EntityTanker.class, "RTM.E.Tanker", mod);
	}

	public static void registerTrain(Class<? extends EntityTrainBase> clazz, String name, Object mod) {
		EntityRegistry.registerModEntity(clazz, name, getNextId(), mod, RANGE, FREQ_VEHICLE, false);
	}

	public static int getNextId() {
		return nextId++;
	}
}