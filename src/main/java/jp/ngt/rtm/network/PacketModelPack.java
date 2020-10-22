package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.ModelPackWriter;

import java.nio.ByteBuffer;

public class PacketModelPack implements IMessage, IMessageHandler<PacketModelPack, IMessage> {
	public static final ModelPackWriter writer = new ModelPackWriter();

	private String name;
	private long size;
	private byte[] bytes;

	public PacketModelPack() {
	}

	public PacketModelPack(String par1, long par2, ByteBuffer par3) {
		this.name = par1;
		this.size = par2;
		this.bytes = par3.array();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeUTF8String(buffer, this.name);
		buffer.writeLong(this.size);
		buffer.writeBytes(this.bytes);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.name = ByteBufUtils.readUTF8String(buffer);
		this.size = buffer.readLong();
		this.readBytesFromBuffer(buffer);
	}

	private void readBytesFromBuffer(ByteBuf buffer) {
		int index = RTMCore.PacketSize;
		int i0 = buffer.writerIndex() - buffer.readerIndex();
		if (index > i0) {
			index = i0;
		}
		this.bytes = buffer.readBytes(index).array();
	}

	@Override
	public IMessage onMessage(PacketModelPack message, MessageContext ctx) {
		writer.onPacket(message.name, message.size, message.bytes);
		return null;
	}
}