package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.world.World;

public class PacketNBTHandlerClient implements IMessageHandler<PacketNBT, IMessage> {
	@Override
	public IMessage onMessage(PacketNBT message, MessageContext ctx) {
		if (message.nbtData == null) {
			return null;
		}

		if (message.nbtData.getBoolean("ToClient")) {
			World world = NGTUtil.getClientWorld();
			message.onGetPacket(world);
		}
		return null;
	}
}