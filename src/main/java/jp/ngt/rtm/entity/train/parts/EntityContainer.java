package jp.ngt.rtm.entity.train.parts;

import jp.ngt.ngtlib.item.ItemUtil;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.item.ItemCargo;
import jp.ngt.rtm.item.ItemCrowbar;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.ContainerConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetContainer;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class EntityContainer extends EntityCargoWithModel<ModelSetContainer> implements IInventory {
    private final ItemStack[] containerSlots = new ItemStack[54];

    public EntityContainer(World world) {
        super(world);
        this.setSize(3.0F, 2.5F);
    }

    public EntityContainer(World world, ItemStack itemStack, int x, int y, int z) {
        super(world, itemStack, x, y, z);
    }

    public EntityContainer(World world, EntityVehicleBase par2, ItemStack itemStack, float[] par4Pos, byte id) {
        super(world, par2, itemStack, par4Pos, id);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    protected void readCargoFromNBT(NBTTagCompound nbt) {
        if (this.itemCargo != null) {
            NBTTagCompound itemNBT = this.itemCargo.getTagCompound();

            NBTTagList tagList = itemNBT.getTagList("Items", 10);
            IntStream.range(0, tagList.tagCount()).mapToObj(tagList::getCompoundTagAt).forEach(nbt2 -> {
                byte b0 = nbt2.getByte("Slot");
                if (b0 >= 0 && b0 < this.containerSlots.length) {
                    this.containerSlots[b0] = ItemUtil.readFromNBT(nbt2);
                }
            });

            this.setModelName(itemNBT.getString("ModelName"));
        }
    }

    @Override
    protected void writeCargoToNBT(NBTTagCompound nbt) {
        if (this.itemCargo != null) {
            NBTTagList tagList = new NBTTagList();
            IntStream.range(0, this.containerSlots.length).filter(i -> this.containerSlots[i] != null).forEach(i -> {
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setByte("Slot", (byte) i);
                ItemUtil.writeToNBT(nbt1, this.containerSlots[i]);
                tagList.appendTag(nbt1);
            });

            NBTTagCompound itemNBT = this.itemCargo.hasTagCompound() ? this.itemCargo.getTagCompound() : new NBTTagCompound();
            if (tagList.tagCount() != 0) {
                itemNBT.setTag("Items", tagList);
            }
            itemNBT.setString("ModelName", this.getModelName());
            this.itemCargo.setTagCompound(itemNBT);
        }
    }

    private NBTTagCompound getCargoNBT() {
        if (!this.itemCargo.hasTagCompound()) {
            this.itemCargo.setTagCompound(new NBTTagCompound());
        }
        return this.itemCargo.getTagCompound();
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        if (entity instanceof EntityFloor || entity instanceof EntityVehicleBase || entity instanceof EntityBogie) {
            return null;
        }
        return entity.boundingBox;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected void dropCargoItem() {
        this.writeCargoToNBT(this.getCargoNBT());
        this.entityDropItem(this.itemCargo, 1.0F);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (super.interactFirst(player)) {
            return true;
        }

        ItemStack itemstack = player.inventory.getCurrentItem();
        if (this.isIndependent && itemstack != null) {
            if (itemstack.getItem() instanceof ItemCargo && itemstack.getItemDamage() == 0) {
                double d0 = 1.5D;
                double d1 = this.getModelSet().getConfig().containerHeight;
                ItemCargo itemCargo = (ItemCargo) itemstack.getItem();
                double d2 = ((ContainerConfig) ModelPackManager.INSTANCE.getModelSet(itemCargo.getModelType(itemstack), itemCargo.getModelName(itemstack)).getConfig()).containerHeight;
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.posX - d0, this.posY + d1, this.posZ - d0, this.posX + d0, this.posY + d1 + d2, this.posZ + d0);
                EntityCargo topEntity = ((List<EntityContainer>) this.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb, EntityCargo.class::isInstance))
                        .stream()
                        .filter(entity -> entity.isIndependent)
                        .max(Comparator.comparingDouble(e -> e.posY))
                        .orElse(this);

                if (topEntity != this) {
                    return topEntity.interactFirst(player);
                }

                EntityCargoWithModel<ModelSetContainer> cargo = new EntityContainer(this.worldObj, itemstack.splitStack(1), 0, 0, 0);
                cargo.setPositionAndRotation(this.posX, this.posY + d1, this.posZ, this.rotationYaw, 0.0F);

                cargo.readCargoFromItem();
                if (!this.worldObj.isRemote) {
                    this.worldObj.spawnEntityInWorld(cargo);

                    ResourceState itemState = itemCargo.getModelState(itemstack);
                    cargo.getResourceState().readFromNBT(itemState.writeToNBT());
                }
                return true;
            } else if (itemstack.getItem() instanceof ItemCrowbar) {
                this.attackEntityFrom(DamageSource.anvil, 0.0F);
                return true;
            }
        }

        if (!this.worldObj.isRemote) {
            if (this.itemCargo != null) {
                player.openGui(RTMCore.instance, RTMCore.guiIdItemContainer, this.worldObj, this.getEntityId(), 0, 0);
            }
        }
        return true;
    }

    @Override
    public Vec3 getPartVec() {
        //コンストラクタ呼び出し時はstateがnull
        if (this.getResourceState() == null) {
            this.needsUpdatePos = true;
            return jp.ngt.ngtlib.math.Vec3.ZERO;
        }

        ContainerConfig cfg = (ContainerConfig) this.getResourceState().getResourceSet().getConfig();
        CargoPos cp = CargoPos.getCargoPos(cfg.containerLength);
        float zPos = cp.zPos[this.getCargoId()];
        if (zPos == 20.0F) {
            zPos = 0.0F;
        }
        Vec3 vec = super.getPartVec();
        return PooledVec3.create(vec.getX(), vec.getY(), zPos);
    }

    @Override
    public int getSizeInventory() {
        return this.containerSlots.length;
    }

    @Override
    public ItemStack getStackInSlot(int par1) {
        return this.containerSlots[par1];
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2) {
        if (this.containerSlots[par1] != null) {
            ItemStack itemstack;
            if (this.containerSlots[par1].stackSize <= par2) {
                itemstack = this.containerSlots[par1];
                this.containerSlots[par1] = null;
            } else {
                itemstack = this.containerSlots[par1].splitStack(par2);
                if (this.containerSlots[par1].stackSize == 0) {
                    this.containerSlots[par1] = null;
                }
            }
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        if (this.containerSlots[par1] != null) {
            ItemStack itemstack = this.containerSlots[par1];
            this.containerSlots[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack itemStack) {
        this.containerSlots[par1] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "Inventory_Container";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1024;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer par1) {
        return this.getDistanceSqToEntity(par1) < 64.0D;
    }

    @Override
    public void openInventory() {
        if (!this.worldObj.isRemote) {
            this.readCargoFromNBT(this.getCargoNBT());
        }
    }

    @Override
    public void closeInventory() {
        if (!this.worldObj.isRemote) {
            this.writeCargoToNBT(this.getCargoNBT());
        }
    }

    @Override
    public boolean isItemValidForSlot(int var1, ItemStack var2) {
        return true;
    }

    @Override
    public String getModelType() {
        return "ModelContainer";
    }

    @Override
    protected void onSetNewModel(ModelSetContainer modelSet) {
        ContainerConfig cfg = modelSet.getConfig();
        //this.setSize(cfg.containerWidth, cfg.containerHeight);
    }

    @Override
    public String getDefaultName() {
        return "19g_JRF_0";
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(RTMItem.itemCargo, 1, 0);
    }
}