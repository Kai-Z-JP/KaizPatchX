package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.ScriptExecuter;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;

public class TileEntitySignal extends TileEntityPlaceable implements IProvideElectricity, IModelSelector {
    private final ResourceState state = new ResourceState(this);
    public int blockDirection;
    private String modelName = "";
    private Block renderBlock;
    private final ScriptExecuter executer = new ScriptExecuter();

    private ModelSetSignal myModelSet;
    private int signalLevel = 0;
    public int tick;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.blockDirection = nbt.getInteger("blockDir");
        this.signalLevel = nbt.getInteger("Signal");
        String s = nbt.getString("modelName");
        this.setModelName(s);
        this.renderBlock = Block.getBlockFromName(nbt.getString("blockName"));
        this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));

        if (this.worldObj != null && this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);//描画の更新
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("blockDir", this.blockDirection);
        nbt.setInteger("Signal", this.signalLevel);
        nbt.setString("modelName", this.modelName);
        String s = Block.blockRegistry.getNameForObject(this.renderBlock);
        nbt.setString("blockName", s == null ? "" : s);
        nbt.setTag("State", this.getResourceState().writeToNBT());
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        ++this.tick;
        if (this.tick == Integer.MAX_VALUE) {
            this.tick = 0;
        }

        if (!this.getWorldObj().isRemote) {
            this.executer.execScript(this);
        }
    }

    @Override
    public int getElectricity() {
        return 0;
    }

    @Override
    public void setElectricity(int x, int y, int z, int level) {
        if (!this.worldObj.isRemote) {
            ModelSetSignal modelSet = this.getModelSet();
            if (level > modelSet.maxSignalLevel) {
                level = modelSet.maxSignalLevel;
            }
            this.signalLevel = level;
            this.markDirty();
            this.sendPacket();
        }
    }

    /**
     * @param par1 元のブロック
     * @param par2 信号機の設置されてる面
     */
    public void setSignalProperty(String name, Block par1, int par2, EntityPlayer player) {
        this.renderBlock = par1;
        this.blockDirection = par2;
        this.modelName = name;
        this.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, false);
        this.sendPacket();
        this.markDirty();
    }

    @SideOnly(Side.CLIENT)
    public float getBlockDirection() {
        return (float) this.blockDirection * 90.0F;
    }

    @SideOnly(Side.CLIENT)
    public int getSignal() {
        return this.signalLevel;
    }

    @SideOnly(Side.CLIENT)
    public void setSignal(int par1) {
        this.signalLevel = par1;
    }

    public Block getRenderBlock()//Block破壊時にも呼び出し(Server)
    {
        if (this.renderBlock == null) {
            return RTMBlock.linePole;
        }
        return this.renderBlock;
    }

    public ModelSetSignal getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy()) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelSignal", this.modelName);
            if (this.worldObj == null || !this.worldObj.isRemote) {
                PacketNBT.sendToClient(this);
            }
        }
        return this.myModelSet;
    }

    private void sendPacket() {
        NGTUtil.sendPacketToClient(this);
    }

    @Override
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass >= 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
                this.xCoord + this.getOffsetX(),
                this.yCoord + this.getOffsetY(),
                this.zCoord + this.getOffsetZ(),
                this.xCoord + 1 + this.getOffsetX(),
                this.yCoord + 2 + this.getOffsetY(),
                this.zCoord + 1 + this.getOffsetZ());
    }
    @Override
    public String getModelType() {
        return "ModelSignal";
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public void setModelName(String par1) {
        this.modelName = par1;
        this.myModelSet = null;
        if (this.worldObj == null || !this.worldObj.isRemote) {
            this.markDirty();
            this.sendPacket();
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
    public ResourceState getResourceState() {
        return this.state;
    }
}