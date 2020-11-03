package jp.ngt.rtm.electric;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase;
import net.minecraft.util.ResourceLocation;

public class TileEntitySpeaker extends TileEntityMachineBase implements IProvideElectricity {

    public int getElectricity() {
        return 0;
    }

    public void setElectricity(int x, int y, int z, int level) {
        if (!this.worldObj.isRemote) {
            if (level > 0 && level <= 64) {
//                ResourceLocation sound = SpeakerSounds.getInstance(true).getSound(level);
                String name = this.getSound(level);
                ResourceLocation sound = (name.contains(":") ? new ResourceLocation(name.split(":")[0], name.split(":")[1]) : new ResourceLocation(name));
                RTMCore.proxy.playSound(this, sound, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.Speaker;
    }

    @Override
    protected String getDefaultName() {
        return "Speaker01";
    }

    public void setSound(int index, String sound) {
        this.getResourceState().getDataMap().setString(String.valueOf(index), sound, 3);
    }

    public String getSound(int index) {
        return this.getResourceState().getDataMap().getString(String.valueOf(index));
    }
}
