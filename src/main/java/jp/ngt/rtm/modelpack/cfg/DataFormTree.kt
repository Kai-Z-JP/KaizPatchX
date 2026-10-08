package jp.ngt.rtm.modelpack.cfg

import jp.ngt.rtm.modelpack.state.*

internal object DataFormTree {
    fun entryAt(root: DataEntry<*>?, path: List<DataFormPathSegment>): DataEntry<*>? {
        var current = root?.let(DataValueCodec::copyEntry) ?: return null
        path.forEach { segment ->
            current = when (segment) {
                is DataFormPathSegment.Key ->
                    (current as? DataEntryCompound)?.rawValue()?.getEntry(segment.value) ?: return null

                is DataFormPathSegment.Element -> {
                    val list = current as? DataEntryList ?: return null
                    if (list.elementType != DataType.COMPOUND) return null
                    val compound = list.get().getOrNull(segment.index) as? DataCompound ?: return null
                    DataEntryCompound.fromCompound(compound, current.flag)
                }

                DataFormPathSegment.Index -> return null
            }
        }
        return current
    }

    fun replace(root: DataEntry<*>, path: List<DataFormPathSegment>, value: DataEntry<*>): DataEntry<*> {
        if (path.isEmpty()) return DataValueCodec.copyEntry(value)
        return when (val segment = path.first()) {
            is DataFormPathSegment.Key -> {
                val compoundEntry = root as? DataEntryCompound
                    ?: throw IllegalArgumentException("Compound path traverses a non-Compound value")
                val compound = compoundEntry.get()
                val remaining = path.drop(1)
                val child = compound.getEntry(segment.value)
                val replaced = when {
                    remaining.isEmpty() -> DataValueCodec.copyEntry(value)
                    child != null -> replace(child, remaining, value)
                    remaining.first() is DataFormPathSegment.Key ->
                        replace(DataEntryCompound.empty(root.flag), remaining, value)

                    else -> throw IllegalArgumentException("Compound path does not exist")
                }
                compound.putEntry(segment.value, replaced)
                DataEntryCompound.fromCompound(compound, root.flag)
            }

            is DataFormPathSegment.Element -> {
                val list = root as? DataEntryList
                    ?: throw IllegalArgumentException("List path traverses a non-List value")
                require(list.elementType == DataType.COMPOUND) { "Only List<Compound> can be traversed" }
                val values = list.get().toMutableList()
                val compound = values.getOrNull(segment.index) as? DataCompound
                    ?: throw IllegalArgumentException("List element index is out of range")
                val replaced = replace(
                    DataEntryCompound.fromCompound(compound, root.flag),
                    path.drop(1), value
                ) as? DataEntryCompound ?: throw IllegalArgumentException("List element must remain Compound")
                values[segment.index] = replaced.get()
                DataEntryList.fromValues(DataType.COMPOUND, values, root.flag)
            }

            DataFormPathSegment.Index -> throw IllegalArgumentException("Wildcard path cannot be applied")
        }
    }

    fun insertCompound(
        root: DataEntry<*>,
        path: List<DataFormPathSegment>,
        index: Int,
        definition: ResourceConfig.DMInitValue
    ): DataEntry<*> = updateCompoundList(root, path, definition, createMissing = true) { values ->
        require(index in 0..values.size) { "List insert index is out of range" }
        values.add(index, DataCompoundDefinitions.createDefault(definition))
    }

    fun removeCompound(
        root: DataEntry<*>,
        path: List<DataFormPathSegment>,
        index: Int
    ): DataEntry<*> = updateCompoundList(root, path, null, createMissing = false) { values ->
        require(index in values.indices) { "List remove index is out of range" }
        values.removeAt(index)
    }

    fun ensureCompoundList(
        root: DataEntry<*>,
        path: List<DataFormPathSegment>,
        definition: ResourceConfig.DMInitValue
    ): DataEntry<*> {
        if (entryAt(root, path) is DataEntryList) return root
        val defaultList = DataTypeHandlers.createDefault(DataType.LIST, definition, root.flag)
        return replace(root, path, defaultList)
    }

    private fun updateCompoundList(
        root: DataEntry<*>,
        path: List<DataFormPathSegment>,
        definition: ResourceConfig.DMInitValue?,
        createMissing: Boolean,
        update: (MutableList<Any>) -> Unit
    ): DataEntry<*> {
        var baseRoot = root
        var list = entryAt(baseRoot, path) as? DataEntryList
        if (list == null && createMissing && definition != null) {
            list = DataTypeHandlers.createDefault(DataType.LIST, definition, root.flag) as DataEntryList
            baseRoot = replace(baseRoot, path, list)
        }
        list ?: throw IllegalArgumentException("List<Compound> path does not exist")
        require(list.elementType == DataType.COMPOUND) { "Path is not List<Compound>" }
        val values = list.get().toMutableList()
        update(values)
        val replacement = DataEntryList.fromValues(DataType.COMPOUND, values, list.flag)
        return replace(baseRoot, path, replacement)
    }
}
