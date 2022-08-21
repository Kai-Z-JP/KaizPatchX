package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.network.PacketTileEntity;
import jp.ngt.rtm.electric.TileEntitySignalConverter;
import jp.ngt.rtm.electric.TileEntitySignalConverter.ComparatorType;
import net.minecraft.world.World;

public class PacketSignalConverter extends PacketTileEntity implements IMessageHandler<PacketSignalConverter, IMessage> {
    private int comparatorIndex;
    private int signal1, signal2;

    public PacketSignalConverter() {
    }

    public PacketSignalConverter(TileEntitySignalConverter par1, int par2, int par3, int par4) {
        super(par1);
        this.comparatorIndex = par2;
        this.signal1 = par3;
        this.signal2 = par4;
    }

    @Override
    protected void write(ByteBuf buffer) {
        buffer.writeInt(this.comparatorIndex);
        buffer.writeInt(this.signal1);
        buffer.writeInt(this.signal2);
    }

    @Override
    protected void read(ByteBuf buffer) {
        this.comparatorIndex = buffer.readInt();
        this.signal1 = buffer.readInt();
        this.signal2 = buffer.readInt();
    }

    @Override
    public IMessage onMessage(PacketSignalConverter message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        TileEntitySignalConverter tile = (TileEntitySignalConverter) message.getTileEntity(world);
        tile.setComparator(ComparatorType.getType(message.comparatorIndex));
        tile.setSignalLevel(message.signal1, message.signal2);
        tile.markDirty();
        world.markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        return null;
    }
}