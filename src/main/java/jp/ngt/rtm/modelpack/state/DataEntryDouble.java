package jp.ngt.rtm.modelpack.state;

import net.minecraft.nbt.NBTTagCompound;

public final class DataEntryDouble extends DataEntry<Double> {
    public DataEntryDouble(Double value, int flag) {
        super(value, flag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.data = nbt.getDouble("Data");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setDouble("Data", this.data);
        nbt.setString("Type", this.getType().key);
    }

    @Override
    public DataType getType() {
        return DataType.DOUBLE;
    }

    @Override
    public String toString() {
        return this.data.toString();
    }
}