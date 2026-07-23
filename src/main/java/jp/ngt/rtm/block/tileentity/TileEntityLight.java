package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.modelpack.DataFormProvider;
import jp.ngt.rtm.modelpack.cfg.DataFormConfig;
import jp.ngt.rtm.render.MachinePartsRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

public class TileEntityLight extends TileEntityMachineBase implements DataFormProvider {
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
    }

    @Override
    public Vec3 getNormal(float x, float y, float z, float pitch, float yaw) {
        if (this.normal == null) {
            this.normal = Vec3.createVectorHelper(x, y, z);
            MachinePartsRenderer.rotateVec(this.normal, this.getBlockMetadata(), pitch, yaw);
        }
        return this.normal;
    }

    @Override
    public DataFormConfig getDataFormConfig() {
        return this.getModelSet().getConfig().customForm;
    }

    @Override
    public String getDataFormPermission() {
        return RTMCore.CHANGE_MODEL;
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.Light;
    }

    @Override
    protected String getDefaultName() {
        return "SearchLight01";
    }
}