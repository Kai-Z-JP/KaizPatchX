package jp.ngt.rtm.network

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.network.PacketCustom
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.modelpack.IModelSelector
import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataMap
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

class PacketDataMap : PacketCustom, IMessageHandler<PacketDataMap, IMessage> {
    private var operation = OP_SET
    private var namespace = ""
    private var key = ""
    private var flag = 0
    private var entryData = NBTTagCompound()

    constructor() : super()

    private constructor(
        entity: Entity,
        operation: Byte,
        namespace: String,
        key: String,
        flag: Int,
        entry: DataEntry<*>?
    ) : super(entity) {
        setup(operation, namespace, key, flag, entry)
    }

    private constructor(
        tileEntity: TileEntity,
        operation: Byte,
        namespace: String,
        key: String,
        flag: Int,
        entry: DataEntry<*>?
    ) : super(
        tileEntity
    ) {
        setup(operation, namespace, key, flag, entry)
    }

    private fun setup(operation: Byte, namespace: String, key: String, flag: Int, entry: DataEntry<*>?) {
        this.operation = operation
        this.namespace = namespace
        this.key = key
        this.flag = flag
        this.entryData = NBTTagCompound()
        entry?.writeToNBT(this.entryData)
        this.entryData.setInteger("Flag", flag)
    }

    override fun toBytes(buffer: ByteBuf) {
        super.toBytes(buffer)
        buffer.writeByte(operation.toInt())
        ByteBufUtils.writeUTF8String(buffer, namespace)
        ByteBufUtils.writeUTF8String(buffer, key)
        buffer.writeInt(flag)
        ByteBufUtils.writeTag(buffer, entryData)
    }

    override fun fromBytes(buffer: ByteBuf) {
        super.fromBytes(buffer)
        operation = buffer.readByte()
        namespace = ByteBufUtils.readUTF8String(buffer)
        key = ByteBufUtils.readUTF8String(buffer)
        flag = buffer.readInt()
        entryData = ByteBufUtils.readTag(buffer) ?: NBTTagCompound()
    }

    override fun onMessage(message: PacketDataMap, ctx: MessageContext): IMessage? {
        val world = if (ctx.side == Side.SERVER) {
            ctx.serverHandler.playerEntity.worldObj
        } else {
            NGTUtil.getClientWorld()
        }
        message.apply(world, ctx.side == Side.CLIENT)
        return null
    }

    private fun apply(world: World?, onClient: Boolean) {
        if (world == null) {
            return
        }

        val target = if (forEntity()) getEntity(world) else getTileEntity(world)
        if (target !is IModelSelector) {
            return
        }

        val applyFlag = getApplyFlag(onClient)
        val dataMap = target.resourceState.dataMap
        when (operation) {
            OP_REMOVE -> {
                dataMap.namespace(namespace).remove(key, applyFlag)
            }

            OP_SET -> {
                val type = entryData.getString("Type")
                val entry = DataEntry.getEntry(type, "", applyFlag) ?: return
                entry.readFromNBT(entryData)
                dataMap.setEntry(namespace, key, entry, applyFlag)
            }
        }

    }

    private fun getApplyFlag(onClient: Boolean): Int {
        val sync = if (onClient) 0 else flag and DataMap.SYNC_FLAG
        val save = flag and DataMap.SAVE_FLAG
        return sync or save
    }

    companion object {
        private const val OP_SET: Byte = 0
        private const val OP_REMOVE: Byte = 1

        @JvmStatic
        fun set(entity: Entity, namespace: String, key: String, value: DataEntry<*>) =
            PacketDataMap(entity, OP_SET, namespace, key, value.flag, value)

        @JvmStatic
        fun set(tileEntity: TileEntity, namespace: String, key: String, value: DataEntry<*>) =
            PacketDataMap(tileEntity, OP_SET, namespace, key, value.flag, value)

        @JvmStatic
        fun remove(entity: Entity, namespace: String, key: String, flag: Int) =
            PacketDataMap(entity, OP_REMOVE, namespace, key, flag, null)

        @JvmStatic
        fun remove(tileEntity: TileEntity, namespace: String, key: String, flag: Int) =
            PacketDataMap(tileEntity, OP_REMOVE, namespace, key, flag, null)
    }
}
