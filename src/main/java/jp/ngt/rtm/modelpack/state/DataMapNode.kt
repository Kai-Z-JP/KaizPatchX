package jp.ngt.rtm.modelpack.state

/**
 * DataMapとDataCompoundが共有する、通信やnamespaceを持たない型付きデータノード。
 * 子ノードはDataEntryCompoundを介して同じ構造を再帰的に保持する。
 */
internal class DataMapNode<K>(
    private val values: MutableMap<K, DataEntry<*>> = LinkedHashMap(),
    private val copyOnWrite: Boolean = true
) {
    val size: Int get() = values.size
    val keys: Set<K> get() = values.keys

    operator fun get(key: K): DataEntry<*>? = values[key]

    fun contains(key: K): Boolean = values.containsKey(key)

    fun put(key: K, entry: DataEntry<*>): DataEntry<*>? =
        values.put(key, if (copyOnWrite) DataValueCodec.copyEntry(entry) else entry)

    fun remove(key: K): DataEntry<*>? = values.remove(key)

    fun entries(): Map<K, DataEntry<*>> = values

    fun deepCopy(): DataMapNode<K> = DataMapNode(
        LinkedHashMap<K, DataEntry<*>>().also { copy ->
            values.forEach { (key, entry) -> copy[key] = DataValueCodec.copyEntry(entry) }
        },
        copyOnWrite
    )
}
