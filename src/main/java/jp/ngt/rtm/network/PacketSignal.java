package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.TileEntitySignal;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketSignal implements IMessage, IMessageHandler<PacketSignal, IMessage> {
	private int x, y, z, level;

	public PacketSignal() {
	}

	public PacketSignal(int par1, int par2, int par3, int par4Level) {
		this.x = par1;
		this.y = par2;
		this.z = par3;
		this.level = par4Level;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeInt(this.level);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.level = buffer.readInt();
	}

	@Override
	public IMessage onMessage(PacketSignal message, MessageContext ctx) {
		World world = NGTUtil.getClientWorld();
		TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
		if (tile instanceof TileEntitySignal) {
			((TileEntitySignal) tile).setSignal(message.level);
		}
		return null;
	}
}