package jp.ngt.rtm.modelpack.state

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

class DataEntryList private constructor(
    elementType: DataType,
    values: List<Any>,
    flag: Int
) : DataEntry<List<Any>>(values.toList(), flag) {
    var elementType: DataType = requireElementType(elementType)
        private set

    override fun readFromNBT(nbt: NBTTagCompound) {
        elementType = supportedElementType(nbt.getString(ELEMENT_TYPE_KEY)) ?: DataType.STRING
        val list = nbt.getTagList(DATA_KEY, 10)
        val values = ArrayList<Any>(list.tagCount())
        repeat(list.tagCount()) { index ->
            val elementTag = list.getCompoundTagAt(index)
            val entry = createElementEntry(elementType, defaultElementValue(elementType), flag)
            entry.readFromNBT(elementTag)
            values += entry.get()
        }
        data = values
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setString("Type", type.key)
        nbt.setString(ELEMENT_TYPE_KEY, elementType.key)
        val list = NBTTagList()
        data.forEach { value ->
            val elementTag = NBTTagCompound()
            createElementEntry(elementType, elementToString(value, elementType), flag).writeToNBT(elementTag)
            list.appendTag(elementTag)
        }
        nbt.setTag(DATA_KEY, list)
    }

    override fun getType(): DataType = DataType.LIST

    override fun getTypeName(): String = typeName(elementType)

    override fun toString(): String = encode(elementType, data)

    companion object {
        private const val DATA_KEY = "Data"
        private const val ELEMENT_TYPE_KEY = "ElementType"
        private val gson = Gson()

        @JvmStatic
        fun fromString(value: String?, fallbackElementType: DataType, flag: Int): DataEntryList {
            val decoded = decode(value, fallbackElementType)
            return fromStrings(decoded.first, decoded.second, flag)
        }

        @JvmStatic
        fun fromStrings(elementType: DataType, values: Collection<String>, flag: Int): DataEntryList {
            val type = requireElementType(elementType)
            return DataEntryList(type, values.map { parseElement(type, it) }, flag)
        }

        @JvmStatic
        fun fromValues(elementType: DataType, values: Collection<*>, flag: Int): DataEntryList {
            val type = requireElementType(elementType)
            return DataEntryList(type, values.map { coerceElement(type, it) }, flag)
        }

        @JvmStatic
        fun decodeStrings(value: String?, fallbackElementType: DataType): Pair<DataType, List<String>> =
            decode(value, fallbackElementType)

        @JvmStatic
        fun encodeStrings(elementType: DataType, values: Collection<String>): String =
            encodeRaw(requireElementType(elementType), values)

        @JvmStatic
        fun supportedElementType(typeName: String?): DataType? =
            typeName?.let(DataType::getType)?.takeIf { it != DataType.LIST }

        @JvmStatic
        fun elementTypeFromTypeName(typeName: String?): DataType? {
            val match = typeName?.let(LIST_TYPE_REGEX::matchEntire) ?: return null
            return supportedElementType(match.groupValues[1])
        }

        @JvmStatic
        fun typeName(elementType: DataType): String = "List<${requireElementType(elementType).key}>"

        @JvmStatic
        fun defaultElementValue(type: DataType): String =
            DataTypeHandlers.defaultElementValue(requireElementType(type))

        @JvmStatic
        fun elementToString(value: Any, type: DataType): String =
            DataTypeHandlers.formatValue(requireElementType(type), value)

        private fun decode(value: String?, fallbackElementType: DataType): Pair<DataType, List<String>> {
            val fallback = requireElementType(fallbackElementType)
            if (value.isNullOrBlank()) {
                return fallback to emptyList()
            }

            val root = JsonParser().parse(value)
            if (!root.isJsonArray) {
                throw IllegalArgumentException("List value must be a JSON array")
            }
            val array: JsonArray = root.asJsonArray

            val values = array.map { element ->
                if (!element.isJsonPrimitive) {
                    throw IllegalArgumentException("List elements must be primitive values")
                }
                element.asString
            }
            return fallback to values
        }

        private fun encode(elementType: DataType, values: Collection<Any>): String {
            return encodeRaw(elementType, values.map { value -> elementToString(value, elementType) })
        }

        private fun encodeRaw(elementType: DataType, values: Collection<String>): String {
            val type = requireElementType(elementType)
            val array = JsonArray()
            values.forEach { value -> array.add(toJsonPrimitive(type, value)) }
            return gson.toJson(array)
        }

        private fun toJsonPrimitive(type: DataType, value: String): JsonPrimitive = when (type) {
            DataType.INT -> value.toIntOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(value)
            DataType.DOUBLE -> value.toDoubleOrNull()
                ?.takeIf(Double::isFinite)
                ?.let(::JsonPrimitive)
                ?: JsonPrimitive(value)

            DataType.BOOLEAN -> when (value) {
                "true" -> JsonPrimitive(true)
                "false" -> JsonPrimitive(false)
                else -> JsonPrimitive(value)
            }

            else -> JsonPrimitive(value)
        }

        private fun parseElement(type: DataType, rawValue: String): Any =
            DataTypeHandlers.parseElementValue(requireElementType(type), rawValue)

        private fun coerceElement(type: DataType, value: Any?): Any =
            DataTypeHandlers.coerceElementValue(requireElementType(type), value)

        private fun createElementEntry(type: DataType, value: String, flag: Int): DataEntry<*> =
            DataTypeHandlers.createElementEntry(requireElementType(type), value, flag)

        private fun requireElementType(type: DataType): DataType {
            require(type != DataType.LIST) { "Nested lists are not supported" }
            return type
        }

        private val LIST_TYPE_REGEX = Regex("^List<([a-zA-Z]+)>$")
    }
}
