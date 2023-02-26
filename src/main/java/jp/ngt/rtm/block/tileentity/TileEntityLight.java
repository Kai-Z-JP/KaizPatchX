package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.render.MachinePartsRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

public class TileEntityLight extends TileEntityMachineBase {
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        boolean b = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
        if (this.isGettingPower ^ b) {
            this.isGettingPower = b;
            this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);//明るさ更新
        }
    }

    @Override
    public Vec3 getNormal(float x, float y, float z, float pitch, float yaw) {
        if (this.normal == null) {
            this.normal = Vec3.createVectorHelper(x, y, z);
            MachinePartsRenderer.rotateVec(this.normal,
                    this.getBlockMetadata(), pitch, yaw);
        }
        return this.normal;
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