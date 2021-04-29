package jp.ngt.rtm.modelpack.state;

import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 1.12から移植、一部ダミー化
 */
public class ResourceState {
    public final DataMap dataMap = new DataMap();

    private final IModelSelector selector;

    public ResourceState(IModelSelector par1) {
        this.selector = par1;
        this.dataMap.setEntity(par1);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.getResourceSet();//DM初期化
        this.dataMap.readFromNBT(nbt.getCompoundTag("DataMap"));
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("DataMap", this.dataMap.writeToNBT());
        return nbt;
    }

    public String getName() {
        return "";
    }

    public String getArg() {
        return this.dataMap.getArg();
    }

    public void setArg(String par1, boolean overwrite) {
        this.dataMap.setArg(par1, overwrite);
    }

    public String getResourceName() {
        return this.selector.getModelName();
    }

    public ModelSetBase getResourceSet() {
        return this.selector.getModelSet();
    }

    public DataMap getDataMap() {
        return this.dataMap;
    }
}