package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.modelpack.ModelPackManager;

/**
 * Serverで登録されたModelSetをClientと同期
 */
public class PacketModelSet implements IMessage, IMessageHandler<PacketModelSet, IMessage> {
    private int count;
    private String type;
    private String name;

    public PacketModelSet() {
    }

    public PacketModelSet(int par1Count, String par2Type, String par3Name) {
        this.count = par1Count;
        this.type = par2Type;
        this.name = par3Name;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.count);
        ByteBufUtils.writeUTF8String(buffer, this.type);
        ByteBufUtils.writeUTF8String(buffer, this.name);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.count = buffer.readInt();
        this.type = ByteBufUtils.readUTF8String(buffer);
        this.name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public IMessage onMessage(PacketModelSet message, MessageContext ctx) {
        ModelPackManager.INSTANCE.addModelSetName(message.count, message.type, message.name);
        return null;
    }
}