package jp.ngt.rtm.electric;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.EntityInstalledObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityElectricalWiring extends EntityInstalledObject {
    public final TileEntityDummyEW tileEW;

    public EntityElectricalWiring(World world) {
        super(world);
        this.tileEW = new TileEntityDummyEW(this);
        this.tileEW.setWorldObj(world);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.tileEW.readFromNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        this.tileEW.writeToNBT(nbt);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.tileEW.yCoord <= 0) {
            this.setTilePos();
        }

        this.tileEW.updateEntity();
    }

    private void setTilePos() {
        this.tileEW.xCoord = MathHelper.floor_double(this.posX);
        this.tileEW.yCoord = MathHelper.floor_double(this.posY);
        this.tileEW.zCoord = MathHelper.floor_double(this.posZ);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (this.worldObj.isRemote) {
            if (player.isSneaking() && player.getCurrentEquippedItem() == null) {
                player.openGui(RTMCore.instance, RTMCore.guiIdSelectEntityModel, this.worldObj, this.getEntityId(), 0, 0);
            }
        } else {
            if (!player.isSneaking()) {
                return this.tileEW.onRightClick(player);
            }
        }
        return true;
    }

    public abstract int getElectricity();

    public abstract void setElectricity(int par1);
}