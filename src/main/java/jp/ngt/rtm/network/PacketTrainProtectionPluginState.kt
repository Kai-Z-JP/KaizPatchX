package jp.ngt.rtm.network

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityTrainBase

class PacketTrainProtectionPluginState() : IMessage, IMessageHandler<PacketTrainProtectionPluginState, IMessage> {
    private var entityId = 0
    private var pluginId = ""
    private var enabled = false

    constructor(train: EntityTrainBase, pluginId: String, enabled: Boolean) : this() {
        this.entityId = train.entityId
        this.pluginId = pluginId
        this.enabled = enabled
    }

    override fun toBytes(buffer: ByteBuf) {
        buffer.writeInt(entityId)
        ByteBufUtils.writeUTF8String(buffer, pluginId)
        buffer.writeBoolean(enabled)
    }

    override fun fromBytes(buffer: ByteBuf) {
        entityId = buffer.readInt()
        pluginId = ByteBufUtils.readUTF8String(buffer)
        enabled = buffer.readBoolean()
    }

    override fun onMessage(message: PacketTrainProtectionPluginState, ctx: MessageContext): IMessage? {
        val entity = NGTUtil.getClientWorld()?.getEntityByID(message.entityId)
        if (entity is EntityTrainBase) {
            entity.applyProtectionPluginState(message.pluginId, message.enabled)
        }
        return null
    }

    companion object {
        @JvmStatic
        fun sendToClient(train: EntityTrainBase, pluginId: String, enabled: Boolean) {
            RTMCore.NETWORK_WRAPPER.sendToAll(PacketTrainProtectionPluginState(train, pluginId, enabled))
        }
    }
}
