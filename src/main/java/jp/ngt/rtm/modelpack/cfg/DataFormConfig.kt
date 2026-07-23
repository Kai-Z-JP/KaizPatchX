package jp.ngt.rtm.modelpack.cfg

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.rtm.modelpack.state.DataType
import jp.ngt.rtm.modelpack.state.DataTypeHandlers

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

    fun initialize(defaultValues: Array<ResourceConfig.DMInitValue>?, modelName: String) {
        validationError = null
        defaultValuesByKey = (defaultValues ?: emptyArray())
            .filter { !it.key.isNullOrEmpty() }
            .associateBy { it.key }

        val configuredFields = getFieldList()
        when {
            columns !in 1..MAX_COLUMNS -> invalidate(modelName, "columns must be between 1 and $MAX_COLUMNS")
            configuredFields.size > MAX_FIELDS -> invalidate(modelName, "too many fields")
            title.orEmpty().length > MAX_LABEL_LENGTH -> invalidate(modelName, "title is too long")
        }
        if (!isValid) {
            return
        }

        val keys = HashSet<String>()
        val cells = HashSet<Long>()
        for (field in configuredFields) {
            val key = field.key.orEmpty()
            val label = field.label.orEmpty()
            val text = field.text.orEmpty()
            val isText = field.isTextElement()
            val fieldName = key.ifEmpty { "text at row ${field.row}, column ${field.column}" }
            val defaultValue = defaultValuesByKey[key]
            val dataType = defaultValue?.type?.let(DataType::getType)

            when {
                field.row !in 0 until MAX_ROWS -> invalidate(modelName, "row is out of range: $fieldName")
                field.rowSpan !in 1..MAX_ROWS -> invalidate(
                    modelName,
                    "rowSpan is out of range: $fieldName"
                )

                field.row + field.rowSpan > MAX_ROWS -> invalidate(
                    modelName,
                    "rowSpan exceeds the grid: $fieldName"
                )

                field.column !in 0 until columns -> invalidate(modelName, "column is out of range: $fieldName")
                field.columnSpan !in 1..columns -> invalidate(
                    modelName,
                    "columnSpan is out of range: $fieldName"
                )

                field.column + field.columnSpan > columns -> invalidate(
                    modelName,
                    "columnSpan exceeds the grid: $fieldName"
                )

                isText && key.isNotEmpty() -> invalidate(modelName, "text field cannot have a key: $key")
                isText && text.length > MAX_TEXT_LENGTH -> invalidate(
                    modelName,
                    "field text is too long: $fieldName"
                )

                !isText && key.isEmpty() -> invalidate(modelName, "field key is empty")
                !isText && key.length > MAX_KEY_LENGTH -> invalidate(modelName, "field key is too long: $key")
                !isText && label.length > MAX_LABEL_LENGTH -> invalidate(modelName, "field label is too long: $key")
                !isText && !keys.add(key) -> invalidate(modelName, "duplicate field key: $key")
                !isText && defaultValue == null -> invalidate(
                    modelName,
                    "field does not exist in defaultValues: $key"
                )

                !isText && dataType == null -> invalidate(modelName, "unknown field type: $key")
            }
            if (!isValid) {
                return
            }
            for (row in field.row until field.row + field.rowSpan) {
                for (column in field.column until field.column + field.columnSpan) {
                    val cell = (row.toLong() shl 32) or (column.toLong() and 0xFFFFFFFFL)
                    if (!cells.add(cell)) {
                        invalidate(modelName, "multiple fields use row $row, column $column")
                        return
                    }
                }
            }
            if (isText) {
                continue
            }
            val resolvedType = dataType ?: return
            DataTypeHandlers.validateDefinition(resolvedType, defaultValue)?.let { error ->
                invalidate(modelName, "$error: $key")
                return
            }
        }
    }

    fun getFieldList(): List<DataFormField> = fields?.toList() ?: emptyList()

    fun getValueFieldList(): List<DataFormField> = getFieldList().filterNot(DataFormField::isTextElement)

    fun getDefaultValue(key: String): ResourceConfig.DMInitValue? = defaultValuesByKey[key]

    fun getValidationError(): String? = validationError

    val isValid: Boolean
        get() = validationError == null

    val rowCount: Int
        get() = getFieldList().maxOfOrNull { it.row + it.rowSpan } ?: 0

    private fun invalidate(modelName: String, reason: String) {
        if (validationError == null) {
            validationError = reason
            NGTLog.debug("[RTM] Invalid data form ($modelName): $reason")
        }
    }

    companion object {
        const val MAX_COLUMNS = 8
        const val MAX_ROWS = 64
        const val MAX_FIELDS = 64
        const val MAX_KEY_LENGTH = 128
        const val MAX_LABEL_LENGTH = 128
        const val MAX_TEXT_LENGTH = 512
        const val MAX_SCALAR_VALUE_LENGTH = DataTypeHandlers.MAX_LIST_ELEMENT_LENGTH
        const val MAX_LIST_ITEMS = DataTypeHandlers.MAX_LIST_ITEMS
        const val DEFAULT_MAX_LIST_ITEMS = DataTypeHandlers.DEFAULT_MAX_LIST_ITEMS

        @JvmStatic
        fun getMinItems(value: ResourceConfig.DMInitValue): Int = value.minItems ?: 0

        @JvmStatic
        fun getMaxItems(value: ResourceConfig.DMInitValue): Int = value.maxItems ?: DEFAULT_MAX_LIST_ITEMS
    }
}

class DataFormField {
    @JvmField
    var key: String? = ""

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

    fun resolvedKey(): String = key.orEmpty()

    fun resolvedLabel(): String = label.orEmpty().ifEmpty { resolvedKey() }

    fun resolvedText(): String = text.orEmpty()

    fun isTextElement(): Boolean = !text.isNullOrEmpty()
}

