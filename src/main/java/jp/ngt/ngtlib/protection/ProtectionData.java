package jp.ngt.ngtlib.protection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ProtectionData extends WorldSavedData {
    private final Map<String, NBTTagCompound> protectedObjs = new HashMap<>();

    public ProtectionData(String par1) {
        super(par1);
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList tagList = nbt.getTagList("Objects", 10);
        IntStream.range(0, tagList.tagCount()).mapToObj(tagList::getCompoundTagAt).forEach(tagElement -> {
            String objName = tagElement.getString("ObjName");
            this.protectedObjs.put(objName, tagElement);
        });
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();
        this.protectedObjs.forEach((key, tagElement) -> {
            tagElement.setString("ObjName", key);
            tagList.appendTag(tagElement);
        });
        nbt.setTag("Objects", tagList);
    }

    public boolean hasObject(String id) {
        return this.protectedObjs.containsKey(id);
    }

    public NBTTagCompound getObject(String id) {
        return this.protectedObjs.get(id);
    }

    public void setObject(String id, NBTTagCompound data) {
        this.protectedObjs.put(id, data);
        this.markDirty();
    }

    public void removeObject(String id) {
        this.protectedObjs.remove(id);
        this.markDirty();
    }

    public Map<String, NBTTagCompound> getDatas() {
        return this.protectedObjs;
    }
}