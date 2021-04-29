package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.util.NGTCertificate;

public class PacketNoticeHandlerClient implements IMessageHandler<PacketNotice, IMessage> {
    @Override
    public IMessage onMessage(PacketNotice message, MessageContext ctx) {
        if (message.type == PacketNotice.Side_CLIENT) {
            if (message.notice.equals("regKey")) {
                NGTCertificate.writePlayerData(NGTCore.proxy.getUserName());
            }
        }
        return null;
    }
}