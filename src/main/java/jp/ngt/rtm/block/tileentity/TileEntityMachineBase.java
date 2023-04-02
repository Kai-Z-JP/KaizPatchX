package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.ScriptExecuter;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachine;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachineClient;
import jp.ngt.rtm.modelpack.state.ResourceState;
import jp.ngt.rtm.render.ModelObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TileEntityMachineBase extends TileEntityPlaceable implements IModelSelectorWithType {
    private final ResourceState state = new ResourceState(this);
    private ModelSetMachine myModelSet;
    private String modelName = "";
    private float pitch;
    private final ScriptExecuter executer = new ScriptExecuter();

    public int tick;
    public boolean isGettingPower;
    protected Vec3 normal;

    /**
     * メタで保存してた方向データを更新したか
     */
    private boolean yawFixed;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String s = nbt.getString("ModelName");
        if (s == null || s.length() == 0) {
            s = this.getDefaultName();
        }
        this.setModelName(s);
        this.pitch = nbt.getFloat("Pitch");

        this.yawFixed = nbt.hasKey("Yaw");
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("ModelName", this.modelName);
        nbt.setFloat("Pitch", this.pitch);
        nbt.setTag("State", this.getResourceState().writeToNBT());
    }

    @Override
    public void updateEntity() {
        ++this.tick;
        if (this.tick == Integer.MAX_VALUE) {
            this.tick = 0;
        }

        if (!this.getWorldObj().isRemote) {
            this.executer.execScript(this);
        }
    }


    @Override
    public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
        super.setRotation(player, rotationInterval, synch);
        this.pitch = -player.rotationPitch;
    }

    @Override
    public void setRotation(float par1, boolean synch) {
        super.setRotation(par1, synch);
        this.yawFixed = true;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Vec3 getNormal(float x, float y, float z, float pitch, float yaw) {
        if (this.normal == null) {
            this.normal = Vec3.createVectorHelper(x, y, z);
        }
        return this.normal;
    }

    /**
     * 右クリック時
     */
    public void onActivate() {
        if (this.worldObj.isRemote && this.getModelSet().sound_OnActivate != null) {
            RTMCore.proxy.playSound(this, this.getModelSet().sound_OnActivate, 1.0F, 1.0F);
        }
    }

    public abstract MachineType getMachineType();

    @Override
    public boolean shouldRenderInPass(int pass) {
        ModelSetMachineClient modelSet = (ModelSetMachineClient) this.getModelSet();
        ModelObject modelObj = modelSet.modelObj;
        return pass == 0 || (modelObj.light || modelObj.alphaBlend) && pass >= 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        float[] box = this.getResourceState().getResourceSet().getConfig().renderAABB;

        float rotation = this.getRotation();
        float rad = NGTMath.toRadians(rotation);

        List<Vec3> vertexList = IntStream.range(0, 4).mapToObj(i -> {
            Vec3 vec = Vec3.createVectorHelper(box[i / 2 * 3] - 0.5, 0, box[i % 2 * 3 + 2] - 0.5);
            vec.rotateAroundY(rad);
            return vec.addVector(0.5, 0.0, 0.5);
        }).collect(Collectors.toList());

        return AxisAlignedBB.getBoundingBox(
                        vertexList.stream().mapToDouble(vec -> vec.xCoord).min().orElse(0.0),
                        box[1],
                        vertexList.stream().mapToDouble(vec -> vec.zCoord).min().orElse(0.0),
                        vertexList.stream().mapToDouble(vec -> vec.xCoord).max().orElse(1.0),
                        box[4],
                        vertexList.stream().mapToDouble(vec -> vec.zCoord).max().orElse(1.0))
                .offset(this.xCoord, this.yCoord, this.zCoord)
                .offset(this.getOffsetX(), this.getOffsetY(), this.getOffsetZ());
    }

    public ModelSetMachine getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy()) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelMachine", this.modelName);
            if (!this.myModelSet.isDummy()) {
                this.myModelSet.dataFormatter.initDataMap(this.getResourceState().getDataMap());
            }
            if (this.worldObj != null && !this.worldObj.isRemote) {
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }
        return this.myModelSet;
    }

    @Override
    public String getModelType() {
        return "ModelMachine";
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public void setModelName(String par1) {
        this.modelName = par1;
        this.myModelSet = null;
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public int[] getPos() {
        return new int[]{this.xCoord, this.yCoord, this.zCoord};
    }

    @Override
    public boolean closeGui(String par1, ResourceState par2) {
        return true;
    }

    @Override
    public String getSubType() {
        return this.getMachineType().toString();
    }

    @Override
    public ResourceState getResourceState() {
        return this.state;
    }

    /**
     * NBTにモデル名が含まれない場合に使用
     */
    protected abstract String getDefaultName();
}