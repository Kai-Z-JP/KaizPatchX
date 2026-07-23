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

    fun validateConstraints(definition: ResourceConfig.DMInitValue): String? = null

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

    fun parseElement(rawValue: String): Any = throw nestedListError()

    fun coerceElement(value: Any?): Any = throw nestedListError()

    fun createElementEntry(value: Any?, flag: Int): DataEntry<*> = throw nestedListError()

    fun defaultElementValue(): String = throw nestedListError()
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
    fun createElementEntry(type: DataType, value: Any?, flag: Int): DataEntry<*> =
        elementHandler(type).createElementEntry(value, flag)

    @JvmStatic
    fun formatValue(type: DataType, value: Any): String = elementHandler(type).format(value)

    @JvmStatic
    fun defaultElementValue(type: DataType): String = elementHandler(type).defaultElementValue()

    private fun elementHandler(type: DataType): DataTypeHandler {
        require(type != DataType.LIST) { "Nested lists are not supported" }
        return get(type)
    }
}

private class ScalarDataTypeHandler<T : Any>(
    override val type: DataType,
    private val defaultValue: T,
    private val parseScalar: (String) -> T,
    private val coerceScalar: (Any?) -> T,
    private val createEntry: (T, Int) -> DataEntry<*>,
    private val formatScalar: (T) -> String = Any::toString,
    private val constraints: (ResourceConfig.DMInitValue) -> String? = { null },
    private val validator: (T, ResourceConfig.DMInitValue) -> String? = { _, _ -> null },
    private val defaultFactory: (ResourceConfig.DMInitValue) -> T = {
        parseScalar(it.value ?: "null")
    }
) : DataTypeHandler {

    override fun parse(rawValue: String, definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        createEntry(parseScalar(rawValue), flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        createEntry(defaultFactory(definition), flag)

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        constraints(definition)

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
        validator(typedValue, definition)?.let { return it }
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

    override fun createElementEntry(value: Any?, flag: Int): DataEntry<*> =
        createEntry(coerceScalar(value), flag)

    override fun defaultElementValue(): String = formatScalar(defaultValue)
}

private fun <T : Number> numericHandler(
    type: DataType,
    defaultValue: T,
    parse: (String) -> T,
    convert: (Number) -> T,
    createEntry: (T, Int) -> DataEntry<*>,
    format: (T) -> String = Any::toString
) = ScalarDataTypeHandler(
    type,
    defaultValue,
    parse,
    { value ->
        when (value) {
            is Number -> convert(value)
            is String -> parse(value)
            else -> throw IllegalArgumentException("${type.key} value is required")
        }
    },
    createEntry,
    format,
    { validateMinMax(it, type) },
    { value, definition -> validateNumberRange(value.toDouble(), definition) }
)

private val IntDataTypeHandler = numericHandler(
    DataType.INT, 0,
    { if (it.isEmpty()) 0 else it.toInt() },
    Number::toInt,
    ::DataEntryInt
)
private val HexDataTypeHandler = numericHandler(
    DataType.HEX, 0,
    { if (it.isEmpty()) 0 else Integer.decode(it) },
    Number::toInt,
    ::DataEntryHex,
    { "0x${it.toString(16)}" }
)
private val DoubleDataTypeHandler = numericHandler(
    DataType.DOUBLE, 0.0,
    { finite(if (it.isEmpty()) 0.0 else it.toDouble()) },
    { finite(it.toDouble()) },
    ::DataEntryDouble
)
private val BooleanDataTypeHandler = ScalarDataTypeHandler(
    DataType.BOOLEAN, false,
    ::strictBoolean,
    { it as? Boolean ?: (it as? String)?.let(::strictBoolean) ?: error("Boolean value is required") },
    ::DataEntryBoolean,
    defaultFactory = { it.value?.let(::strictBoolean) ?: false }
)
private val StringDataTypeHandler = ScalarDataTypeHandler(
    DataType.STRING, "",
    { it },
    { it as? String ?: error("String value is required") },
    ::DataEntryString,
    { it },
    ::validateStringPattern,
    ::validateString
)
private val VecDataTypeHandler = ScalarDataTypeHandler(
    DataType.VEC, Vec3.ZERO,
    { DataEntryVec.fromString(it).also(::requireFinite) },
    { value ->
        when (value) {
            is Vec3 -> value.also(::requireFinite)
            is String -> DataEntryVec.fromString(value).also(::requireFinite)
            else -> error("Vec value is required")
        }
    },
    ::DataEntryVec,
    { "${it.x} ${it.y} ${it.z}" },
    { validateMinMax(it, DataType.VEC) },
    ::validateVec
)

private object ListDataTypeHandler : DataTypeHandler {
    override val type: DataType = DataType.LIST

    override fun parse(
        rawValue: String,
        definition: ResourceConfig.DMInitValue,
        flag: Int
    ): DataEntry<*> = DataEntryList.fromString(rawValue, requireElementType(definition), flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> {
        val elementType = requireElementType(definition)
        return definition.values?.let { DataEntryList.fromValues(elementType, it.asList(), flag) }
            ?: DataEntryList.fromString(definition.value, elementType, flag)
    }

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? {
        val elementType = elementType(definition) ?: return "Invalid List element type"
        val minItems = minItems(definition)
        val maxItems = maxItems(definition)
        return when {
            minItems !in 0..DataTypeHandlers.MAX_LIST_ITEMS -> "List minItems is out of range"
            maxItems !in 0..DataTypeHandlers.MAX_LIST_ITEMS -> "List maxItems is out of range"
            minItems > maxItems -> "List minItems is greater than maxItems"
            else -> DataTypeHandlers.get(elementType).validateConstraints(definition)
        }
    }

    override fun validateEntry(
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val list = entry as? DataEntryList ?: return "Expected List value"
        val elementType = elementType(definition) ?: return "Invalid List element type"
        if (list.elementType != elementType) {
            return "List element type does not match the definition"
        }
        return validateList(list.get(), elementType, definition, includeSuggestions)
    }

    override fun validateValue(
        value: Any,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val values = value as? List<*> ?: return "Invalid List value"
        val elementType = elementType(definition) ?: return "Invalid List element type"
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

    private fun elementType(definition: ResourceConfig.DMInitValue): DataType? =
        DataEntryList.supportedElementType(definition.elementType)

    private fun requireElementType(definition: ResourceConfig.DMInitValue): DataType =
        requireNotNull(elementType(definition)) { "Invalid List element type" }

    private fun minItems(definition: ResourceConfig.DMInitValue): Int = definition.minItems ?: 0

    private fun maxItems(definition: ResourceConfig.DMInitValue): Int =
        definition.maxItems ?: DataTypeHandlers.DEFAULT_MAX_LIST_ITEMS

}

private fun nestedListError() = IllegalArgumentException("Nested lists are not supported")

private fun finite(value: Double): Double = value.also {
    require(it.isFinite()) { "Double value must be finite" }
}

private fun strictBoolean(value: String): Boolean = when (value) {
    "true" -> true
    "false" -> false
    else -> throw IllegalArgumentException("Boolean value must be true or false")
}

private fun validateStringPattern(definition: ResourceConfig.DMInitValue): String? =
    definition.pattern
        ?.takeIf { it.size < 3 || it.take(3).any { part -> part == null } }
        ?.let { "String pattern must contain start, contains and end values" }

private fun validateString(value: String, definition: ResourceConfig.DMInitValue): String? {
    val pattern = definition.pattern ?: return null
    if (pattern.size < 3) {
        return "String pattern must contain start, contains and end values"
    }
    return if (value.matches(pattern[0], pattern[1], pattern[2])) {
        null
    } else {
        "String value does not match the configured pattern"
    }
}

private fun String.matches(start: String, contains: String, end: String): Boolean =
    (start.isEmpty() || startsWith(start)) &&
            (contains.isEmpty() || contains(contains)) &&
            (end.isEmpty() || endsWith(end))

private fun requireFinite(value: Vec3) {
    require(value.x.isFinite() && value.y.isFinite() && value.z.isFinite()) {
        "Vec components must be finite"
    }
}

private fun validateVec(value: Vec3, definition: ResourceConfig.DMInitValue): String? {
    if (!value.x.isFinite() || !value.y.isFinite() || !value.z.isFinite()) {
        return "Vec components must be finite"
    }
    val range = definition.minmax ?: return null
    return if (listOf(value.x, value.y, value.z).all { it in range[0]..range[1] }) {
        null
    } else {
        "Vec component is outside the configured range"
    }
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
