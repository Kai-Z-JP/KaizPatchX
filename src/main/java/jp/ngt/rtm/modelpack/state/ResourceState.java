package jp.ngt.rtm.modelpack.state;

import jp.ngt.rtm.entity.util.CollisionObj;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.12から移植、一部ダミー化
 */
public class ResourceState {
    public final DataMap dataMap = new DataMap();
    public final List<String> exclusionParts = new ArrayList<>();

    private final IModelSelector selector;
    private String name;
    public int color = 0xFFFFFF;

    public ResourceState(IModelSelector par1) {
        this.selector = par1;
        this.dataMap.setEntity(par1);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.getResourceSet();//DM初期化
        this.dataMap.readFromNBT(nbt.getCompoundTag("DataMap"));
        this.setName(nbt.getString("Name"));
        this.color = nbt.getInteger("Color");
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("DataMap", this.dataMap.writeToNBT());
        nbt.setString("Name", this.getName());
        nbt.setInteger("Color", this.color);
        return nbt;
    }

    public String getName() {
        this.setName(this.name);//初期化
        return this.name;
    }

    public void setName(String par1) {
        if (par1 == null || par1.isEmpty()) {
            par1 = "no_name";
        }
        this.name = par1;
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

    public void applyCollison(Entity target, Entity myself, AxisAlignedBB playerAABB, List<AxisAlignedBB> list) {
        if (this.getResourceSet() != null) {
            CollisionObj obj = this.getResourceSet().getCollisionObj();
            if (obj != null) {
                obj.applyCollison(target, myself, playerAABB, list, this.exclusionParts);
            }
        }
    }

    public void addExclusionParts(String... names) {
        for (String name : names) {
            if (!this.exclusionParts.contains(name)) {
                this.exclusionParts.add(name);
            }
        }
    }

    public void removeExclusionParts(String... names) {
        for (String name : names) {
            if (this.exclusionParts.contains(name)) {
                this.exclusionParts.remove(name);
            }
        }
    }
}