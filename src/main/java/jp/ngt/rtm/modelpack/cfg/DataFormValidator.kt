package jp.ngt.rtm.modelpack.cfg

import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataType
import jp.ngt.rtm.modelpack.state.DataTypeHandlers
import jp.ngt.rtm.modelpack.state.DataValueCodec

data class DataFormValidationResult(
    val entries: Map<String, DataEntry<*>> = emptyMap(),
    val error: String? = null
) {
    val isValid: Boolean get() = error == null
}

object DataFormValidator {
    @JvmStatic
    fun validate(
        form: DataFormConfig?,
        operations: List<DataFormOperation>,
        currentEntries: Map<String, DataEntry<*>>
    ): DataFormValidationResult {
        if (form == null || !form.isValid) return invalid("form definition is not valid")

        val descriptors = form.getDescriptors()
        val working = currentEntries.mapValuesTo(LinkedHashMap()) { DataValueCodec.copyEntry(it.value) }
        val touchedRoots = LinkedHashSet<String>()
        val setPaths = HashSet<Pair<String, List<DataFormPathSegment>>>()
        try {
            operations.forEach { operation ->
                val expectedKind = when (operation.type) {
                    DataFormOperationType.SET -> DataFormTargetKind.LEAF
                    DataFormOperationType.LIST_INSERT, DataFormOperationType.LIST_REMOVE ->
                        DataFormTargetKind.COMPOUND_LIST
                }
                val descriptor = descriptors.firstOrNull {
                    it.kind == expectedKind && it.matches(operation.rootKey, operation.path)
                } ?: return invalid("operation path is not defined in the form")

                val rootDefinition = form.getDefaultValue(operation.rootKey)
                    ?: return invalid("missing root data definition")
                val rootType = DataType.getType(rootDefinition.type)
                    ?: return invalid("unknown root data type")
                var root = working[operation.rootKey]
                    ?.takeIf { it.type == rootType }
                    ?: DataTypeHandlers.createDefault(rootType, rootDefinition, 0)

                root = when (operation.type) {
                    DataFormOperationType.SET -> {
                        val entry = operation.entry ?: return invalid("missing form value")
                        validateLeaf(entry, descriptor)?.let { return invalid(it) }
                        val pathKey = operation.rootKey to operation.path
                        if (!setPaths.add(pathKey)) return invalid("duplicate form value path")
                        operation.path.indices
                            .filter { operation.path[it] is DataFormPathSegment.Element }
                            .forEach { elementIndex ->
                                val listPath = operation.path.take(elementIndex)
                                val listDescriptor = descriptors.firstOrNull {
                                    it.kind == DataFormTargetKind.COMPOUND_LIST &&
                                            it.matches(operation.rootKey, listPath)
                                } ?: return invalid("List<Compound> path is not defined")
                                root = DataFormTree.ensureCompoundList(root, listPath, listDescriptor.definition)
                            }
                        DataFormTree.replace(root, operation.path, entry)
                    }

                    DataFormOperationType.LIST_INSERT ->
                        DataFormTree.insertCompound(root, operation.path, operation.index, descriptor.definition)

                    DataFormOperationType.LIST_REMOVE ->
                        DataFormTree.removeCompound(root, operation.path, operation.index)
                }
                working[operation.rootKey] = root
                touchedRoots += operation.rootKey
            }
        } catch (e: RuntimeException) {
            return invalid(e.message ?: "invalid form operation")
        }

        val result = LinkedHashMap<String, DataEntry<*>>()
        touchedRoots.forEach { key ->
            val definition = form.getDefaultValue(key) ?: return invalid("missing root data definition")
            val type = DataType.getType(definition.type) ?: return invalid("unknown root data type")
            val entry = working[key] ?: return invalid("missing root data value")
            DataTypeHandlers.validateDefinition(type, definition)?.let { return invalid("$key: $it") }
            DataTypeHandlers.validateEntry(type, entry, definition)?.let { return invalid("$key: $it") }
            result[key] = entry
        }
        return DataFormValidationResult(result)
    }

    private fun validateLeaf(entry: DataEntry<*>, descriptor: DataFormFieldDescriptor): String? {
        val type = DataType.getType(descriptor.definition.type) ?: return "unknown field type"
        if (entry.type != type) return "field type does not match the definition"
        val handler = DataTypeHandlers.get(type)
        return handler.validateConstraints(descriptor.definition)
            ?: handler.validateEntry(entry, descriptor.definition)
    }

    private fun invalid(error: String) = DataFormValidationResult(error = error)
}
