package jp.ngt.rtm.modelpack.state

import jp.ngt.ngtlib.math.Vec3

class DataCompound private constructor(
    private val node: DataMapNode<String>
) {
    constructor() : this(newNode())

    internal constructor(initialEntries: Map<String, DataEntry<*>>) : this(newNode(initialEntries))

    val size: Int
        get() = node.size

    fun contains(key: String): Boolean = node.contains(key)

    fun remove(key: String): Boolean = node.remove(key) != null

    fun getType(key: String): DataType? = node[key]?.type

    fun getInt(key: String): Int = typedValue<DataEntryInt, Int>(key, 0)
    fun setInt(key: String, value: Int) = putEntry(key, DataEntryInt(value, 0))
    fun getDouble(key: String): Double = typedValue<DataEntryDouble, Double>(key, 0.0)
    fun setDouble(key: String, value: Double) = putEntry(key, DataEntryDouble(value, 0))
    fun getBoolean(key: String): Boolean = typedValue<DataEntryBoolean, Boolean>(key, false)
    fun setBoolean(key: String, value: Boolean) = putEntry(key, DataEntryBoolean(value, 0))
    fun getString(key: String): String = typedValue<DataEntryString, String>(key, "")
    fun setString(key: String, value: String) = putEntry(key, DataEntryString(value, 0))
    fun getVec(key: String): Vec3 =
        (node[key] as? DataEntryVec)?.let(DataValueCodec::entryValue) as? Vec3 ?: Vec3.ZERO

    fun setVec(key: String, value: Vec3) = putEntry(key, DataEntryVec(value, 0))
    fun getHex(key: String): Int = typedValue<DataEntryHex, Int>(key, 0)
    fun setHex(key: String, value: Int) = putEntry(key, DataEntryHex(value, 0))

    fun getList(key: String): List<Any> =
        (node[key] as? DataEntryList)?.get() ?: emptyList()

    fun getListElementType(key: String): DataType? = (node[key] as? DataEntryList)?.elementType

    fun setList(key: String, value: Collection<*>, dataType: DataType) =
        putEntry(key, DataEntryList.fromValues(dataType, value, 0))

    fun getCompound(key: String): DataCompound =
        (node[key] as? DataEntryCompound)?.get() ?: DataCompound()

    fun setCompound(key: String, value: DataCompound) =
        putEntry(key, DataEntryCompound.fromCompound(value, 0))

    fun toJson(): String = DataEntryJsonCodec.plainObject(this).toString()

    internal fun getEntry(key: String): DataEntry<*>? = node[key]?.let(DataValueCodec::copyEntry)

    internal fun putEntry(key: String, entry: DataEntry<*>) = node.put(key, entry).let { Unit }

    internal fun removeEntry(key: String): DataEntry<*>? = node.remove(key)

    internal fun typedEntries(): Map<String, DataEntry<*>> = node.entries()

    internal fun deepCopy(): DataCompound = DataCompound(node.deepCopy())

    private inline fun <reified E : DataEntry<T>, T> typedValue(key: String, default: T): T =
        (node[key] as? E)?.get() ?: default

    companion object {
        private fun newNode(initialEntries: Map<String, DataEntry<*>> = emptyMap()): DataMapNode<String> =
            DataMapNode<String>().also { node ->
                initialEntries.forEach(node::put)
            }
    }
}
