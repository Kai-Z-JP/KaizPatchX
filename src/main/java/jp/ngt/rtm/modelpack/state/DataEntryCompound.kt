package jp.ngt.rtm.modelpack.state

import net.minecraft.nbt.NBTTagCompound

class DataEntryCompound private constructor(
    value: DataCompound,
    flag: Int
) : DataEntry<DataCompound>(value.deepCopy(), flag) {

    override fun readFromNBT(nbt: NBTTagCompound) {
        val compoundTag = nbt.getCompoundTag(DATA_KEY)
        val compound = DataCompound()

        @Suppress("UNCHECKED_CAST")
        val keys = compoundTag.func_150296_c() as Set<String>
        keys.forEach { key ->
            val entryTag = compoundTag.getCompoundTag(key)
            val entry = DataEntry.getEntry(entryTag.getString(TYPE_KEY), "", flag)
                ?: throw IllegalArgumentException("Unknown Compound member type")
            entry.readFromNBT(entryTag)
            compound.putEntry(key, entry)
        }
        val decoded = fromCompound(compound, flag)
        data = decoded.rawValue()
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setString(TYPE_KEY, type.key)
        val compoundTag = NBTTagCompound()
        data.typedEntries().forEach { (key, entry) ->
            val entryTag = NBTTagCompound()
            entry.writeToNBT(entryTag)
            compoundTag.setTag(key, entryTag)
        }
        nbt.setTag(DATA_KEY, compoundTag)
    }

    override fun getType(): DataType = DataType.COMPOUND

    override fun get(): DataCompound = data.deepCopy()

    override fun toString(): String = DataEntryJsonCodec.typedObject(data).toString()

    internal fun rawValue(): DataCompound = data

    companion object {
        private const val DATA_KEY = "Data"
        private const val TYPE_KEY = "Type"

        @JvmStatic
        fun empty(flag: Int): DataEntryCompound = DataEntryCompound(DataCompound(), flag)

        @JvmStatic
        fun fromString(value: String?, flag: Int): DataEntryCompound {
            if (value.isNullOrBlank()) {
                return empty(flag)
            }
            return fromCompound(DataEntryJsonCodec.parseTypedObject(value), flag)
        }

        internal fun fromCompound(value: DataCompound, flag: Int): DataEntryCompound =
            DataEntryCompound(value, flag)
    }
}
