package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.world.World;

public class PacketNBTHandlerServer implements IMessageHandler<PacketNBT, IMessage> {
	@Override
	public IMessage onMessage(PacketNBT message, MessageContext ctx) {
		if (!message.nbtData.getBoolean("ToClient")) {
			World world = ctx.getServerHandler().playerEntity.worldObj;
			message.onGetPacket(world);
		}
		return null;
	}
}