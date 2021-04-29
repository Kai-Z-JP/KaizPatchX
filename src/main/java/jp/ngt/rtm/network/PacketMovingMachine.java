package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.network.PacketTileEntity;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import net.minecraft.world.World;

public class PacketMovingMachine extends PacketTileEntity implements IMessageHandler<PacketMovingMachine, IMessage> {
    private int w, h, d;
    private float s;
    private boolean v;

    public PacketMovingMachine() {
    }

    public PacketMovingMachine(TileEntityMovingMachine par1, int p2, int p3, int p4, float p5, boolean p6) {
        super(par1);
        this.w = p2;
        this.h = p3;
        this.d = p4;
        this.s = p5;
        this.v = p6;
    }

    @Override
    protected void write(ByteBuf buffer) {
        buffer.writeInt(this.w);
        buffer.writeInt(this.h);
        buffer.writeInt(this.d);
        buffer.writeFloat(this.s);
        buffer.writeBoolean(this.v);
    }

    @Override
    protected void read(ByteBuf buffer) {
        this.w = buffer.readInt();
        this.h = buffer.readInt();
        this.d = buffer.readInt();
        this.s = buffer.readFloat();
        this.v = buffer.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketMovingMachine message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        TileEntityMovingMachine tile = (TileEntityMovingMachine) message.getTileEntity(world);
        tile.setData(message.w, message.h, message.d, message.s, message.v);
        return null;
    }
}