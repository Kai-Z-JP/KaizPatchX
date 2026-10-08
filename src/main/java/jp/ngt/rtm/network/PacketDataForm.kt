package jp.ngt.rtm.network

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.util.PermissionManager
import jp.ngt.rtm.modelpack.DataFormProvider
import jp.ngt.rtm.modelpack.cfg.DataFormOperation
import jp.ngt.rtm.modelpack.cfg.DataFormOperationType
import jp.ngt.rtm.modelpack.cfg.DataFormPathSegment
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
    private var operations: List<DataFormOperation> = emptyList()

    constructor(provider: DataFormProvider, operations: List<DataFormOperation>) : this() {
        require(provider.pos.size >= POSITION_COMPONENTS) { "Data form provider position is invalid" }
        pos = provider.pos.copyOf(POSITION_COMPONENTS)
        this.operations = operations.toList()
    }

    override fun toBytes(buffer: ByteBuf) {
        pos.forEach(buffer::writeInt)
        buffer.writeInt(operations.size)
        operations.forEach { operation ->
            buffer.writeByte(operation.type.ordinal)
            ByteBufUtils.writeUTF8String(buffer, operation.rootKey)
            buffer.writeInt(operation.path.size)
            operation.path.forEach { segment ->
                when (segment) {
                    is DataFormPathSegment.Key -> {
                        buffer.writeByte(KEY_SEGMENT)
                        ByteBufUtils.writeUTF8String(buffer, segment.value)
                    }

                    is DataFormPathSegment.Element -> {
                        buffer.writeByte(ELEMENT_SEGMENT)
                        buffer.writeInt(segment.index)
                    }

                    DataFormPathSegment.Index -> throw IllegalArgumentException("Wildcard path cannot be sent")
                }
            }
            when (operation.type) {
                DataFormOperationType.SET -> {
                    val tag = NBTTagCompound()
                    requireNotNull(operation.entry) { "SET operation has no value" }.writeToNBT(tag)
                    ByteBufUtils.writeTag(buffer, tag)
                }

                DataFormOperationType.LIST_INSERT, DataFormOperationType.LIST_REMOVE ->
                    buffer.writeInt(operation.index)
            }
        }
    }

    override fun fromBytes(buffer: ByteBuf) {
        pos = IntArray(POSITION_COMPONENTS) { buffer.readInt() }
        val size = buffer.readInt()
        require(size >= 0) { "Invalid data form operation count" }
        operations = List(size) { readOperation(buffer) }
    }

    private fun readOperation(buffer: ByteBuf): DataFormOperation {
        val type = DataFormOperationType.entries.getOrNull(buffer.readUnsignedByte().toInt())
            ?: throw IllegalArgumentException("Unknown data form operation")
        val rootKey = ByteBufUtils.readUTF8String(buffer)
        require(rootKey.isNotEmpty()) { "Invalid data form root key" }
        val pathSize = buffer.readInt()
        require(pathSize >= 0) { "Invalid data form path size" }
        val path = List(pathSize) {
            when (buffer.readUnsignedByte().toInt()) {
                KEY_SEGMENT -> DataFormPathSegment.Key(ByteBufUtils.readUTF8String(buffer).also { key ->
                    require(key.isNotEmpty()) { "Invalid data form path key" }
                })

                ELEMENT_SEGMENT -> DataFormPathSegment.Element(buffer.readInt())
                else -> throw IllegalArgumentException("Unknown data form path segment")
            }
        }
        return when (type) {
            DataFormOperationType.SET -> {
                val tag = ByteBufUtils.readTag(buffer)
                    ?: throw IllegalArgumentException("Missing data form value")
                val entry = DataEntry.getEntry(tag.getString("Type"), "", FORM_VALUE_FLAGS)
                    ?: throw IllegalArgumentException("Unknown data form value type")
                entry.readFromNBT(tag)
                DataFormOperation.set(rootKey, path, entry)
            }

            DataFormOperationType.LIST_INSERT -> DataFormOperation.insert(rootKey, path, buffer.readInt())
            DataFormOperationType.LIST_REMOVE -> DataFormOperation.remove(rootKey, path, buffer.readInt())
        }
    }

    override fun onMessage(message: PacketDataForm, ctx: MessageContext): IMessage? {
        val player = ctx.serverHandler.playerEntity
        val provider = message.resolveProvider(player.worldObj) ?: return null
        if (!message.isWithinReach(player, provider)) return null
        if (!PermissionManager.INSTANCE.hasPermission(player, provider.dataFormPermission)) return null

        val dataMap = provider.resourceState.dataMap
        val validation = DataFormValidator.validate(
            provider.dataFormConfig,
            message.operations,
            dataMap.getEntries()
        )
        if (!validation.isValid) {
            NGTLog.debug("[RTM] Rejected data form update: ${validation.error}")
            return null
        }
        validation.entries.forEach { (key, entry) -> dataMap.setEntry(key, entry, FORM_VALUE_FLAGS) }
        return null
    }

    private fun resolveProvider(world: World): DataFormProvider? {
        val target = if (pos[1] >= 0) world.getTileEntity(pos[0], pos[1], pos[2])
        else world.getEntityByID(pos[0])
        return target as? DataFormProvider
    }

    private fun isWithinReach(player: EntityPlayer, provider: DataFormProvider): Boolean = when (provider) {
        is Entity -> !provider.isDead && player.getDistanceSqToEntity(provider) <= INTERACTION_DISTANCE_SQ
        is TileEntity -> provider.worldObj === player.worldObj && player.getDistanceSq(
            provider.xCoord + 0.5, provider.yCoord + 0.5, provider.zCoord + 0.5
        ) <= INTERACTION_DISTANCE_SQ

        else -> false
    }

    companion object {
        private const val POSITION_COMPONENTS = 3
        private const val KEY_SEGMENT = 0
        private const val ELEMENT_SEGMENT = 1
        private const val INTERACTION_DISTANCE_SQ = 64.0
        private const val FORM_VALUE_FLAGS = DataMap.SYNC_FLAG or DataMap.SAVE_FLAG
    }
}
