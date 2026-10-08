package jp.ngt.rtm.modelpack.cfg

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.rtm.modelpack.state.DataCompoundDefinitions
import jp.ngt.rtm.modelpack.state.DataEntryList
import jp.ngt.rtm.modelpack.state.DataType
import jp.ngt.rtm.modelpack.state.DataTypeHandlers
import java.util.*

class DataFormConfig {
    @JvmField
    var title: String? = ""

    @JvmField
    var columns: Int = 1

    @JvmField
    var fields: Array<DataFormField>? = emptyArray()

    @Transient
    private var validationError: String? = null

    @Transient
    private var defaultValuesByKey: Map<String, ResourceConfig.DMInitValue> = emptyMap()

    @Transient
    private var resolvedDefinitions = IdentityHashMap<DataFormField, ResourceConfig.DMInitValue>()

    @Transient
    private var descriptors: List<DataFormFieldDescriptor> = emptyList()

    fun initialize(defaultValues: Array<ResourceConfig.DMInitValue>?, modelName: String) {
        defaultValuesByKey = defaultValues.orEmpty()
            .filter { !it.key.isNullOrEmpty() }
            .associateBy { it.key }
        initializeContext(modelName, relativeEntries = null)
        if (isValid) {
            descriptors = buildRootDescriptors()
        }
    }

    internal fun initializeElement(entries: Array<ResourceConfig.DMInitValue>?, modelName: String) {
        defaultValuesByKey = emptyMap()
        initializeContext(modelName, entries)
    }

    private fun initializeContext(modelName: String, relativeEntries: Array<ResourceConfig.DMInitValue>?) {
        validationError = null
        resolvedDefinitions = IdentityHashMap()
        descriptors = emptyList()
        val configuredFields = getFieldList()
        if (columns < 1) invalidate(modelName, "columns must be positive")
        if (!isValid) return

        val identities = HashSet<String>()
        val cells = HashSet<Long>()
        val validatedRoots = HashSet<String>()
        configuredFields.forEach { field ->
            val fieldName = field.displayPath(relativeEntries != null)
            validateLayout(field, fieldName, modelName, cells)
            if (!isValid) return

            if (field.isTextElement()) {
                if (field.resolvedKey().isNotEmpty() || field.resolvedPath().isNotEmpty()) {
                    invalidate(modelName, "text field cannot have a key or path: $fieldName")
                }
                if (!isValid) return
                return@forEach
            }

            validateIdentity(field, relativeEntries != null, fieldName, modelName)
            if (!isValid) return
            val identity = "${field.resolvedKey()}\u0000${field.resolvedPath().joinToString("\u0000")}"
            if (!identities.add(identity)) {
                invalidate(modelName, "duplicate field path: $fieldName")
                return
            }

            if (relativeEntries == null && validatedRoots.add(field.resolvedKey())) {
                val root = defaultValuesByKey[field.resolvedKey()]
                val rootType = root?.type?.let(DataType::getType)
                if (root == null || rootType == null) {
                    invalidate(modelName, "field does not exist in defaultValues: $fieldName")
                    return
                }
                DataTypeHandlers.validateDefinition(rootType, root)?.let { error ->
                    invalidate(modelName, "$error: ${field.resolvedKey()}")
                    return
                }
            }

            val definition = resolveDefinition(field, relativeEntries)
            if (definition == null) {
                invalidate(modelName, "field does not exist in defaultValues: $fieldName")
                return
            }
            resolvedDefinitions[field] = definition
            val type = DataType.getType(definition.type)
            if (type == null) {
                invalidate(modelName, "unknown field type: $fieldName")
                return
            }
            if (type == DataType.COMPOUND) {
                invalidate(modelName, "Compound field must reference a member: $fieldName")
                return
            }

            val compoundList = type == DataType.LIST &&
                    DataEntryList.supportedElementType(definition.elementType) == DataType.COMPOUND
            when {
                compoundList && field.elementForm == null ->
                    invalidate(modelName, "List<Compound> field requires elementForm: $fieldName")

                !compoundList && field.elementForm != null ->
                    invalidate(modelName, "elementForm is only valid for List<Compound>: $fieldName")

                compoundList -> {
                    field.elementForm!!.initializeElement(definition.entries, "$modelName:$fieldName")
                    if (!field.elementForm!!.isValid) {
                        invalidate(modelName, "invalid elementForm: $fieldName")
                        return
                    }
                }
            }

            DataTypeHandlers.validateDefinition(type, definition)?.let { error ->
                invalidate(modelName, "$error: $fieldName")
                return
            }
        }
    }

    private fun validateLayout(
        field: DataFormField,
        fieldName: String,
        modelName: String,
        cells: MutableSet<Long>
    ) {
        when {
            field.row < 0 -> invalidate(modelName, "row is out of range: $fieldName")
            field.rowSpan < 1 -> invalidate(modelName, "rowSpan is out of range: $fieldName")

            field.column !in 0 until columns -> invalidate(modelName, "column is out of range: $fieldName")
            field.columnSpan !in 1..columns || field.column + field.columnSpan > columns ->
                invalidate(modelName, "columnSpan is out of range: $fieldName")
        }
        if (!isValid) return
        for (row in field.row until field.row + field.rowSpan) {
            for (column in field.column until field.column + field.columnSpan) {
                val cell = (row.toLong() shl 32) or (column.toLong() and 0xFFFFFFFFL)
                if (!cells.add(cell)) {
                    invalidate(modelName, "multiple fields use row $row, column $column")
                    return
                }
            }
        }
    }

