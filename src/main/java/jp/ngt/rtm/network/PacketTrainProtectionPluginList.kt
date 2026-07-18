package jp.ngt.rtm.network

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import jp.ngt.rtm.entity.train.protection.TrainProtectionPluginInfo
import jp.ngt.rtm.entity.train.protection.TrainProtectionPluginManager

class PacketTrainProtectionPluginList() : IMessage, IMessageHandler<PacketTrainProtectionPluginList, IMessage> {
    private var plugins: List<TrainProtectionPluginInfo> = emptyList()

    constructor(plugins: List<TrainProtectionPluginInfo>) : this() {
        this.plugins = plugins
    }

    override fun toBytes(buffer: ByteBuf) {
        buffer.writeInt(plugins.size)
        for (plugin in plugins) {
            ByteBufUtils.writeUTF8String(buffer, plugin.id)
            ByteBufUtils.writeUTF8String(buffer, plugin.displayName)
            buffer.writeBoolean(plugin.defaultEnabled)
            buffer.writeBoolean(plugin.hidden)
        }
    }

    override fun fromBytes(buffer: ByteBuf) {
        val size = buffer.readInt()
        val entries = ArrayList<TrainProtectionPluginInfo>(size)
        for (i in 0 until size) {
            entries.add(
                TrainProtectionPluginInfo(
                    ByteBufUtils.readUTF8String(buffer),
                    ByteBufUtils.readUTF8String(buffer),
                    buffer.readBoolean(),
                    buffer.readBoolean()
                )
            )
        }
        plugins = entries
    }

    override fun onMessage(message: PacketTrainProtectionPluginList, ctx: MessageContext): IMessage? {
        TrainProtectionPluginManager.setSyncedPluginInfos(message.plugins)
        return null
    }
}
