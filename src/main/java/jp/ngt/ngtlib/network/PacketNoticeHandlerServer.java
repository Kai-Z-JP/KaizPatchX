package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketNoticeHandlerServer implements IMessageHandler<PacketNotice, IMessage> {
	@Override
	public IMessage onMessage(PacketNotice message, MessageContext ctx) {
		if (message.type == PacketNotice.Side_SERVER) {
			if (message.notice.equals("isConnected")) {
			}
		}
		return null;
	}
}