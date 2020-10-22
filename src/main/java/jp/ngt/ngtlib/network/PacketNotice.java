package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketNotice implements IMessage {
	public static final byte Side_SERVER = 0;
	public static final byte Side_CLIENT = 1;
	public byte type;
	public String notice;

	public PacketNotice() {
	}

	public PacketNotice(byte par1, String par2) {
		this.type = par1;
		this.notice = par2;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(this.type);
		ByteBufUtils.writeUTF8String(buffer, this.notice);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.type = buffer.readByte();
		this.notice = ByteBufUtils.readUTF8String(buffer);
	}
}