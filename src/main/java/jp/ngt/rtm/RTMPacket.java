package jp.ngt.rtm;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import jp.ngt.rtm.network.*;

public final class RTMPacket {
	private static short packetId;

	public static void init() {
		registerPacket(PacketLargeRailBase.class, PacketLargeRailBase.class, Side.CLIENT);
		registerPacket(PacketModelSet.class, PacketModelSet.class, Side.CLIENT);
		registerPacket(PacketPlaySound.class, PacketPlaySound.class, Side.CLIENT);
		registerPacket(PacketLargeRailCore.class, PacketLargeRailCore.class, Side.CLIENT);
		registerPacket(PacketNoticeHandlerClient.class, PacketNotice.class, Side.CLIENT);
		registerPacket(PacketNoticeHandlerServer.class, PacketNotice.class, Side.SERVER);
		registerPacket(PacketRTMKey.class, PacketRTMKey.class, Side.SERVER);
		registerPacket(PacketSelectModel.class, PacketSelectModel.class, Side.SERVER);
		registerPacket(PacketSignal.class, PacketSignal.class, Side.CLIENT);
		registerPacket(PacketWire.class, PacketWire.class, Side.CLIENT);
		registerPacket(PacketTextureHolder.class, PacketTextureHolder.class, Side.SERVER);
		registerPacket(PacketSetTrainState.class, PacketSetTrainState.class, Side.SERVER);
		registerPacket(PacketModelPack.class, PacketModelPack.class, Side.CLIENT);
		registerPacket(PacketVehicleMovement.class, PacketVehicleMovement.class, Side.CLIENT);
		registerPacket(PacketMarker.class, PacketMarker.class, Side.CLIENT);
		registerPacket(PacketMarkerRPClient.class, PacketMarkerRPClient.class, Side.SERVER);
		registerPacket(PacketFormation.class, PacketFormation.class, Side.CLIENT);
		registerPacket(PacketSignalConverter.class, PacketSignalConverter.class, Side.SERVER);
		registerPacket(PacketStationData.class, PacketStationData.class, Side.SERVER);
		registerPacket(PacketMovingMachine.class, PacketMovingMachine.class, Side.SERVER);
		registerPacket(PacketMoveMM.class, PacketMoveMM.class, Side.CLIENT);
		registerPacket(PacketSyncItem.class, PacketSyncItem.class, Side.SERVER);
	}

	public static <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
		RTMCore.NETWORK_WRAPPER.registerMessage(messageHandler, requestMessageType, packetId++, side);
	}
}
