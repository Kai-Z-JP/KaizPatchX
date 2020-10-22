package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketLargeRailBase implements IMessage, IMessageHandler<PacketLargeRailBase, IMessage> {
	private int x, y, z;
	private int sX, sY, sZ;

	public PacketLargeRailBase() {
	}

	public PacketLargeRailBase(TileEntityLargeRailBase tile) {
		this.x = tile.xCoord;
		this.y = tile.yCoord;
		this.z = tile.zCoord;
		this.sX = tile.getStartPoint()[0];
		this.sY = tile.getStartPoint()[1];
		this.sZ = tile.getStartPoint()[2];
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeInt(this.sX);
		buffer.writeInt(this.sY);
		buffer.writeInt(this.sZ);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.sX = buffer.readInt();
		this.sY = buffer.readInt();
		this.sZ = buffer.readInt();
	}

	@Override
	public IMessage onMessage(PacketLargeRailBase message, MessageContext ctx) {
		World world = NGTUtil.getClientWorld();
		TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
		if (tile instanceof TileEntityLargeRailBase) {
			TileEntityLargeRailBase tile0 = (TileEntityLargeRailBase) tile;
			tile0.setStartPoint(message.sX, message.sY, message.sZ);
		}
		return null;
	}
}