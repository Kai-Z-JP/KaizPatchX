package jp.ngt.rtm.modelpack.state;

import jp.ngt.ngtlib.math.Vec3;
import net.minecraft.nbt.NBTTagCompound;

public final class DataEntryVec extends DataEntry<Vec3> {
    public DataEntryVec(Vec3 value, int flag) {
        super(value, flag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.data = fromString(nbt.getString("Data"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("Data", this.toString());
        nbt.setString("Type", this.getType().key);
    }

    @Override
    public DataType getType() {
        return DataType.VEC;
    }

    @Override
    public String toString() {
        return "" + this.data.getX() + " " + this.data.getY() + " " + this.data.getZ();
    }

    public static Vec3 fromString(String par1) {
        String[] sa = par1.split(" ");
        return new Vec3(Double.parseDouble(sa[0]), Double.parseDouble(sa[1]), Double.parseDouble(sa[2]));
    }
}