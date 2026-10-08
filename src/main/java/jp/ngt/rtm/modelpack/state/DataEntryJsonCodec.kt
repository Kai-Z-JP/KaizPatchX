package jp.ngt.rtm.modelpack.state

import com.google.gson.*
import jp.ngt.ngtlib.math.Vec3

internal object DataEntryJsonCodec {
    fun typedObject(value: DataCompound): JsonObject {
        val result = JsonObject()
        value.typedEntries().toSortedMap().forEach { (key, entry) ->
            val node = JsonObject()
            node.addProperty("type", entry.typeName)
            node.add("value", typedValue(entry))
            result.add(key, node)
        }
        return result
    }

    fun parseTypedObject(value: String): DataCompound {
        val root = JsonParser().parse(value)
        require(root.isJsonObject) { "Compound value must be a JSON object" }
        return parseTypedObject(root.asJsonObject)
    }

    fun parseTypedObject(value: JsonObject): DataCompound {
        val result = DataCompound()
        value.entrySet().forEach { member ->
            val node = member.value
            require(node.isJsonObject) { "Compound members must contain a type and value" }
            val nodeObject = node.asJsonObject
            val type = nodeObject.get("type")?.asString
                ?: throw IllegalArgumentException("Compound member type is missing")
            val rawValue = nodeObject.get("value")
                ?: throw IllegalArgumentException("Compound member value is missing")
            result.putEntry(member.key, entryFromJson(type, rawValue))
        }
        return result
    }

    fun plainObject(value: DataCompound): JsonObject {
        val result = JsonObject()
        value.typedEntries().toSortedMap().forEach { (key, entry) -> result.add(key, plainValue(entry)) }
        return result
    }

    fun typedListElement(value: Any, elementType: DataType): JsonElement = when (elementType) {
        DataType.COMPOUND -> typedObject(value as DataCompound)
        else -> scalarValue(value, elementType)
    }

    fun parseTypedListElement(value: JsonElement, elementType: DataType): Any = when (elementType) {
        DataType.COMPOUND -> parseTypedObject(value.asJsonObject)
        else -> DataTypeHandlers.parseElementValue(elementType, value.asString)
    }

    private fun entryFromJson(typeName: String, value: JsonElement): DataEntry<*> {
        val listElementType = DataEntryList.elementTypeFromTypeName(typeName)
        if (listElementType != null) {
            return DataEntryList.fromString(value.toString(), listElementType, 0)
        }
        return when (val type = DataType.getType(typeName)) {
            DataType.COMPOUND -> DataEntryCompound.fromCompound(parseTypedObject(value.asJsonObject), 0)
            null -> throw IllegalArgumentException("Unknown Compound member type: $typeName")
            else -> DataEntry.getEntry(type.key, value.asString, 0)
                ?: throw IllegalArgumentException("Unknown Compound member type: $typeName")
        }
    }

    private fun typedValue(entry: DataEntry<*>): JsonElement = when (entry) {
        is DataEntryCompound -> typedObject(entry.rawValue())
        is DataEntryList -> {
            val array = JsonArray()
            entry.rawValues().forEach { value -> array.add(typedListElement(value, entry.elementType)) }
            array
        }

        is DataEntryInt -> JsonPrimitive(entry.get())
        is DataEntryDouble -> JsonPrimitive(entry.get())
        is DataEntryBoolean -> JsonPrimitive(entry.get())
        is DataEntryString -> JsonPrimitive(entry.get())
        else -> JsonPrimitive(entry.toString())
    }

    private fun plainValue(entry: DataEntry<*>): JsonElement = when (entry) {
        is DataEntryCompound -> plainObject(entry.rawValue())
        is DataEntryList -> {
            val array = JsonArray()
            entry.rawValues().forEach { value ->
                array.add(
                    if (entry.elementType == DataType.COMPOUND) {
                        plainObject(value as DataCompound)
                    } else {
                        plainScalarValue(value, entry.elementType)
                    }
                )
            }
            array
        }

        is DataEntryVec -> vecValue(entry.get())
        is DataEntryHex -> JsonPrimitive(entry.get())
        is DataEntryInt -> JsonPrimitive(entry.get())
        is DataEntryDouble -> JsonPrimitive(entry.get())
        is DataEntryBoolean -> JsonPrimitive(entry.get())
        is DataEntryString -> JsonPrimitive(entry.get())
        else -> JsonPrimitive(entry.toString())
    }

    private fun scalarValue(value: Any, type: DataType): JsonElement = when (type) {
        DataType.INT -> JsonPrimitive(value as Int)
        DataType.DOUBLE -> JsonPrimitive(value as Double)
        DataType.BOOLEAN -> JsonPrimitive(value as Boolean)
        DataType.HEX -> JsonPrimitive("0x${(value as Int).toString(16)}")
        DataType.VEC -> JsonPrimitive(DataTypeHandlers.formatValue(type, value))
        DataType.STRING -> JsonPrimitive(value as String)
        else -> throw IllegalArgumentException("Unsupported scalar type: ${type.key}")
    }

    private fun plainScalarValue(value: Any, type: DataType): JsonElement = when (type) {
        DataType.HEX -> JsonPrimitive(value as Int)
        DataType.VEC -> vecValue(value as Vec3)
        else -> scalarValue(value, type)
    }

    private fun vecValue(value: Vec3): JsonArray = JsonArray().also { array ->
        array.add(JsonPrimitive(value.x))
        array.add(JsonPrimitive(value.y))
        array.add(JsonPrimitive(value.z))
    }
}
