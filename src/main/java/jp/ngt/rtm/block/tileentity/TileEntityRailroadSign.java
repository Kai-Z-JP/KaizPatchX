package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.texture.ITextureHolder;
import jp.ngt.rtm.modelpack.texture.RRSProperty;
import jp.ngt.rtm.modelpack.texture.TextureManager;
import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityRailroadSign extends TileEntityPlaceable implements ITextureHolder<RRSProperty> {
    private RRSProperty property;
    private String textureName = "";

    /**
     * メタで保存してた方向データを更新したか
     */
    private boolean yawFixed;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String s = nbt.getString("textureName");
        this.setTexture(RRSProperty.fixName(s));

        //"Yaw"値を持っていればデータ移行済み
        this.yawFixed = nbt.hasKey("Yaw");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("textureName", this.textureName);
    }

    @Override
    public void updateEntity() {
        //互換性
        if (!this.worldObj.isRemote && !this.yawFixed) {
            float f0 = -(float) this.getBlockMetadata() * 90.0F;
            this.setRotation(f0, true);
        }
    }

    @Override
    public void setRotation(float par1, boolean synch) {
        super.setRotation(par1, synch);
        this.yawFixed = true;
    }

    @Override
    public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
        super.setRotation(player, rotationInterval, synch);
        this.yawFixed = true;
    }

    @Override
    public RRSProperty getProperty() {
        if (this.property == null || this.property == RRSProperty.DUMMY) {
            this.property = TextureManager.INSTANCE.getProperty(this.getType(), this.textureName);
            if (this.property == null) {
                this.property = RRSProperty.DUMMY;
            }
        }
        return this.property;
    }

    @Override
    public void setTexture(String name) {
        this.textureName = name;
        this.property = null;
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public TexturePropertyType getType() {
        return TexturePropertyType.RRS;
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
}