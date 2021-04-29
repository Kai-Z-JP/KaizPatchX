package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.NGTVec;
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
    public NGTVec wirePos;

    public TileEntityConnectorBase() {
    }

    public TileEntityConnectorBase(int meta) {
        this.modelName = this.getDefaultName();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String name = nbt.getString("ModelName");
        this.setModelName(name);
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
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
            if (this.worldObj == null || !this.worldObj.isRemote) {
                PacketNBT.sendToClient(this);
            }

            if (this.worldObj != null)//readNBT時ぬるぽ回避
            {
                ConnectorConfig cfg = this.myModelSet.getConfig();
                this.wirePos = new NGTVec(cfg.wirePos[0], cfg.wirePos[1], cfg.wirePos[2]);
                int meta = this.getBlockMetadata();
                switch (meta) {
                    case 0:
                        this.wirePos.rotateAroundZ(NGTMath.toRadians(180.0F));
                        break;
                    case 1:
                        break;
                    case 2://Z
                        this.wirePos.rotateAroundX(NGTMath.toRadians(-90.0F));
                        this.wirePos.rotateAroundY(NGTMath.toRadians(180.0F));
                        break;
                    case 3://Z
                        this.wirePos.rotateAroundX(NGTMath.toRadians(-90.0F));
                        break;
                    case 4://X
                        this.wirePos.rotateAroundX(NGTMath.toRadians(-90.0F));
                        this.wirePos.rotateAroundY(NGTMath.toRadians(-90.0F));
                        break;
                    case 5://X
                        this.wirePos.rotateAroundX(NGTMath.toRadians(-90.0F));
                        this.wirePos.rotateAroundY(NGTMath.toRadians(90.0F));
                        break;
                }
            }
        }
        return this.myModelSet;
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