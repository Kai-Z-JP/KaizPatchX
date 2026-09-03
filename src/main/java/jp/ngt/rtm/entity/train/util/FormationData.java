package jp.ngt.rtm.entity.train.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

public class FormationData extends WorldSavedData {
    public FormationData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList tagList = nbt.getTagList("Formations", 10);
        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            long formationId = tag.getLong("FormationId");
            Formation formation = FormationManager.getInstance().getFormation(formationId);
            if (formation == null) {
                Formation.readFromNBT(tag, false);
                //登録はFormationコンストラクタで行ってる
            } else {
                formation.applySavedMetadata(tag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();
        FormationManager.getInstance().getFormations().values().forEach(formation -> {
            NBTTagCompound tag = new NBTTagCompound();
            formation.writeToNBT(tag, false);
            tagList.appendTag(tag);
        });
        nbt.setTag("Formations", tagList);
    }
}
