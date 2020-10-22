package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.EntityElectricalWiring;
import jp.ngt.rtm.electric.TileEntityDummyEW;
import jp.ngt.rtm.electric.TileEntityElectricalWiring;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketWire implements IMessage, IMessageHandler<PacketWire, IMessage> {
	public static final int ARRAY_SIZE = 4;

	private int x, y, z;
	private boolean isBlock, isActivated;
	private NBTTagCompound nbtData;

	public PacketWire() {
	}

	public PacketWire(TileEntityElectricalWiring tile) {
		this.x = tile.xCoord;
		this.y = tile.yCoord;
		this.z = tile.zCoord;
		this.isBlock = tile.isBlockTile();
		if (!this.isBlock && tile instanceof TileEntityDummyEW) {
			this.x = ((TileEntityDummyEW) tile).entityEW.getEntityId();
			this.y = 0;
			this.z = 0;
		}
		this.isActivated = tile.isActivated;
		this.nbtData = new NBTTagCompound();
		tile.writeToNBT(this.nbtData);
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeBoolean(this.isBlock);
		buffer.writeBoolean(this.isActivated);
		ByteBufUtils.writeTag(buffer, this.nbtData);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.isBlock = buffer.readBoolean();
		this.isActivated = buffer.readBoolean();
		this.nbtData = ByteBufUtils.readTag(buffer);
	}

	@Override
	public IMessage onMessage(PacketWire message, MessageContext ctx) {
		World world = NGTUtil.getClientWorld();
		TileEntityElectricalWiring tile = null;
		if (message.isBlock) {
			TileEntity tile1 = world.getTileEntity(message.x, message.y, message.z);
			if (tile1 instanceof TileEntityElectricalWiring) {
				tile = (TileEntityElectricalWiring) tile1;
			}
		} else {
			Entity entity = world.getEntityByID(message.x);
			if (entity != null && entity instanceof EntityElectricalWiring) {
				tile = ((EntityElectricalWiring) entity).tileEW;
			}
		}

		if (tile != null) {
			tile.isActivated = message.isActivated;
			tile.readFromNBT(message.nbtData);
		}
		return null;
	}
}