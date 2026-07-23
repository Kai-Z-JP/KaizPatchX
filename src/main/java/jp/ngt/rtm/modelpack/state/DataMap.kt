package jp.ngt.rtm.modelpack.state

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.math.Vec3
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.network.PacketDataMap
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.WorldServer
import java.util.regex.Pattern

class DataMap {
    private val map = HashMap<DataKey, DataEntry<*>>()
    private var entity: Any? = null
    private var dataFormatter = DataFormatter(null)

    fun setEntity(par1: Any?) {
        entity = par1
    }

    fun setFormatter(formatter: DataFormatter?) {
        dataFormatter = formatter ?: DataFormatter(null)
    }

    fun readFromNBT(nbt: NBTTagCompound) {
        val isServer = NGTUtil.isServer()

        val list = nbt.getTagList("DataList", 10)
        for (i in 0 until list.tagCount()) {
            val entry = list.getCompoundTagAt(i)
            val type = entry.getString("Type")
            val name = entry.getString("Name")
            val namespace = entry.getString("Namespace")
            val key = DataKey.of(namespace, name)
            val flag = entry.getInteger("Flag")
            if (isServer && flag and SAVE_FLAG == 0) {
                continue
            }

            val dataEntry = DataEntry.getEntry(type, "", flag) ?: continue
            dataEntry.readFromNBT(entry)
            set(key, dataEntry, flag)
        }
    }

    fun writeToNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()

