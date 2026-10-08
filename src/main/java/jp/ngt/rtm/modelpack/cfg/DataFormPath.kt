package jp.ngt.rtm.modelpack.cfg

sealed class DataFormPathSegment {
    data class Key(val value: String) : DataFormPathSegment()
    data class Element(val index: Int) : DataFormPathSegment()
    data object Index : DataFormPathSegment()
}

internal enum class DataFormTargetKind { LEAF, COMPOUND_LIST }

internal data class DataFormFieldDescriptor(
    val rootKey: String,
    val pattern: List<DataFormPathSegment>,
    val definition: ResourceConfig.DMInitValue,
    val kind: DataFormTargetKind
) {
    fun matches(rootKey: String, path: List<DataFormPathSegment>): Boolean {
        if (this.rootKey != rootKey || pattern.size != path.size) return false
        return pattern.zip(path).all { (expected, actual) ->
            when (expected) {
                DataFormPathSegment.Index -> actual is DataFormPathSegment.Element && actual.index >= 0
                is DataFormPathSegment.Key -> expected == actual
                is DataFormPathSegment.Element -> expected == actual
            }
        }
    }
}
