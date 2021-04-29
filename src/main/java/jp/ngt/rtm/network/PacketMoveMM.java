package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.entity.EntityMMBoundingBox;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.stream.IntStream;

public class PacketMoveMM implements IMessage, IMessageHandler<PacketMoveMM, IMessage> {
    private int[] entityIds;
    private double moveX;
    private double moveY;
    private double moveZ;

    public PacketMoveMM() {
    }

    public PacketMoveMM(int[] p1, double p2, double p3, double p4) {
        this.entityIds = p1;
        this.moveX = p2;
        this.moveY = p3;
        this.moveZ = p4;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.entityIds.length);
        Arrays.stream(this.entityIds).forEach(buffer::writeInt);
        buffer.writeDouble(this.moveX);
        buffer.writeDouble(this.moveY);
        buffer.writeDouble(this.moveZ);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int size = buffer.readInt();
        this.entityIds = new int[size];
        IntStream.range(0, size).forEach(i -> this.entityIds[i] = buffer.readInt());
        this.moveX = buffer.readDouble();
        this.moveY = buffer.readDouble();
        this.moveZ = buffer.readDouble();
    }

    @Override
    public IMessage onMessage(PacketMoveMM message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        if (world == null) {
            return null;
        }
        EntityMMBoundingBox.handleMMMovement(world, message.entityIds, message.moveX, message.moveY, message.moveZ);
        return null;
    }
}