        val list = NBTTagList()
        map.forEach { (key, value) ->
            val entryTag = NBTTagCompound()
            if (key.namespace.isNotEmpty()) {
                entryTag.setString("Namespace", key.namespace)
            }
            entryTag.setString("Name", key.name)
            entryTag.setInteger("Flag", value.flag and SAVE_FLAG)
            value.writeToNBT(entryTag)
            list.appendTag(entryTag)
        }
        nbt.setTag("DataList", list)
        return nbt
    }

    private fun sendPacket(key: DataKey, value: DataEntry<*>, toClient: Boolean) {
        val target = entity

        if (target is Entity) {
            val packet = PacketDataMap.set(target, key.namespace, key.name, value)
            if (toClient) {
                if (target.worldObj is WorldServer) {
                    (target.worldObj as WorldServer)
                        .entityTracker
                        .getTrackingPlayers(target)
                        .forEach { player -> RTMCore.NETWORK_WRAPPER.sendTo(packet, player as EntityPlayerMP) }
                }
                return
            } else {
                RTMCore.NETWORK_WRAPPER.sendToServer(packet)
            }
        } else if (target is TileEntity) {
            if (toClient) {
                if (target.worldObj is WorldServer) {
                    target.markDirty()
                    target.worldObj.markBlockForUpdate(target.xCoord, target.yCoord, target.zCoord)
                }
                return
            } else {
                RTMCore.NETWORK_WRAPPER.sendToServer(PacketDataMap.set(target, key.namespace, key.name, value))
            }
        } else {
            return
        }
    }

    private fun sendRemovePacket(key: DataKey, flag: Int, toClient: Boolean) {
        val target = entity

        if (target is Entity) {
            val packet = PacketDataMap.remove(target, key.namespace, key.name, flag)
            if (toClient) {
                if (target.worldObj is WorldServer) {
                    (target.worldObj as WorldServer)
                        .entityTracker
                        .getTrackingPlayers(target)
                        .forEach { player -> RTMCore.NETWORK_WRAPPER.sendTo(packet, player as EntityPlayerMP) }
                }
                return
            } else {
                RTMCore.NETWORK_WRAPPER.sendToServer(packet)
            }
        } else if (target is TileEntity) {
            if (toClient) {
                if (target.worldObj is WorldServer) {
                    target.markDirty()
                    target.worldObj.markBlockForUpdate(target.xCoord, target.yCoord, target.zCoord)
                }
                return
            } else {
                RTMCore.NETWORK_WRAPPER.sendToServer(PacketDataMap.remove(target, key.namespace, key.name, flag))
            }
        } else {
            return
        }
    }

    fun contains(key: String) = rootNamespace().contains(key)

    @Suppress("UNCHECKED_CAST")
    private fun <T : DataEntry<*>> get(key: DataKey) = map[key] as? T

    fun setEntry(namespace: String, key: String, value: DataEntry<*>, flag: Int) =
        set(DataKey.of(namespace, key), value, flag)

    private fun set(key: DataKey, value: DataEntry<*>, flag: Int = value.flag) {
        if (!dataFormatter.check(key.toCompatKey(), value)) {
            NGTLog.debug("Invalid data : %s=%s", key.toCompatKey(), value.toString())
            return
        }

        val sync = flag and SYNC_FLAG != 0
        val onServerSide = sync && NGTUtil.isServer()
        if (!sync || onServerSide || entity == null || entity is Item) {
            map[key] = value
        }

        if (sync) {
            sendPacket(key, value, onServerSide)
        }
    }

    fun remove(key: String, flag: Int) = rootNamespace().remove(key, flag)

    private fun remove(key: DataKey, flag: Int): Boolean {
        val sync = flag and SYNC_FLAG != 0
        val onServerSide = sync && NGTUtil.isServer()
        var removed = false
        if (!sync || onServerSide || entity == null || entity is Item) {
            removed = map.remove(key) != null
        }

        if (sync) {
            sendRemovePacket(key, flag, onServerSide)
        }
        return removed
    }

    fun namespace(namespace: String) = NamespaceView(this, namespace)

    private fun rootNamespace() = NamespaceView(this, parseCompatKey = true)

    /**
     * コマンドから使用想定
     *
     * @param key
     * @param value "(type)hoge" で型強制
     * @param flag
     */
    fun set(key: String, value: String, flag: Int) = rootNamespace().set(key, value, flag)

    private fun set(key: DataKey, value: String, flag: Int): Boolean {
        val matcher = VAL_TYPE.matcher(value)
        val entry: DataEntry<*>? = if (matcher.find()) {
            val type = matcher.group().replace("(", "").replace(")", "")
            val val2 = value.substring(matcher.end())
            DataEntry.getEntry(type, val2, flag)
        } else {
            val current = get<DataEntry<*>>(key) ?: return false
            if (current is DataEntryList) {
                DataEntryList.fromString(value, current.elementType, flag)
            } else {
                DataEntry.getEntry(current.type.key, value, flag)
            }
        }

        return if (entry != null) {
            set(key, entry, flag)
            true
        } else {
            NGTLog.debug("[DataMap] Invalid Data (Key:%s, Value:%s)", key, value)
            false
        }
    }

    fun getEntries(): MutableMap<String, DataEntry<*>> {
        val entries = HashMap<String, DataEntry<*>>()
        map.forEach { (key, value) -> entries[key.toCompatKey()] = value }
        return entries
    }

    fun getArg() =
        map.entries.joinToString(",") { (key, value) -> "${key.toCompatKey()}=(${value.typeName})$value" }

    fun setArg(par1: String?, overwrite: Boolean) {
        setArg(par1, overwrite, SYNC_FLAG or SAVE_FLAG)
    }

    fun setArg(par1: String?, overwrite: Boolean, flag: Int) {
        val array = convertArg(par1)
        array
            .filter { sa -> !contains(sa[0]) || overwrite }
            .forEach { sa -> set(sa[0], "(${sa[1]})${sa[2]}", flag) }
    }

    fun getInt(key: String) = rootNamespace().getInt(key)
    fun setInt(key: String, value: Int, flag: Int) = rootNamespace().setInt(key, value, flag)
    fun getDouble(key: String) = rootNamespace().getDouble(key)
    fun setDouble(key: String, value: Double, flag: Int) = rootNamespace().setDouble(key, value, flag)
    fun getBoolean(key: String) = rootNamespace().getBoolean(key)
    fun setBoolean(key: String, value: Boolean, flag: Int) = rootNamespace().setBoolean(key, value, flag)
    fun getString(key: String) = rootNamespace().getString(key)
    fun setString(key: String, value: String, flag: Int) = rootNamespace().setString(key, value, flag)
    fun getVec(key: String) = rootNamespace().getVec(key)
    fun setVec(key: String, value: Vec3, flag: Int) = rootNamespace().setVec(key, value, flag)
    fun getHex(key: String) = rootNamespace().getHex(key)
    fun setHex(key: String, value: Int, flag: Int) = rootNamespace().setHex(key, value, flag)
    fun getList(key: String) = rootNamespace().getList(key)
    fun getArray(key: String) = rootNamespace().getArray(key)
    fun getListElementType(key: String) = rootNamespace().getListElementType(key)

    fun setList(key: String, value: Collection<*>, dataType: DataType, flag: Int) =
        rootNamespace().setList(key, value, dataType, flag)

    inline fun <reified T> setList(key: String, value: Collection<T>, flag: Int) =
        setList(key, value, DataType.inferElementType<T>(), flag)

    fun setArray(key: String, value: Array<*>, dataType: DataType, flag: Int) =
        rootNamespace().setList(key, value.toList(), dataType, flag)

    inline fun <reified T> setArray(key: String, value: Array<T>, flag: Int) =
        setList(key, value.toList(), flag)

    class NamespaceView internal constructor(
        private val dataMap: DataMap,
        namespace: String = "",
        private val parseCompatKey: Boolean = false
    ) {
        val namespace: String = normalizeNamespace(namespace)

        private fun keyOf(key: String) =
            if (parseCompatKey) DataKey.parseCompatKey(key) else DataKey.of(namespace, key)

        fun getKey(key: String) = keyOf(key).toCompatKey()

        fun contains(key: String) = dataMap.map.containsKey(keyOf(key))

        fun clear(flag: Int): Int {
            if (namespace.isEmpty()) {
                return 0
            }

            val keys = dataMap.map.keys.filter { key -> key.namespace == namespace }
            keys.forEach { key -> dataMap.remove(key, flag) }
            return keys.size
        }

        fun remove(key: String, flag: Int) = dataMap.remove(keyOf(key), flag)

        fun set(key: String, value: String, flag: Int) = dataMap.set(keyOf(key), value, flag)

        private inline fun <reified T : DataEntry<V>, V> getValue(key: String, defaultValue: V): V {
            return try {
                dataMap.get<T>(keyOf(key))?.get() ?: defaultValue
            } catch (_: Exception) {
                NGTLog.debug("%s is not %s", getKey(key), T::class.java.simpleName)
                defaultValue
            }
        }

        fun getInt(key: String): Int = getValue<DataEntryInt, Int>(key, 0)
        fun setInt(key: String, value: Int, flag: Int) = dataMap.set(keyOf(key), DataEntryInt(value, flag))
        fun getDouble(key: String): Double = getValue<DataEntryDouble, Double>(key, 0.0)
        fun setDouble(key: String, value: Double, flag: Int) = dataMap.set(keyOf(key), DataEntryDouble(value, flag))
        fun getBoolean(key: String): Boolean = getValue<DataEntryBoolean, Boolean>(key, false)
        fun setBoolean(key: String, value: Boolean, flag: Int) = dataMap.set(keyOf(key), DataEntryBoolean(value, flag))
        fun getString(key: String): String = getValue<DataEntryString, String>(key, "")
        fun setString(key: String, value: String, flag: Int) = dataMap.set(keyOf(key), DataEntryString(value, flag))
        fun getVec(key: String): Vec3 = getValue<DataEntryVec, Vec3>(key, Vec3.ZERO)
        fun setVec(key: String, value: Vec3, flag: Int) = dataMap.set(keyOf(key), DataEntryVec(value, flag))
        fun getHex(key: String): Int = getValue<DataEntryHex, Int>(key, 0)
        fun setHex(key: String, value: Int, flag: Int) = dataMap.set(keyOf(key), DataEntryHex(value, flag))
        fun getList(key: String): List<Any> =
            dataMap.get<DataEntryList>(keyOf(key))?.get()?.toList() ?: emptyList()

        fun getArray(key: String): Array<Any> = getList(key).toTypedArray()
        fun getListElementType(key: String): DataType? = dataMap.get<DataEntryList>(keyOf(key))?.elementType

        fun setList(key: String, value: Collection<*>, dataType: DataType, flag: Int) =
            dataMap.set(keyOf(key), DataEntryList.fromValues(dataType, value, flag))

        inline fun <reified T> setList(key: String, value: Collection<T>, flag: Int) =
            setList(key, value, DataType.inferElementType<T>(), flag)

        fun setArray(key: String, value: Array<*>, dataType: DataType, flag: Int) =
            setList(key, value.toList(), dataType, flag)

        inline fun <reified T> setArray(key: String, value: Array<T>, flag: Int) =
            setList(key, value.toList(), flag)
    }

    private class DataKey private constructor(
        val namespace: String,
        val name: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is DataKey) {
                return false
            }
            return namespace == other.namespace && name == other.name
        }

        override fun hashCode(): Int {
            var result = namespace.hashCode()
            result = 31 * result + name.hashCode()
            return result
        }

        fun toCompatKey() = getNamespacedKey(namespace, name)

        companion object {
            fun of(namespace: String = "", name: String) = DataKey(normalizeNamespace(namespace), name)

            fun parseCompatKey(key: String): DataKey {
                val index = key.indexOf(':')
                return if (index > 0 && index < key.length - 1) {
                    DataKey(normalizeNamespace(key.substring(0, index)), key.substring(index + 1))
                } else {
                    DataKey("", key)
                }
            }
        }
    }

    companion object {
        const val SYNC_FLAG: Int = 1
        const val SAVE_FLAG: Int = 2

        private val VAL_TYPE: Pattern = Pattern.compile("^\\([a-zA-Z]+(?:<[a-zA-Z]+>)?\\)")

        fun getNamespacedKey(namespace: String, key: String): String {
            val prefix = getNamespacePrefix(namespace)
            return if (prefix.isEmpty()) key else prefix + key
        }

        private fun getNamespacePrefix(namespace: String): String {
            val name = normalizeNamespace(namespace)
            return if (name.isEmpty()) "" else "$name:"
        }

        private fun normalizeNamespace(namespace: String): String {
            val name = namespace.trim()
            if (name.indexOf(':') >= 0) {
                throw IllegalArgumentException("DataMap namespace must not contain ':'")
            }
            return name
        }

        /**
         * [key, type, value]
         */
        @JvmStatic
        fun convertArg(par1: String?): Array<Array<String>> {
            val parts = par1?.let(::splitArguments) ?: return emptyArray()
            val array = Array(parts.size) { emptyArray<String>() }
            for (i in array.indices) {
                val s = parts[i]
                val idxEq = s.indexOf('=')
                val idxBr = s.indexOf(')')
                if (idxEq >= 0 && idxBr >= 0) {
                    val key = s.substring(0, idxEq)
                    val type = s.substring(idxEq + 2, idxBr)
                    val value = s.substring(idxBr + 1)
                    array[i] = arrayOf(key, type, value)
                } else {
                    NGTLog.debug("Invalid data : %s", s)
                    return emptyArray()
                }
            }
            return array
        }

        private fun splitArguments(value: String): List<String> {
            val parts = ArrayList<String>()
            var start = 0
            var depth = 0
            var quoted = false
            var escaped = false
            value.forEachIndexed { index, char ->
                if (quoted) {
                    when {
                        escaped -> escaped = false
                        char == '\\' -> escaped = true
                        char == '"' -> quoted = false
                    }
                } else {
                    when (char) {
                        '"' -> quoted = true
                        '[', '{' -> depth++
                        ']', '}' -> depth = maxOf(0, depth - 1)
                        ',' -> if (depth == 0) {
                            parts += value.substring(start, index)
                            start = index + 1
                        }
                    }
                }
            }
            parts += value.substring(start)
            return parts
        }
    }
}
