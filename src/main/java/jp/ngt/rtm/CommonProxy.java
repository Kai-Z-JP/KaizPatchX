package jp.ngt.rtm;

import cpw.mods.fml.relauncher.Side;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.entity.vehicle.IUpdateVehicle;
import jp.ngt.rtm.modelpack.ModelPackLoadThread;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.*;
import jp.ngt.rtm.modelpack.modelset.*;
import jp.ngt.rtm.network.PacketPlaySound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CommonProxy {
	private final FormationManager fm = new FormationManager(false);

	public void preInit() {
		ModelPackManager.INSTANCE.registerType("ModelFirearm", FirearmConfig.class, ModelSetFirearm.class);
		ModelPackManager.INSTANCE.registerType("ModelRail", RailConfig.class, ModelSetRail.class);
		ModelPackManager.INSTANCE.registerType("ModelSignal", SignalConfig.class, ModelSetSignal.class);
		ModelPackManager.INSTANCE.registerType("ModelTrain", TrainConfig.class, ModelSetTrain.class);
		ModelPackManager.INSTANCE.registerType("ModelContainer", ContainerConfig.class, ModelSetContainer.class);
		ModelPackManager.INSTANCE.registerType("ModelVehicle", VehicleConfig.class, ModelSetVehicle.class);
		ModelPackManager.INSTANCE.registerType("ModelNPC", NPCConfig.class, ModelSetNPC.class);
		ModelPackManager.INSTANCE.registerType("ModelMachine", MachineConfig.class, ModelSetMachine.class);
		ModelPackManager.INSTANCE.registerType("ModelWire", WireConfig.class, ModelSetWire.class);
		ModelPackManager.INSTANCE.registerType("ModelConnector", ConnectorConfig.class, ModelSetConnector.class);

		ModelPackLoadThread thread = new ModelPackLoadThread(Side.SERVER);
		thread.start();
	}

	public void init() {
	}

	public IUpdateVehicle getSoundUpdater(EntityVehicleBase par1) {
		return null;
	}

	/**
	 * @return 0:接続なし, 1:接続完了
	 */
	public byte getConnectionState() {
		return 1;
	}

	/**
	 * @param par1 : 0:接続なし, 1:接続完了
	 */
	public void setConnectionState(byte par1) {
	}

	public void spawnModParticle(World world, double x, double y, double z, double mX, double mY, double mZ) {
	}

	public void renderMissingModel() {
	}

	public float getFov(EntityPlayer player, float fov) {
		return 1.0F;
	}

	/**
	 * 音を鳴らす、リピートなし
	 *
	 * @param entity
	 * @param sound  null可
	 */
	public void playSound(Entity entity, ResourceLocation sound, float vol, float pitch) {
		if (sound != null) {
			RTMCore.NETWORK_WRAPPER.sendToAll(new PacketPlaySound(entity, sound, vol, pitch));
		}
	}

	public void playSound(TileEntity entity, ResourceLocation sound, float vol, float pitch) {
	}

	/**
	 * Sever/Clientでインスタンス分けて取得
	 */
	public FormationManager getFormationManager() {
		return this.fm;
	}
}