package jp.ngt.rtm.entity.train;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.parts.*;
import jp.ngt.rtm.item.ItemCargo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class EntityFreightCar extends EntityTrainBase implements IInventory {
    private static final float[][] CARGO_POS = new float[][]{
            {0.0F, 0.0F, 8.0F},
            {0.0F, 0.0F, 4.0F},
            {0.0F, 0.0F, 0.0F},
            {0.0F, 0.0F, -4.0F},
            {0.0F, 0.0F, -8.0F}};

    private final ItemStack[] cargoSlots = new ItemStack[5];
    public EntityCargo[] cargoEntities = new EntityCargo[5];

    public EntityFreightCar(World world) {
        super(world);
    }

    public EntityFreightCar(World world, String s) {
        super(world, s);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        NBTTagList list = nbt.getTagList("Items", 10);
        IntStream.range(0, list.tagCount()).mapToObj(list::getCompoundTagAt).filter(nbt1 -> nbt1.hasKey("Slot", 1)).forEach(nbt1 -> {
            byte b0 = nbt1.getByte("Slot");
            if (b0 >= 0 && b0 < this.cargoSlots.length) {
                this.cargoSlots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
            }
        });
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        NBTTagList list = new NBTTagList();
        IntStream.range(0, this.cargoSlots.length).filter(i -> this.cargoSlots[i] != null && this.cargoEntities[i] != null).forEach(i -> {
            this.cargoEntities[i].writeCargoToItem();
            NBTTagCompound nbt0 = new NBTTagCompound();
            nbt0.setByte("Slot", (byte) i);
            this.cargoSlots[i].writeToNBT(nbt0);
            list.appendTag(nbt0);
        });
        nbt.setTag("Items", list);
    }

    @Override
    public void setDead() {
        super.setDead();

        Arrays.stream(this.cargoEntities).filter(Objects::nonNull).forEach(Entity::setDead);
    }

    @Override
    public void onVehicleUpdate() {
        super.onVehicleUpdate();

        if (!this.worldObj.isRemote) {
            IntStream.range(0, this.cargoSlots.length).forEach(i -> {
                if (this.hasCargo(i)) {
                    if (this.cargoEntities[i] == null) {
                        EntityCargo entity = this.createCargoEntity((byte) i);
                        entity.updatePartPos(this);
                        this.worldObj.spawnEntityInWorld(entity);
                        this.cargoEntities[i] = entity;
                    }
                } else {
                    if (this.cargoEntities[i] != null) {
                        this.cargoEntities[i].setDead();
                        this.cargoEntities[i] = null;
                    }
                }
            });
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1, float par2) {
        if (!this.worldObj.isRemote) {
            Arrays.stream(this.cargoSlots).filter(Objects::nonNull).forEach(cargoSlot -> this.entityDropItem(cargoSlot, 1.0F));
        }
        return super.attackEntityFrom(par1, par2);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (super.interactFirst(player)) {
            return true;
        } else {
            if (!this.worldObj.isRemote) {
                player.openGui(RTMCore.instance, RTMCore.guiIdFreightCar, player.worldObj, this.getEntityId(), 0, 0);
            }
            return true;
        }
    }

    private boolean hasCargo(int par1) {
        ItemStack itemstack = this.cargoSlots[par1];
        return itemstack != null && itemstack.getItem() instanceof ItemCargo;
    }

    private EntityCargo createCargoEntity(byte slot) {
        EntityCargo cargo;
        int damage = this.cargoSlots[slot].getItemDamage();
        switch (damage) {
            case 1:
                cargo = new EntityArtillery(this.worldObj, this, this.cargoSlots[slot], CARGO_POS[slot], slot);
                break;
            case 2:
                cargo = new EntityTie(this.worldObj, this, this.cargoSlots[slot], CARGO_POS[slot], slot);
                break;
            default:
                cargo = new EntityContainer(this.worldObj, this, this.cargoSlots[slot], CARGO_POS[slot], slot);
                break;
        }

        cargo.readCargoFromItem();

        if (damage == 0 || damage == 1) {
            EntityCargoWithModel entity = (EntityCargoWithModel) cargo;
            if (entity.getModelName().length() == 0) {
                entity.setModelName(entity.getDefaultName());
            }
        }

        return cargo;
    }

    @Override
    public int getSizeInventory() {
        return this.cargoSlots.length;
    }

    @Override
    public ItemStack getStackInSlot(int par1) {
        return this.cargoSlots[par1];
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2) {
        if (this.cargoSlots[par1] != null) {
            ItemStack itemstack;
            if (this.cargoSlots[par1].stackSize <= par2) {
                itemstack = this.cargoSlots[par1];
                this.cargoSlots[par1] = null;
            } else {
                itemstack = this.cargoSlots[par1].splitStack(par2);
                if (this.cargoSlots[par1].stackSize == 0) {
                    this.cargoSlots[par1] = null;
                }
            }
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        if (this.cargoSlots[par1] != null) {
            ItemStack itemstack = this.cargoSlots[par1];
            this.cargoSlots[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack itemStack) {
        this.cargoSlots[par1] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "Inventory_FreightCar";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1) {
        return this.getDistanceSqToEntity(var1) < 64.0D;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int var1, ItemStack var2) {
        return true;
    }
}