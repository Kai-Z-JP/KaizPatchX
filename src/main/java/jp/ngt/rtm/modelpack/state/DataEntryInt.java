package jp.ngt.rtm.modelpack.state;

import net.minecraft.nbt.NBTTagCompound;

public class DataEntryInt extends DataEntry<Integer> {
    public DataEntryInt(Integer value, int flag) {
        super(value, flag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.data = nbt.getInteger("Data");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Data", this.data);
        nbt.setString("Type", this.getType().key);
    }

    @Override
    public DataType getType() {
        return DataType.INT;
    }

    @Override
    public String toString() {
        return this.data.toString();
    }
}