package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.rail.TileEntityMarker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PacketMarker implements IMessage, IMessageHandler<PacketMarker, IMessage> {
	private int x, y, z;
	private List<int[]> list;

	public PacketMarker() {
	}

	public PacketMarker(int par1, int par2, int par3, List<int[]> par4) {
		this.x = par1;
		this.y = par2;
		this.z = par3;
		this.list = par4;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);

		buffer.writeInt(this.list.size());
		for (int[] ia : this.list) {
			buffer.writeInt(ia[0]);
			buffer.writeInt(ia[1]);
			buffer.writeInt(ia[2]);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();

		int size = buffer.readInt();
		this.list = new ArrayList<int[]>();
		for (int i = 0; i < size; ++i) {
			int i0 = buffer.readInt();
			int i1 = buffer.readInt();
			int i2 = buffer.readInt();
			this.list.add(new int[]{i0, i1, i2});
		}
	}

	@Override
	public IMessage onMessage(PacketMarker message, MessageContext ctx) {
		World world = NGTUtil.getClientWorld();
		TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
		if (tile instanceof TileEntityMarker) {
			((TileEntityMarker) tile).setMarkersPos(message.list, true);
		}
		return null;
	}
}