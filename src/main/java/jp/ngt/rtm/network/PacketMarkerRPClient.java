package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketMarkerRPClient implements IMessage, IMessageHandler<PacketMarkerRPClient, IMessage> {
    private int x, y, z;
    private RailPosition[] railPositions;

    public PacketMarkerRPClient() {
    }

    public PacketMarkerRPClient(TileEntityMarker par4) {
//		this.x = par4.xCoord;
//		this.y = par4.yCoord;
//		this.z = par4.zCoord;
        this.railPositions = par4.getAllRP();
    }

    public PacketMarkerRPClient(int par1, int par2, int par3, TileEntityMarker par4) {
        this.x = par1;
        this.y = par2;
        this.z = par3;
        this.railPositions = par4.getAllRP();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);

        buffer.writeByte(this.railPositions.length);
        for (RailPosition rp : this.railPositions) {
            ByteBufUtils.writeTag(buffer, rp.writeToNBT());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();

        byte size = buffer.readByte();
        if (size > 0) {
            this.railPositions = new RailPosition[size];
            for (int i = 0; i < size; ++i) {
                NBTTagCompound nbt = ByteBufUtils.readTag(buffer);
                this.railPositions[i] = RailPosition.readFromNBT(nbt);
            }
        }
    }

    @Override
    public IMessage onMessage(PacketMarkerRPClient message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;

        if (message.railPositions != null) {
            for (RailPosition rp : message.railPositions) {
                TileEntity tile = world.getTileEntity(rp.blockX, rp.blockY, rp.blockZ);
                if (tile instanceof TileEntityMarker) {
                    ((TileEntityMarker) tile).setMarkerRP(rp);
                    if (((TileEntityMarker) tile).isCoreMarker()) {
                        RTMBlock.marker.onMarkerActivated(world, rp.blockX, rp.blockY, rp.blockZ, ctx.getServerHandler().playerEntity, false);
                    }
                }
            }
        }
        return null;
    }
}