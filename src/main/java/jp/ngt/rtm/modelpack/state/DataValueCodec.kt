package jp.ngt.rtm.modelpack.state

import jp.ngt.ngtlib.math.Vec3
import java.lang.reflect.Array as ReflectArray

internal object DataValueCodec {
    fun entryValue(entry: DataEntry<*>): Any = when (entry) {
        is DataEntryCompound -> entry.get()
        is DataEntryList -> entry.get()
        is DataEntryVec -> copyVec(entry.get())
        else -> entry.get()
    }

    fun copyValue(value: Any): Any = when (value) {
        is DataCompound -> value.deepCopy()
        is Vec3 -> copyVec(value)
        else -> value
    }

    fun copyEntry(entry: DataEntry<*>): DataEntry<*> = when (entry) {
        is DataEntryHex -> DataEntryHex(entry.get(), entry.flag)
        is DataEntryInt -> DataEntryInt(entry.get(), entry.flag)
        is DataEntryDouble -> DataEntryDouble(entry.get(), entry.flag)
        is DataEntryBoolean -> DataEntryBoolean(entry.get(), entry.flag)
        is DataEntryString -> DataEntryString(entry.get(), entry.flag)
        is DataEntryVec -> DataEntryVec(copyVec(entry.get()), entry.flag)
        is DataEntryList -> DataEntryList.fromValues(entry.elementType, entry.get(), entry.flag)
        is DataEntryCompound -> DataEntryCompound.fromCompound(entry.rawValue(), entry.flag)
        else -> throw IllegalArgumentException("Unsupported data entry: ${entry.javaClass.name}")
    }

    fun entryFromAny(
        value: Any,
        existing: DataEntry<*>?,
        flag: Int
    ): DataEntry<*> {
        if (value is DataEntry<*>) {
            return copyEntry(value)
        }
        existing?.let { return coerceExisting(value, it, flag) }

        return when (value) {
            is Boolean -> DataEntryBoolean(value, flag)
            is Byte, is Short, is Int -> DataEntryInt((value as Number).toInt(), flag)
            is Long -> {
                require(value in Int.MIN_VALUE..Int.MAX_VALUE) { "Integer value is out of range" }
                DataEntryInt(value.toInt(), flag)
            }

            is Float, is Double -> DataEntryDouble((value as Number).toDouble(), flag)
            is String -> DataEntryString(value, flag)
            is Vec3 -> DataEntryVec(copyVec(value), flag)
            is DataCompound -> DataEntryCompound.fromCompound(value, flag)
            is Collection<*> -> inferList(value.toList(), flag)
            else -> javaArrayValues(value)?.let { inferList(it, flag) }
                ?: throw IllegalArgumentException("Unsupported Compound value: ${value.javaClass.name}")
        }
    }

    private fun coerceExisting(
        value: Any,
        existing: DataEntry<*>,
        flag: Int
    ): DataEntry<*> = when (existing) {
        is DataEntryCompound -> {
            val compound = value as? DataCompound
                ?: throw IllegalArgumentException("Compound value is required")
            DataEntryCompound.fromCompound(compound, flag)
        }

        is DataEntryList -> {
            val values = valueAsList(value) ?: throw IllegalArgumentException("List value is required")
            val normalized = values.mapIndexed { index, child ->
                val nonNull = child ?: throw IllegalArgumentException("List values must not be null")
                val template = if (existing.elementType == DataType.COMPOUND) {
                    (existing.rawValues().getOrNull(index) as? DataCompound)
                        ?.let { DataEntryCompound.fromCompound(it, flag) }
                        ?: defaultEntry(existing.elementType, flag)
                } else {
                    defaultEntry(existing.elementType, flag)
                }
                entryValue(entryFromAny(nonNull, template, flag))
            }
            DataEntryList.fromValues(existing.elementType, normalized, flag)
        }

        else -> DataTypeHandlers.createElementEntry(existing.type, value, flag)
    }

    private fun inferList(
        values: List<*>,
        flag: Int
    ): DataEntryList {
        if (values.isEmpty()) {
            return DataEntryList.fromValues(DataType.STRING, emptyList<Any>(), flag)
        }
        val entries = values.map { value ->
            entryFromAny(value ?: throw IllegalArgumentException("List values must not be null"), null, flag)
                .also { require(it.type != DataType.LIST) { "Nested lists are not supported" } }
        }
        val types = entries.map(DataEntry<*>::getType).toSet()
        val elementType = when {
            types.size == 1 -> types.single()
            types.all { it == DataType.INT || it == DataType.DOUBLE } -> DataType.DOUBLE
            else -> throw IllegalArgumentException("List values must have one element type")
        }
        val rawValues = entries.map { entry ->
            if (elementType == DataType.DOUBLE && entry.type == DataType.INT) {
                (entry.get() as Number).toDouble()
            } else {
                entryValue(entry)
            }
        }
        return DataEntryList.fromValues(elementType, rawValues, flag)
    }

    private fun defaultEntry(type: DataType, flag: Int): DataEntry<*> = when (type) {
        DataType.INT -> DataEntryInt(0, flag)
        DataType.DOUBLE -> DataEntryDouble(0.0, flag)
        DataType.BOOLEAN -> DataEntryBoolean(false, flag)
        DataType.STRING -> DataEntryString("", flag)
        DataType.VEC -> DataEntryVec(Vec3.ZERO, flag)
        DataType.HEX -> DataEntryHex(0, flag)
        DataType.COMPOUND -> DataEntryCompound.empty(flag)
        DataType.LIST -> throw IllegalArgumentException("Nested lists are not supported")
    }

    private fun valueAsList(value: Any): List<*>? = (value as? Collection<*>)?.toList()
        ?: javaArrayValues(value)

    private fun javaArrayValues(value: Any): List<*>? {
        if (!value.javaClass.isArray) {
            return null
        }
        return List(ReflectArray.getLength(value)) { index -> ReflectArray.get(value, index) }
    }

    private fun copyVec(value: Vec3): Vec3 = Vec3(value.x, value.y, value.z)
}
