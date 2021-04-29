package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PacketSetTrainState implements IMessage, IMessageHandler<PacketSetTrainState, IMessage> {
    private int entityId;
    private int stateId;
    private byte stateData;

    public PacketSetTrainState() {
    }

    public PacketSetTrainState(EntityTrainBase train, int par2, byte par3) {
        this.entityId = train.getEntityId();
        this.stateId = par2;
        this.stateData = par3;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.stateId);
        buffer.writeByte(this.stateData);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.stateId = buffer.readInt();
        this.stateData = buffer.readByte();
    }

    @Override
    public IMessage onMessage(PacketSetTrainState message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        Entity entity = world.getEntityByID(message.entityId);
        if (entity instanceof EntityTrainBase) {
            ((EntityTrainBase) entity).setTrainStateData(message.stateId, message.stateData);
        }
        return null;
    }
}