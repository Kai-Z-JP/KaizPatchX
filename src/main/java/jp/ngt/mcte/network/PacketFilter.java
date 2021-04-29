package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.editor.filter.FilterManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PacketFilter implements IMessage, IMessageHandler<PacketFilter, IMessage> {
    private String playerName;
    private String filterName;
    private String cfgData;
    private String script;

    public PacketFilter() {
    }

    public PacketFilter(EntityPlayer player, String name, String data, String script) {
        this.playerName = player.getCommandSenderName();
        this.filterName = name;
        this.cfgData = data;
        this.script = script;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.playerName);
        ByteBufUtils.writeUTF8String(buffer, this.filterName);
        ByteBufUtils.writeUTF8String(buffer, this.cfgData);
        ByteBufUtils.writeUTF8String(buffer, this.script);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.playerName = ByteBufUtils.readUTF8String(buffer);
        this.filterName = ByteBufUtils.readUTF8String(buffer);
        this.cfgData = ByteBufUtils.readUTF8String(buffer);
        this.script = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public IMessage onMessage(PacketFilter message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        EntityPlayer player = world.getPlayerEntityByName(message.playerName);
        FilterManager.INSTANCE.execFilter(player, message.filterName, message.cfgData, message.script);
        return null;
    }
}