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
    private int markerState;
    private RailPosition[] railPositions;

    public PacketMarkerRPClient() {
    }

    public PacketMarkerRPClient(TileEntityMarker par4) {
        this.railPositions = par4.getAllRP();
        this.markerState = par4.getMarkerState();
    }

    public PacketMarkerRPClient(int par1, int par2, int par3, TileEntityMarker par4) {
        this.railPositions = par4.getAllRP();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.markerState);

        buffer.writeByte(this.railPositions.length);
        for (RailPosition rp : this.railPositions) {
            ByteBufUtils.writeTag(buffer, rp.writeToNBT());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.markerState = buffer.readInt();

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
                    TileEntityMarker marker = (TileEntityMarker) tile;
                    marker.setMarkerRP(rp);
                    if (marker.isCoreMarker()) {
                        RTMBlock.marker.onMarkerActivated(world, rp.blockX, rp.blockY, rp.blockZ, ctx.getServerHandler().playerEntity, false);
                    }
                    marker.setMarkerState(message.markerState);
                    marker.markDirty();
                    world.markBlockForUpdate(rp.blockX, rp.blockY, rp.blockZ);
                }
            }
        }
        return null;
    }
}