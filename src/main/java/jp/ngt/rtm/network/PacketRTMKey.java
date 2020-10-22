package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.event.RTMKeyHandlerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PacketRTMKey implements IMessage, IMessageHandler<PacketRTMKey, IMessage> {
	private String playerName;
	private byte keyId;
	private String sound;

	public PacketRTMKey() {
	}

	public PacketRTMKey(EntityPlayer par1Entity, byte par2Key, String par3Sound) {
		this.playerName = par1Entity.getCommandSenderName();
		this.keyId = par2Key;
		this.sound = par3Sound;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeUTF8String(buffer, this.playerName);
		buffer.writeByte(this.keyId);
		ByteBufUtils.writeUTF8String(buffer, this.sound);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.playerName = ByteBufUtils.readUTF8String(buffer);
		this.keyId = buffer.readByte();
		this.sound = ByteBufUtils.readUTF8String(buffer);
	}

	@Override
	public IMessage onMessage(PacketRTMKey message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;
		EntityPlayer player = world.getPlayerEntityByName(message.playerName);
		RTMKeyHandlerServer.INSTANCE.onKeyDown(player, message.keyId, message.sound);
		return null;
	}
}