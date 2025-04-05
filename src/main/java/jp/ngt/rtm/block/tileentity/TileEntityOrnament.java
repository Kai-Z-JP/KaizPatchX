package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.block.OrnamentType;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetOrnament;
import jp.ngt.rtm.modelpack.modelset.ModelSetOrnamentClient;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TileEntityOrnament extends TileEntityPlaceable implements IModelSelectorWithType {
    private final ResourceState state = new ResourceState(this);
    private String modelName = "";
    private ModelSetOrnament myModelSet;
    private byte attachedSide;
    private float randomScale;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String s = nbt.getString("ModelName");
        this.setModelName(s);
        this.state.readFromNBT(nbt.getCompoundTag("State"));
        this.attachedSide = nbt.getByte("AttachedSide");
        this.randomScale = nbt.getFloat("RandomScale");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("ModelName", this.modelName);
        nbt.setTag("State", this.state.writeToNBT());
        nbt.setByte("AttachedSide", this.attachedSide);
        nbt.setFloat("RandomScale", this.getRandomScale());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    public byte getAttachedSide() {
        return this.attachedSide;
    }

    public void setAttachedSide(byte side) {
        this.attachedSide = side;
        this.markDirty();
    }

    public float getRandomScale() {
        if (this.randomScale <= 0.0F) {
            float min = this.myModelSet == null ? 1.0F : this.myModelSet.getConfig().minRandomScale;
            float randF = NGTMath.RANDOM.nextFloat();
            this.randomScale = min + (1.0F - min) * randF;
        }
        return this.randomScale;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        ModelSetOrnamentClient modelSet = (ModelSetOrnamentClient) this.getModelSet();
        return pass == 0 || (modelSet.model.light || modelSet.model.alphaBlend) && pass >= 0;
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

    @Override
    public ResourceState getResourceState() {
        return this.state;
    }

    @Override
    public ModelSetOrnament getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy()) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelOrnament", this.modelName);
            if (this.myModelSet != null && !this.myModelSet.isDummy()) {
                this.myModelSet.dataFormatter.initDataMap(this.getResourceState().getDataMap());
            }
            if (this.worldObj != null && !this.worldObj.isRemote && this.myModelSet != null && !this.myModelSet.isDummy()) {
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }
        return this.myModelSet;
    }

    @Override
    public String getModelType() {
        return "ModelOrnament";
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

    public void setModelNameNoSync(String par1) {
        this.modelName = par1;
        this.myModelSet = null;
    }

    @Override
    public String getSubType() {
        return this.getOrnamentType().toString();
    }

    @Override
    public int[] getPos() {
        return new int[]{this.xCoord, this.yCoord, this.zCoord};
    }

    @Override
    public boolean closeGui(String par1, ResourceState par2) {
        return true;
    }

    abstract public OrnamentType getOrnamentType();

    protected abstract String getDefaultName();
}