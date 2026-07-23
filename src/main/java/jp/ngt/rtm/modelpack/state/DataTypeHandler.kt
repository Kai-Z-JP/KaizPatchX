package jp.ngt.rtm.modelpack.state

import jp.ngt.ngtlib.math.Vec3
import jp.ngt.rtm.modelpack.cfg.ResourceConfig

data class DataTypeValidationResult(
    val entry: DataEntry<*>? = null,
    val error: String? = null
) {
    val isValid: Boolean
        get() = error == null
}

interface DataTypeHandler {
    val type: DataType

    fun parse(rawValue: String, definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*>

    fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*>

    fun validateConstraints(definition: ResourceConfig.DMInitValue): String?

    fun validateEntry(
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean = true
    ): String?

    fun validateValue(
        value: Any,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean = true
    ): String?

    fun format(value: Any): String

    fun parseElement(rawValue: String): Any

    fun coerceElement(value: Any?): Any

    fun createElementEntry(rawValue: String, flag: Int): DataEntry<*>

    fun defaultElementValue(): String
}

object DataTypeHandlers {
    const val MAX_LIST_ITEMS = 64
    const val DEFAULT_MAX_LIST_ITEMS = 16
    const val MAX_LIST_ELEMENT_LENGTH = 1024

    private val handlers: Map<DataType, DataTypeHandler> = listOf(
        BooleanDataTypeHandler,
        DoubleDataTypeHandler,
        IntDataTypeHandler,
        StringDataTypeHandler,
        VecDataTypeHandler,
        HexDataTypeHandler,
        ListDataTypeHandler
    ).associateBy { it.type }

    @JvmStatic
    fun get(type: DataType): DataTypeHandler =
        handlers[type] ?: throw IllegalArgumentException("Unsupported data type: ${type.key}")

    @JvmStatic
    fun validateDefinition(type: DataType, definition: ResourceConfig.DMInitValue): String? {
        val handler = get(type)
        handler.validateConstraints(definition)?.let { return it }
        val entry = try {
            handler.createDefault(definition, 0)
        } catch (_: RuntimeException) {
            return "Invalid ${type.key} default value"
        }
        return handler.validateEntry(entry, definition, includeSuggestions = false)
    }

    @JvmStatic
    fun parseAndValidate(
        type: DataType,
        rawValue: String,
        definition: ResourceConfig.DMInitValue,
        flag: Int
    ): DataTypeValidationResult {
        val handler = get(type)
        handler.validateConstraints(definition)?.let { return DataTypeValidationResult(error = it) }
        val entry = try {
            handler.parse(rawValue, definition, flag)
        } catch (_: RuntimeException) {
            return DataTypeValidationResult(error = "Invalid ${type.key} value")
        }
        val error = handler.validateEntry(entry, definition)
        return if (error == null) DataTypeValidationResult(entry) else DataTypeValidationResult(error = error)
    }

    @JvmStatic
    fun validateEntry(
        type: DataType,
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue
    ): String? = get(type).validateConstraints(definition)
        ?: get(type).validateEntry(entry, definition, includeSuggestions = false)

    @JvmStatic
    fun validateCandidate(
        type: DataType,
        candidate: Any?,
        definition: ResourceConfig.DMInitValue
    ): Boolean {
        if (candidate == null) {
            return false
        }
        return try {
            when (candidate) {
                is DataEntry<*> -> validateEntry(type, candidate, definition) == null
                is String -> {
                    val handler = get(type)
                    handler.validateConstraints(definition) == null &&
                            handler.validateEntry(
                                handler.parse(candidate, definition, 0),
                                definition,
                                includeSuggestions = false
                            ) == null
                }

                else -> get(type).validateConstraints(definition) == null &&
                        get(type).validateValue(candidate, definition, includeSuggestions = false) == null
            }
        } catch (_: RuntimeException) {
            false
        }
    }

