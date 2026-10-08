package jp.ngt.rtm.modelpack.state

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import jp.ngt.rtm.modelpack.cfg.ResourceConfig

internal object CompoundDataTypeHandler : DataTypeHandler {
    override val type: DataType = DataType.COMPOUND

    override fun parse(
        rawValue: String,
        definition: ResourceConfig.DMInitValue,
        flag: Int
    ): DataEntry<*> = DataEntryCompound.fromString(rawValue, flag)

    override fun createDefault(definition: ResourceConfig.DMInitValue, flag: Int): DataEntry<*> =
        DataEntryCompound.fromCompound(DataCompoundDefinitions.createDefault(definition), flag)

    override fun validateConstraints(definition: ResourceConfig.DMInitValue): String? =
        DataCompoundDefinitions.validateDefinition(definition)

    override fun validateEntry(
        entry: DataEntry<*>,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val compound = entry as? DataEntryCompound ?: return "Expected Compound value"
        return DataCompoundDefinitions.validateValue(compound.rawValue(), definition, includeSuggestions)
    }

    override fun validateValue(
        value: Any,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val compound = value as? DataCompound ?: return "Invalid Compound value"
        return DataCompoundDefinitions.validateValue(compound, definition, includeSuggestions)
    }

    override fun format(value: Any): String = when (value) {
        is DataEntryCompound -> value.toString()
        is DataCompound -> DataEntryJsonCodec.typedObject(value).toString()
        else -> throw IllegalArgumentException("Compound value is required")
    }

    override fun parseElement(rawValue: String): Any = DataEntryCompound.fromString(rawValue, 0).get()

    override fun coerceElement(value: Any?): Any = when (value) {
        is DataCompound -> value.deepCopy()
        is String -> DataEntryCompound.fromString(value, 0).get()
        else -> throw IllegalArgumentException("Compound value is required")
    }

    override fun createElementEntry(value: Any?, flag: Int): DataEntry<*> =
        DataEntryCompound.fromCompound(coerceElement(value) as DataCompound, flag)

    override fun defaultElementValue(): String = "{}"
}

internal object DataCompoundDefinitions {
    fun createDefault(definition: ResourceConfig.DMInitValue): DataCompound {
        val result = DataCompound()
        definition.entries.orEmpty().forEach { child ->
            val key = child.key.orEmpty()
            val type = DataType.getType(child.type)
                ?: throw IllegalArgumentException("Unknown Compound member type: ${child.type}")
            result.putEntry(key, DataTypeHandlers.createDefault(type, child, 0))
        }
        return result
    }

    fun fromPlainJson(value: JsonObject, definition: ResourceConfig.DMInitValue): DataCompound {
        val result = createDefault(definition)
        val definitions = definition.entries.orEmpty().associateBy { it.key.orEmpty() }
        value.entrySet().forEach { member ->
            val childDefinition = definitions[member.key]
            val entry = if (childDefinition == null) {
                DataValueCodec.entryFromAny(plainValue(member.value), result.getEntry(member.key), 0)
            } else {
                entryFromPlainJson(member.value, childDefinition)
            }
            result.putEntry(member.key, entry)
        }
        return result
    }

    fun validateDefinition(definition: ResourceConfig.DMInitValue): String? {
        val keys = HashSet<String>()
        definition.entries.orEmpty().forEach { child ->
            val key = child.key.orEmpty()
            if (!keys.add(key)) {
                return "Duplicate Compound member: $key"
            }
            val type = DataType.getType(child.type) ?: return "Unknown Compound member type: $key"
            DataTypeHandlers.validateDefinition(type, child)?.let { return "$key: $it" }
        }
        return null
    }

    fun validateValue(
        value: DataCompound,
        definition: ResourceConfig.DMInitValue,
        includeSuggestions: Boolean
    ): String? {
        val definitions = definition.entries.orEmpty().associateBy { it.key.orEmpty() }
        value.typedEntries().forEach { (key, entry) ->
            val child = definitions[key] ?: return@forEach
            val type = DataType.getType(child.type) ?: return "Unknown Compound member type: $key"
            if (entry.type != type) {
                return "Compound member type does not match the definition: $key"
            }
            val handler = DataTypeHandlers.get(type)
            handler.validateConstraints(child)?.let { return "$key: $it" }
            handler.validateEntry(entry, child, includeSuggestions)?.let { return "$key: $it" }
        }
        return null
    }

    fun findDefinition(definition: ResourceConfig.DMInitValue, path: List<String>): ResourceConfig.DMInitValue? {
        var current = definition
        path.forEach { segment ->
            if (DataType.getType(current.type) != DataType.COMPOUND) {
                return null
            }
            current = current.entries.orEmpty().firstOrNull { it.key == segment } ?: return null
        }
        return current
    }

    fun findDefinition(
        entries: Array<ResourceConfig.DMInitValue>,
        path: List<String>
    ): ResourceConfig.DMInitValue? {
        if (path.isEmpty()) return null
        var current = entries.firstOrNull { it.key == path.first() } ?: return null
        path.drop(1).forEach { segment ->
            if (DataType.getType(current.type) != DataType.COMPOUND) return null
            current = current.entries.orEmpty().firstOrNull { it.key == segment } ?: return null
        }
        return current
    }

    private fun entryFromPlainJson(
        value: JsonElement,
        definition: ResourceConfig.DMInitValue
    ): DataEntry<*> {
        val type = DataType.getType(definition.type)
            ?: throw IllegalArgumentException("Unknown Compound member type")
        return when (type) {
            DataType.COMPOUND -> {
                require(value.isJsonObject) { "Compound member must be an object" }
                DataEntryCompound.fromCompound(fromPlainJson(value.asJsonObject, definition), 0)
            }

            DataType.LIST -> {
                require(value.isJsonArray) { "List member must be an array" }
                val elementType = DataEntryList.supportedElementType(definition.elementType)
                    ?: throw IllegalArgumentException("Invalid List element type")
                if (elementType == DataType.COMPOUND) {
                    val compounds = value.asJsonArray.map { element ->
                        require(element.isJsonObject) { "List<Compound> element must be an object" }
                        fromPlainJson(element.asJsonObject, definition)
                    }
                    DataEntryList.fromValues(elementType, compounds, 0)
                } else {
                    DataEntryList.fromString(value.toString(), elementType, 0)
                }
            }

            else -> DataTypeHandlers.get(type).parse(value.asString, definition, 0)
        }
    }

    private fun plainValue(value: JsonElement): Any = when {
        value.isJsonObject -> DataCompound().also { compound ->
            value.asJsonObject.entrySet().forEach { member ->
                compound.putEntry(member.key, DataValueCodec.entryFromAny(plainValue(member.value), null, 0))
            }
        }

        value.isJsonArray -> value.asJsonArray.map(::plainValue)
        !value.isJsonPrimitive -> throw IllegalArgumentException("Null Compound values are not supported")
        value.asJsonPrimitive.isBoolean -> value.asBoolean
        value.asJsonPrimitive.isString -> value.asString
        else -> value.asString.toLongOrNull()?.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
            ?: value.asDouble
    }

}
