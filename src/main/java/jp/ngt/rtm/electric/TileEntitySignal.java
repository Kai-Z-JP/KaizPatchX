package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityPole;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.ScriptExecuter;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignalClient;
import jp.ngt.rtm.modelpack.state.ResourceState;
import jp.ngt.rtm.render.ModelObject;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntitySignal extends TileEntityPlaceable implements IProvideElectricity, IModelSelector {
    private final ResourceState state = new ResourceState(this);
    public int blockDirection;
    private String modelName = "";
    private Block renderBlock;
    private final ScriptExecuter executer = new ScriptExecuter();

    private TileEntity origTileEntity;
    private ModelSetSignal myModelSet;
    private int signalLevel = 0;
    public int tick;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (!nbt.hasKey("blockDir")) {
            return;
        }
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

        if (nbt.hasKey("BaseBlockData")) {
            this.origTileEntity = TileEntity.createAndLoadEntity(nbt.getCompoundTag("BaseBlockData"));
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

        if (this.origTileEntity != null) {
            NBTTagCompound nbt2 = new NBTTagCompound();
            this.origTileEntity.writeToNBT(nbt2);
            nbt.setTag("BaseBlockData", nbt2);
        }
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

            if (this.renderBlock == RTMBlock.linePole && this.origTileEntity == null) {
                TileEntityPole tileEntity = new TileEntityPole();
                String modelName = TileEntityPole.getFixedModelName(this.getBlockMetadata());
                tileEntity.setModelNameNoSync(modelName);
                this.origTileEntity = tileEntity;
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
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
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    /**
     * @param par1 元のブロック
     * @param par2 信号機の設置されてる面
     */
    public void setSignalProperty(String name, Block par1, int par2, EntityPlayer player, TileEntity tileEntity) {
        this.renderBlock = par1;
        this.origTileEntity = tileEntity;
        this.blockDirection = par2;
        this.modelName = name;
        this.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, false);
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public TileEntity getOrigTileEntity() {
        return this.origTileEntity;
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

    /**
     * Block破壊時呼び出し
     */
    public void setOrigBlock() {
        Block block = this.getRenderBlock();
        int meta = this.getBlockMetadata();
        this.getWorldObj().setBlock(this.xCoord, this.yCoord, this.zCoord, block, meta, 3);

        TileEntity tile = this.getWorldObj().getTileEntity(this.xCoord, this.yCoord, this.zCoord);
        if (this.origTileEntity != null && tile != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            this.origTileEntity.writeToNBT(nbt);
            tile.readFromNBT(nbt);
        }
    }

    public ModelSetSignal getModelSet() {
        if (this.myModelSet == null || this.myModelSet.isDummy()) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelSignal", this.modelName);
            if (!this.myModelSet.isDummy()) {
                this.myModelSet.dataFormatter.initDataMap(this.getResourceState().getDataMap());
            }
            if (this.worldObj != null && !this.worldObj.isRemote) {
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }
        return this.myModelSet;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        ModelSetSignalClient modelSet = (ModelSetSignalClient) this.getModelSet();
        ModelObject modelObj = modelSet.model;
        return pass == 0 || (modelObj.light || modelObj.alphaBlend) && pass >= 1;
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
    public ResourceState getResourceState() {
        return this.state;
    }
}