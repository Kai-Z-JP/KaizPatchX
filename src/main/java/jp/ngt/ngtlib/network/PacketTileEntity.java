package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class PacketTileEntity implements IMessage {
	private int x, y, z;

	public PacketTileEntity() {
	}

	public PacketTileEntity(TileEntity tileEntity) {
		this.x = tileEntity.xCoord;
		this.y = tileEntity.yCoord;
		this.z = tileEntity.zCoord;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		this.write(buffer);
	}

	protected abstract void write(ByteBuf buffer);

	@Override
	public final void fromBytes(ByteBuf buffer) {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.read(buffer);
	}

	protected abstract void read(ByteBuf buffer);

	protected TileEntity getTileEntity(World world) {
		return world.getTileEntity(this.x, this.y, this.z);
	}
}