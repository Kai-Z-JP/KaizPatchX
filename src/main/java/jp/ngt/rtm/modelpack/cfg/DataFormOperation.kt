package jp.ngt.rtm.modelpack.cfg

import jp.ngt.rtm.modelpack.state.DataEntry

enum class DataFormOperationType {
    SET,
    LIST_INSERT,
    LIST_REMOVE
}

data class DataFormOperation(
    val type: DataFormOperationType,
    val rootKey: String,
    val path: List<DataFormPathSegment>,
    val entry: DataEntry<*>? = null,
    val index: Int = -1
) {
    companion object {
        fun set(rootKey: String, path: List<DataFormPathSegment>, entry: DataEntry<*>) =
            DataFormOperation(DataFormOperationType.SET, rootKey, path, entry)

        fun insert(rootKey: String, path: List<DataFormPathSegment>, index: Int) =
            DataFormOperation(DataFormOperationType.LIST_INSERT, rootKey, path, index = index)

        fun remove(rootKey: String, path: List<DataFormPathSegment>, index: Int) =
            DataFormOperation(DataFormOperationType.LIST_REMOVE, rootKey, path, index = index)
    }
}
