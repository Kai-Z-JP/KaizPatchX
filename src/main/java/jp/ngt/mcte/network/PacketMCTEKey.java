package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.MCTEKeyHandlerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PacketMCTEKey implements IMessage, IMessageHandler<PacketMCTEKey, IMessage> {
    private String playerName;
    private byte keyId;

    public PacketMCTEKey() {
    }

    public PacketMCTEKey(EntityPlayer par1Entity, byte par2Key) {
        this.playerName = par1Entity.getCommandSenderName();
        this.keyId = par2Key;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.playerName);
        buffer.writeByte(this.keyId);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.playerName = ByteBufUtils.readUTF8String(buffer);
        this.keyId = buffer.readByte();
    }

    @Override
    public IMessage onMessage(PacketMCTEKey message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        EntityPlayer player = world.getPlayerEntityByName(message.playerName);
        MCTEKeyHandlerServer.INSTANCE.onKeyDown(player, message.keyId);
        return null;
    }
}