    @JvmStatic
    fun createDefault(type: DataType, definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        get(type).createDefault(definition, flag)

    @JvmStatic
    fun parseElementValue(type: DataType, rawValue: String): Any = elementHandler(type).parseElement(rawValue)

    @JvmStatic
    fun coerceElementValue(type: DataType, value: Any?): Any = elementHandler(type).coerceElement(value)

    @JvmStatic
    fun createElementEntry(type: DataType, rawValue: String, flag: Int): DataEntry<*> =
        elementHandler(type).createElementEntry(rawValue, flag)

    @JvmStatic
    fun formatValue(type: DataType, value: Any): String = elementHandler(type).format(value)

    @JvmStatic
    fun defaultElementValue(type: DataType): String = elementHandler(type).defaultElementValue()

    private fun elementHandler(type: DataType): DataTypeHandler {
        require(type != DataType.LIST) { "Nested lists are not supported" }
        return get(type)
    }
}

private abstract class ScalarDataTypeHandler<T : Any>(
    final override val type: DataType
) : DataTypeHandler {
    protected abstract fun parseScalar(rawValue: String): T

    protected abstract fun coerceScalar(value: Any?): T

    protected abstract fun createEntry(value: T, flag: Int): DataEntry<*>

    protected abstract fun formatScalar(value: T): String

    protected open fun validateTyped(value: T, definition: ResourceConfig.DMInitValue): String? = null

    override fun parse(rawValue: String, definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        createEntry(parseScalar(rawValue), flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        parse(definition.value ?: "null", definition, flag)

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? = null

    override fun validateEntry(
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        if (entry.type != type) {
            return "Expected ${type.key} value"
        }
        return validateValue(entry.get(), definition, includeSuggestions)
    }

    override fun validateValue(
        value: Any,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val typedValue = try {
            coerceScalar(value)
        } catch (_: RuntimeException) {
            return "Invalid ${type.key} value"
        }
        validateTyped(typedValue, definition)?.let { return it }
        if (includeSuggestions && !matchesSuggestions(typedValue, definition.suggestions)) {
            return "Value is not one of the configured suggestions"
        }
        return null
    }

    private fun matchesSuggestions(value: T, suggestions: Array<String>?): Boolean {
        if (suggestions.isNullOrEmpty()) {
            return true
        }
        if (suggestions.size == 1 && suggestions[0].startsWith("-")) {
            return true
        }
        val formatted = formatScalar(value)
        return suggestions.any { suggestion ->
            suggestion == formatted || try {
                formatScalar(parseScalar(suggestion)) == formatted
            } catch (_: RuntimeException) {
                false
            }
        }
    }

    override fun format(value: Any): String = formatScalar(coerceScalar(value))

    override fun parseElement(rawValue: String): Any = parseScalar(rawValue)

    override fun coerceElement(value: Any?): Any = coerceScalar(value)

    override fun createElementEntry(rawValue: String, flag: Int): DataEntry<*> =
        createEntry(parseScalar(rawValue), flag)
}

private object IntDataTypeHandler : ScalarDataTypeHandler<Int>(DataType.INT) {
    override fun parseScalar(rawValue: String): Int = if (rawValue.isEmpty()) 0 else rawValue.toInt()

    override fun coerceScalar(value: Any?): Int = when (value) {
        is Number -> value.toInt()
        is String -> parseScalar(value)
        else -> throw IllegalArgumentException("Int value is required")
    }

    override fun createEntry(value: Int, flag: Int): DataEntry<*> = DataEntryInt(value, flag)

    override fun formatScalar(value: Int): String = value.toString()

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        validateMinMax(definition, type)

    override fun validateTyped(value: Int, definition: ResourceConfig.DMInitValue): String? =
        validateNumberRange(value.toDouble(), definition)

    override fun defaultElementValue(): String = "0"
}

private object HexDataTypeHandler : ScalarDataTypeHandler<Int>(DataType.HEX) {
    override fun parseScalar(rawValue: String): Int = if (rawValue.isEmpty()) 0 else Integer.decode(rawValue)

    override fun coerceScalar(value: Any?): Int = when (value) {
        is Number -> value.toInt()
        is String -> parseScalar(value)
        else -> throw IllegalArgumentException("Hex value is required")
    }

    override fun createEntry(value: Int, flag: Int): DataEntry<*> = DataEntryHex(value, flag)

    override fun formatScalar(value: Int): String = "0x${value.toString(16)}"

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        validateMinMax(definition, type)

    override fun validateTyped(value: Int, definition: ResourceConfig.DMInitValue): String? =
        validateNumberRange(value.toDouble(), definition)

    override fun defaultElementValue(): String = "0x0"
}

private object DoubleDataTypeHandler : ScalarDataTypeHandler<Double>(DataType.DOUBLE) {
    override fun parseScalar(rawValue: String): Double =
        (if (rawValue.isEmpty()) 0.0 else rawValue.toDouble()).also {
            require(it.isFinite()) { "Double value must be finite" }
        }

    override fun coerceScalar(value: Any?): Double = when (value) {
        is Number -> value.toDouble().also { require(it.isFinite()) { "Double value must be finite" } }
        is String -> parseScalar(value)
        else -> throw IllegalArgumentException("Double value is required")
    }

    override fun createEntry(value: Double, flag: Int): DataEntry<*> = DataEntryDouble(value, flag)

    override fun formatScalar(value: Double): String = value.toString()

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        validateMinMax(definition, type)

    override fun validateTyped(value: Double, definition: ResourceConfig.DMInitValue): String? {
        if (!value.isFinite()) {
            return "Double value must be finite"
        }
        return validateNumberRange(value, definition)
    }

    override fun defaultElementValue(): String = "0.0"
}

private object BooleanDataTypeHandler : ScalarDataTypeHandler<Boolean>(DataType.BOOLEAN) {
    override fun parseScalar(rawValue: String): Boolean = when (rawValue) {
        "true" -> true
        "false" -> false
        else -> throw IllegalArgumentException("Boolean value must be true or false")
    }

    override fun coerceScalar(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is String -> parseScalar(value)
        else -> throw IllegalArgumentException("Boolean value is required")
    }

    override fun createEntry(value: Boolean, flag: Int): DataEntry<*> = DataEntryBoolean(value, flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        createEntry(definition.value?.let(::parseScalar) ?: false, flag)

    override fun formatScalar(value: Boolean): String = value.toString()

    override fun defaultElementValue(): String = "false"
}

private object StringDataTypeHandler : ScalarDataTypeHandler<String>(DataType.STRING) {
    override fun parseScalar(rawValue: String): String = rawValue

    override fun coerceScalar(value: Any?): String =
        value as? String ?: throw IllegalArgumentException("String value is required")

    override fun createEntry(value: String, flag: Int): DataEntry<*> = DataEntryString(value, flag)

    override fun formatScalar(value: String): String = value

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? {
        val pattern = definition.pattern ?: return null
        if (pattern.size < 3 || pattern.take(3).any { it == null }) {
            return "String pattern must contain start, contains and end values"
        }
        return null
    }

    override fun validateTyped(value: String, definition: ResourceConfig.DMInitValue): String? {
        val pattern = definition.pattern ?: return null
        if (pattern.size < 3) {
            return "String pattern must contain start, contains and end values"
        }
        val start = pattern[0]
        val contains = pattern[1]
        val end = pattern[2]
        return if ((start.isEmpty() || value.startsWith(start)) &&
            (contains.isEmpty() || value.contains(contains)) &&
            (end.isEmpty() || value.endsWith(end))
        ) {
            null
        } else {
            "String value does not match the configured pattern"
        }
    }

    override fun defaultElementValue(): String = ""
}

private object VecDataTypeHandler : ScalarDataTypeHandler<Vec3>(DataType.VEC) {
    override fun parseScalar(rawValue: String): Vec3 = DataEntryVec.fromString(rawValue).also(::requireFinite)

    override fun coerceScalar(value: Any?): Vec3 = when (value) {
        is Vec3 -> value.also(::requireFinite)
        is String -> parseScalar(value)
        else -> throw IllegalArgumentException("Vec value is required")
    }

    override fun createEntry(value: Vec3, flag: Int): DataEntry<*> = DataEntryVec(value, flag)

    override fun formatScalar(value: Vec3): String = "${value.x} ${value.y} ${value.z}"

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        validateMinMax(definition, type)

    override fun validateTyped(value: Vec3, definition: ResourceConfig.DMInitValue): String? {
        if (!value.x.isFinite() || !value.y.isFinite() || !value.z.isFinite()) {
            return "Vec components must be finite"
        }
        val range = definition.minmax ?: return null
        return if (value.x in range[0]..range[1] &&
            value.y in range[0]..range[1] &&
            value.z in range[0]..range[1]
        ) {
            null
        } else {
            "Vec component is outside the configured range"
        }
    }

    override fun defaultElementValue(): String = "0.0 0.0 0.0"

    private fun requireFinite(value: Vec3) {
        require(value.x.isFinite() && value.y.isFinite() && value.z.isFinite()) {
            "Vec components must be finite"
        }
    }
}

private object ListDataTypeHandler : DataTypeHandler {
    override val type: DataType = DataType.LIST

    override fun parse(
        rawValue: String,
        definition: ResourceConfig.DMInitValue,
        flag: Int
    ): DataEntry<*> = DataEntryList.fromString(rawValue, expectedElementType(definition), flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> {
        val elementType = expectedElementType(definition)
        return definition.values?.let { DataEntryList.fromStrings(elementType, it.asList(), flag) }
            ?: DataEntryList.fromString(definition.value, elementType, flag)
    }

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? {
        val elementType = try {
            expectedElementType(definition)
        } catch (_: RuntimeException) {
            return "Invalid List element type"
        }
        val minItems = minItems(definition)
        val maxItems = maxItems(definition)
        if (minItems !in 0..DataTypeHandlers.MAX_LIST_ITEMS) {
            return "List minItems is out of range"
        }
        if (maxItems !in 0..DataTypeHandlers.MAX_LIST_ITEMS) {
            return "List maxItems is out of range"
        }
        if (minItems > maxItems) {
            return "List minItems is greater than maxItems"
        }
        return DataTypeHandlers.get(elementType).validateConstraints(definition)
    }

    override fun validateEntry(
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        if (entry !is DataEntryList) {
            return "Expected List value"
        }
        val elementType = try {
            expectedElementType(definition)
        } catch (_: RuntimeException) {
            return "Invalid List element type"
        }
        if (entry.elementType != elementType) {
            return "List element type does not match the definition"
        }
        return validateList(entry.get(), elementType, definition, includeSuggestions)
    }

    override fun validateValue(
        value: Any,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val values = value as? List<*> ?: return "Invalid List value"
        val elementType = try {
            expectedElementType(definition)
        } catch (_: RuntimeException) {
            return "Invalid List element type"
        }
        return validateList(values, elementType, definition, includeSuggestions)
    }

    private fun validateList(
        values: List<*>,
        elementType: DataType,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        if (values.size !in minItems(definition)..maxItems(definition)) {
            return "List item count is outside the configured constraints"
        }
        val elementHandler = DataTypeHandlers.get(elementType)
        values.forEachIndexed { index, value ->
            if (value == null) {
                return "List element $index must not be null"
            }
            elementHandler.validateValue(value, definition, includeSuggestions)?.let {
                return "List element $index: $it"
            }
            val text = try {
                elementHandler.format(value)
            } catch (_: RuntimeException) {
                return "List element $index has an invalid type"
            }
            if (text.length > DataTypeHandlers.MAX_LIST_ELEMENT_LENGTH) {
                return "List element $index is too long"
            }
        }
        return null
    }

    override fun format(value: Any): String =
        (value as? DataEntryList)?.toString() ?: throw IllegalArgumentException("List entry is required")

    override fun parseElement(rawValue: String): Any = throw unsupportedNestedList()

    override fun coerceElement(value: Any?): Any = throw unsupportedNestedList()

    override fun createElementEntry(rawValue: String, flag: Int): DataEntry<*> = throw unsupportedNestedList()

    override fun defaultElementValue(): String = throw unsupportedNestedList()

    private fun expectedElementType(definition: ResourceConfig.DMInitValue): DataType =
        DataEntryList.supportedElementType(definition.elementType)
            ?: throw IllegalArgumentException("Invalid List element type")

    private fun minItems(definition: ResourceConfig.DMInitValue): Int = definition.minItems ?: 0

    private fun maxItems(definition: ResourceConfig.DMInitValue): Int =
        definition.maxItems ?: DataTypeHandlers.DEFAULT_MAX_LIST_ITEMS

    private fun unsupportedNestedList() = IllegalArgumentException("Nested lists are not supported")
}

private fun validateMinMax(definition: ResourceConfig.DMInitValue, type: DataType): String? {
    val range = definition.minmax ?: return null
    if (range.size < 2 || !range[0].isFinite() || !range[1].isFinite()) {
        return "${type.key} minmax must contain two finite values"
    }
    if (range[0] > range[1]) {
        return "${type.key} minmax minimum is greater than maximum"
    }
    return null
}

private fun validateNumberRange(value: Double, definition: ResourceConfig.DMInitValue): String? {
    val range = definition.minmax ?: return null
    return if (value in range[0]..range[1]) null else "Value is outside the configured range"
}
