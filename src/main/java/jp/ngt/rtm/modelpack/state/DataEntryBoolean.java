package jp.ngt.rtm.modelpack.state;

import net.minecraft.nbt.NBTTagCompound;

public final class DataEntryBoolean extends DataEntry<Boolean> {
	public DataEntryBoolean(Boolean value, int flag) {
		super(value, flag);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.data = nbt.getBoolean("Data");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("Data", this.data);
		nbt.setString("Type", this.getType().key);
	}

	@Override
	public DataType getType() {
		return DataType.BOOLEAN;
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
}