    private fun validateIdentity(field: DataFormField, relative: Boolean, fieldName: String, modelName: String) {
        val key = field.resolvedKey()
        val path = field.resolvedPath()
        when {
            relative && key.isNotEmpty() -> invalidate(modelName, "elementForm field key must be empty: $fieldName")
            relative && path.isEmpty() -> invalidate(modelName, "elementForm field path is empty")
            !relative && key.isEmpty() -> invalidate(modelName, "field key is empty")
            path.any { it.isEmpty() } ->
                invalidate(modelName, "field path contains an invalid segment: $fieldName")
        }
    }

    private fun resolveDefinition(
        field: DataFormField,
        relativeEntries: Array<ResourceConfig.DMInitValue>?
    ): ResourceConfig.DMInitValue? {
        val path = field.resolvedPath()
        if (relativeEntries != null) {
            return DataCompoundDefinitions.findDefinition(relativeEntries, path)
        }
        val root = defaultValuesByKey[field.resolvedKey()] ?: return null
        if (path.isEmpty()) return root
        return DataCompoundDefinitions.findDefinition(root, path)
    }

    private fun buildRootDescriptors(): List<DataFormFieldDescriptor> {
        val result = ArrayList<DataFormFieldDescriptor>()
        getValueFieldList().forEach { field ->
            val definition = getResolvedDefinition(field) ?: return@forEach
            collectDescriptors(
                field.resolvedKey(), field.resolvedPath().map(DataFormPathSegment::Key),
                field, definition, result
            )
        }
        return result
    }

    private fun collectDescriptors(
        rootKey: String,
        prefix: List<DataFormPathSegment>,
        field: DataFormField,
        definition: ResourceConfig.DMInitValue,
        target: MutableList<DataFormFieldDescriptor>
    ) {
        val compoundList = DataType.getType(definition.type) == DataType.LIST &&
                DataEntryList.supportedElementType(definition.elementType) == DataType.COMPOUND
        if (!compoundList) {
            target += DataFormFieldDescriptor(rootKey, prefix, definition, DataFormTargetKind.LEAF)
            return
        }
        target += DataFormFieldDescriptor(rootKey, prefix, definition, DataFormTargetKind.COMPOUND_LIST)
        val elementForm = field.elementForm ?: return
        elementForm.getValueFieldList().forEach { child ->
            val childDefinition = elementForm.getResolvedDefinition(child) ?: return@forEach
            elementForm.collectDescriptors(
                rootKey,
                prefix + DataFormPathSegment.Index + child.resolvedPath().map(DataFormPathSegment::Key),
                child, childDefinition, target
            )
        }
    }

    fun getFieldList(): List<DataFormField> = fields?.toList() ?: emptyList()
    fun getValueFieldList(): List<DataFormField> = getFieldList().filterNot(DataFormField::isTextElement)
    fun getDefaultValue(key: String): ResourceConfig.DMInitValue? = defaultValuesByKey[key]
    fun getResolvedDefinition(field: DataFormField): ResourceConfig.DMInitValue? = resolvedDefinitions[field]
    internal fun getDescriptors(): List<DataFormFieldDescriptor> = descriptors
    fun getValidationError(): String? = validationError

    val isValid: Boolean get() = validationError == null
    val rowCount: Int get() = getFieldList().maxOfOrNull { it.row + it.rowSpan } ?: 0

    private fun invalidate(modelName: String, reason: String) {
        if (validationError == null) {
            validationError = reason
            NGTLog.debug("[RTM] Invalid data form ($modelName): $reason")
        }
    }

    companion object {
        @JvmStatic
        fun getMinItems(value: ResourceConfig.DMInitValue): Int = value.minItems ?: 0

        @JvmStatic
        fun getMaxItems(value: ResourceConfig.DMInitValue): Int = value.maxItems ?: Int.MAX_VALUE
    }
}

class DataFormField {
    @JvmField
    var key: String? = ""

    @JvmField
    var path: Array<String>? = emptyArray()

    @JvmField
    var label: String? = ""

    @JvmField
    var text: String? = ""

    @JvmField
    var row: Int = 0

    @JvmField
    var column: Int = 0

    @JvmField
    var columnSpan: Int = 1

    @JvmField
    var rowSpan: Int = 1

    @JvmField
    var elementForm: DataFormConfig? = null

    fun resolvedKey(): String = key.orEmpty()
    fun resolvedPath(): List<String> = path?.toList() ?: emptyList()
    fun resolvedLabel(): String = label.orEmpty().ifEmpty { resolvedPath().lastOrNull() ?: resolvedKey() }
    fun resolvedText(): String = text.orEmpty()
    fun isTextElement(): Boolean = !text.isNullOrEmpty()
    internal fun displayPath(relative: Boolean): String =
        (if (relative) emptyList() else listOf(resolvedKey()))
            .plus(resolvedPath()).filter(String::isNotEmpty).joinToString(".")
            .ifEmpty { "text at row $row, column $column" }
}
