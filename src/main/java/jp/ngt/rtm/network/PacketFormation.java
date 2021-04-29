package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationManager;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 編成データをClientへ送る
 */
public class PacketFormation implements IMessage, IMessageHandler<PacketFormation, IMessage> {
    private long formationId;
    private NBTTagCompound data;

    public PacketFormation() {
    }

    public PacketFormation(Formation par2) {
        this.formationId = par2.id;
        this.data = new NBTTagCompound();
        par2.writeToNBT(this.data, true);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(this.formationId);
        ByteBufUtils.writeTag(buffer, this.data);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.formationId = buffer.readLong();
        this.data = ByteBufUtils.readTag(buffer);
    }

    @Override
    public IMessage onMessage(PacketFormation message, MessageContext ctx) {
        Formation formation = Formation.readFromNBT(message.data, true);
        FormationManager.getInstance().setFormation(message.formationId, formation);
        return null;
    }
}