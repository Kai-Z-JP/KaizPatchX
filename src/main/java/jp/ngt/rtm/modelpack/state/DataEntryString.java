package jp.ngt.rtm.modelpack.state;

import net.minecraft.nbt.NBTTagCompound;

public final class DataEntryString extends DataEntry<String> {
    public DataEntryString(String value, int flag) {
        super(value, flag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.data = nbt.getString("Data");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("Data", this.data);
        nbt.setString("Type", this.getType().key);
    }

    @Override
    public DataType getType() {
        return DataType.STRING;
    }

    @Override
    public String toString() {
        return this.data;
    }
}