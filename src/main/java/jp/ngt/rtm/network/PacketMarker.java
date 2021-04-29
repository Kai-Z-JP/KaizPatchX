package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.network.PacketCustom;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.rail.TileEntityMarker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PacketMarker extends PacketCustom implements IMessageHandler<PacketMarker, IMessage> {
    private List<int[]> list;

    public PacketMarker() {
    }

    public PacketMarker(TileEntityMarker marker, List<int[]> par4) {
        super(marker);
        this.list = par4;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeInt(this.list.size());
        this.list.forEach(ia -> {
            buffer.writeInt(ia[0]);
            buffer.writeInt(ia[1]);
            buffer.writeInt(ia[2]);
        });
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);

        int size = buffer.readInt();
        this.list = new ArrayList<>();
        IntStream.range(0, size).forEach(i -> {
            int i0 = buffer.readInt();
            int i1 = buffer.readInt();
            int i2 = buffer.readInt();
            this.list.add(new int[]{i0, i1, i2});
        });
    }

    @Override
    public IMessage onMessage(PacketMarker message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        TileEntity tile = message.getTileEntity(world);
        if (tile instanceof TileEntityMarker) {
            ((TileEntityMarker) tile).setMarkersPos(message.list);
        }
        return null;
    }
}