package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.ConnectorConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetConnector;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileEntityConnectorBase extends TileEntityElectricalWiring implements IModelSelectorWithType {
    private final ResourceState state = new ResourceState(this);
    private String modelName = "";
    private ModelSetConnector myModelSet;
    public Vec3 wirePos = Vec3.ZERO;

    public TileEntityConnectorBase() {
    }

    public TileEntityConnectorBase(int meta) {
        this.modelName = this.getDefaultName();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String name;
        if (nbt.hasKey("ModelName") || !this.hasWorldObj()) {
            name = nbt.getString("ModelName");
        } else {
            name = this.getDefaultName();
        }
        this.setModelName(name);
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.getModelName().isEmpty() && this.hasWorldObj()) {
            this.setModelName(this.getDefaultName());
        }
        nbt.setString("ModelName", this.modelName);
        nbt.setTag("State", this.getResourceState().writeToNBT());
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
    }

    @Override
    public ModelSetConnector getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy()) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelConnector", this.modelName);
            if (!this.myModelSet.isDummy()) {
                this.myModelSet.dataFormatter.initDataMap(this.getResourceState().getDataMap());
            }
            if (this.worldObj == null || !this.worldObj.isRemote) {
                PacketNBT.sendToClient(this);
            }

            //readNBT時ぬるぽ回避
            if (this.worldObj != null && this.myModelSet != null) {
                this.updateWirePos(this.myModelSet.getConfig());
            }
        }
        return this.myModelSet;
    }

    public void updateWirePos(ConnectorConfig cfg) {
        if (cfg == null) {
            cfg = this.getModelSet().getConfig();
        }
        Vec3 vec = PooledVec3.create(cfg.wirePos[0], cfg.wirePos[1], cfg.wirePos[2]);
        int meta = this.getBlockMetadata();
        switch (meta) {
            case 0:
                vec = vec.rotateAroundZ((180.0F));
                break;
            case 1:
                break;
            case 2://Z
                vec = vec.rotateAroundX(-90.0F);
                vec = vec.rotateAroundY(180.0F);
                break;
            case 3://Z
                vec = vec.rotateAroundX(-90.0F);
                break;
            case 4://X
                vec = vec.rotateAroundX(-90.0F);
                vec = vec.rotateAroundY(-90.0F);
                break;
            case 5://X
                vec = vec.rotateAroundX(-90.0F);
                vec = vec.rotateAroundY(90.0F);
                break;
        }
        vec = vec.rotateAroundY(this.getRotation());
        vec = vec.add(this.getOffsetX(), this.getOffsetY(), this.getOffsetZ());
        this.wirePos = vec;
    }

    @Override
    public String getModelType() {
        return "ModelConnector";
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public void setModelName(String par1) {
        this.modelName = par1;
        this.myModelSet = null;
        this.getDescriptionPacket();
    }

    @Override
    public int[] getPos() {
        return new int[]{this.xCoord, this.yCoord, this.zCoord};
    }

    @Override
    public boolean closeGui(String par1, ResourceState par2) {
        this.setModelName(par1);
        return true;
    }

    @Override
    public ResourceState getResourceState() {
        return this.state;
    }

    protected abstract String getDefaultName();
}