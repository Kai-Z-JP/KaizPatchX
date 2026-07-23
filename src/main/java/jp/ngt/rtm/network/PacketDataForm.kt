package jp.ngt.rtm.network

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.util.PermissionManager
import jp.ngt.rtm.modelpack.DataFormProvider
import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import jp.ngt.rtm.modelpack.cfg.DataFormValidator
import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataMap
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

class PacketDataForm() : IMessage, IMessageHandler<PacketDataForm, IMessage> {
    private var pos = IntArray(POSITION_COMPONENTS)
    private var values: Map<String, DataEntry<*>> = emptyMap()

    constructor(provider: DataFormProvider, values: Map<String, DataEntry<*>>) : this() {
        val providerPos = provider.pos
        require(providerPos.size >= POSITION_COMPONENTS) { "Data form provider position is invalid" }
        pos = providerPos.copyOf(POSITION_COMPONENTS)
        this.values = LinkedHashMap(values)
    }

    override fun toBytes(buffer: ByteBuf) {
        pos.forEach(buffer::writeInt)
        buffer.writeShort(values.size)
        values.forEach { (key, value) ->
            ByteBufUtils.writeUTF8String(buffer, key)
            val tag = NBTTagCompound()
            value.writeToNBT(tag)
            ByteBufUtils.writeTag(buffer, tag)
        }
    }

    override fun fromBytes(buffer: ByteBuf) {
        pos = IntArray(POSITION_COMPONENTS) { buffer.readInt() }
        val size = buffer.readUnsignedShort()
        if (size > DataFormConfig.MAX_FIELDS) {
            throw IllegalArgumentException("Too many data form values: $size")
        }

        val decoded = LinkedHashMap<String, DataEntry<*>>(size)
        repeat(size) {
            val key = ByteBufUtils.readUTF8String(buffer)
            val tag = ByteBufUtils.readTag(buffer)
                ?: throw IllegalArgumentException("Missing data form value")
            if (key.length > DataFormConfig.MAX_KEY_LENGTH) {
                throw IllegalArgumentException("Data form key is too long")
            }
            val value = DataEntry.getEntry(tag.getString("Type"), "", FORM_VALUE_FLAGS)
                ?: throw IllegalArgumentException("Unknown data form value type")
            value.readFromNBT(tag)
            if (decoded.put(key, value) != null) {
                throw IllegalArgumentException("Duplicate data form key: $key")
            }
        }
        values = decoded
    }

    override fun onMessage(message: PacketDataForm, ctx: MessageContext): IMessage? {
        val player = ctx.serverHandler.playerEntity
        val provider = message.resolveProvider(player.worldObj) ?: return null
        if (!message.isWithinReach(player, provider)) {
            return null
        }
        if (!PermissionManager.INSTANCE.hasPermission(player, provider.dataFormPermission)) {
            return null
        }

        val validation = DataFormValidator.validate(provider.dataFormConfig, message.values)
        if (!validation.isValid) {
            NGTLog.debug("[RTM] Rejected data form update: ${validation.error}")
            return null
        }

        val dataMap = provider.resourceState.dataMap
        validation.values.forEach { value ->
            dataMap.setEntry(value.key, value.entry, FORM_VALUE_FLAGS)
        }
        return null
    }

    private fun resolveProvider(world: World): DataFormProvider? {
        val target = if (pos[1] >= 0) {
            world.getTileEntity(pos[0], pos[1], pos[2])
        } else {
            world.getEntityByID(pos[0])
        }
        return target as? DataFormProvider
    }

    private fun isWithinReach(player: EntityPlayer, provider: DataFormProvider): Boolean =
        when (provider) {
            is Entity -> !provider.isDead && player.getDistanceSqToEntity(provider) <= MAX_DISTANCE_SQ
            is TileEntity -> provider.worldObj === player.worldObj && player.getDistanceSq(
                provider.xCoord + 0.5,
                provider.yCoord + 0.5,
                provider.zCoord + 0.5
            ) <= MAX_DISTANCE_SQ

            else -> false
        }

    companion object {
        private const val POSITION_COMPONENTS = 3
        private const val MAX_DISTANCE_SQ = 64.0
        private const val FORM_VALUE_FLAGS = DataMap.SYNC_FLAG or DataMap.SAVE_FLAG
    }
}
