package jp.ngt.rtm.modelpack.state

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
            createElementEntry(elementType, value, flag).writeToNBT(elementTag)
            list.appendTag(elementTag)
        }
        nbt.setTag(DATA_KEY, list)
    }

    override fun getType(): DataType = DataType.LIST

    override fun getTypeName(): String = "List<${elementType.key}>"

    override fun toString(): String {
        val array = JsonArray()
        data.forEach { value -> array.add(toJsonPrimitive(elementType, value)) }
        return array.toString()
    }

    companion object {
        private const val DATA_KEY = "Data"
        private const val ELEMENT_TYPE_KEY = "ElementType"
        @JvmStatic
        fun fromString(value: String?, fallbackElementType: DataType, flag: Int): DataEntryList {
            val type = requireElementType(fallbackElementType)
            if (value.isNullOrBlank()) {
                return DataEntryList(type, emptyList(), flag)
            }

            val root = JsonParser().parse(value)
            require(root.isJsonArray) { "List value must be a JSON array" }
            val values = root.asJsonArray.map { element ->
                require(element.isJsonPrimitive) { "List elements must be primitive values" }
                parseElement(type, element.asString)
            }
            return DataEntryList(type, values, flag)
        }

        @JvmStatic
        fun fromValues(elementType: DataType, values: Collection<*>, flag: Int): DataEntryList {
            val type = requireElementType(elementType)
            return DataEntryList(type, values.map { coerceElement(type, it) }, flag)
        }

        @JvmStatic
        fun supportedElementType(typeName: String?): DataType? =
            typeName?.let(DataType::getType)?.takeIf { it != DataType.LIST }

        @JvmStatic
        fun elementTypeFromTypeName(typeName: String?): DataType? {
            val match = typeName?.let(LIST_TYPE_REGEX::matchEntire) ?: return null
            return supportedElementType(match.groupValues[1])
        }

        @JvmStatic
        fun defaultElementValue(type: DataType): String =
            DataTypeHandlers.defaultElementValue(requireElementType(type))

        @JvmStatic
        fun elementToString(value: Any, type: DataType): String =
            DataTypeHandlers.formatValue(requireElementType(type), value)

        private fun toJsonPrimitive(type: DataType, value: Any): JsonPrimitive = when (type) {
            DataType.INT -> JsonPrimitive(value as Int)
            DataType.DOUBLE -> JsonPrimitive(value as Double)
            DataType.BOOLEAN -> JsonPrimitive(value as Boolean)
            else -> JsonPrimitive(elementToString(value, type))
        }

        private fun parseElement(type: DataType, rawValue: String): Any =
            DataTypeHandlers.parseElementValue(requireElementType(type), rawValue)

        private fun coerceElement(type: DataType, value: Any?): Any =
            DataTypeHandlers.coerceElementValue(requireElementType(type), value)

        private fun createElementEntry(type: DataType, value: Any?, flag: Int): DataEntry<*> =
            DataTypeHandlers.createElementEntry(requireElementType(type), value, flag)

        private fun requireElementType(type: DataType): DataType {
            require(type != DataType.LIST) { "Nested lists are not supported" }
            return type
        }

        private val LIST_TYPE_REGEX = Regex("^List<([a-zA-Z]+)>$")
    }
}
