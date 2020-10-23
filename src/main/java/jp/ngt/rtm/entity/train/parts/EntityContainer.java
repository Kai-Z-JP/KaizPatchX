package jp.ngt.rtm.entity.train.parts;

import jp.ngt.ngtlib.item.ItemUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.item.ItemCargo;
import jp.ngt.rtm.item.ItemCrowbar;
import jp.ngt.rtm.modelpack.cfg.ContainerConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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
			for (int i = 0; i < tagList.tagCount(); ++i) {
				NBTTagCompound nbt2 = tagList.getCompoundTagAt(i);
				byte b0 = nbt2.getByte("Slot");

				if (b0 >= 0 && b0 < this.containerSlots.length) {
					this.containerSlots[b0] = ItemUtil.readFromNBT(nbt2);
				}
			}

			this.setModelName(itemNBT.getString("ModelName"));
		}
	}

	@Override
	protected void writeCargoToNBT(NBTTagCompound nbt) {
		if (this.itemCargo != null) {
			NBTTagList tagList = new NBTTagList();
			for (int i = 0; i < this.containerSlots.length; ++i) {
				if (this.containerSlots[i] != null) {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setByte("Slot", (byte) i);
					ItemUtil.writeToNBT(nbt1, this.containerSlots[i]);
					tagList.appendTag(nbt1);
				}
			}

			NBTTagCompound itemNBT = this.itemCargo.hasTagCompound() ? this.itemCargo.getTagCompound() : new NBTTagCompound();
			itemNBT.setTag("Items", tagList);
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
				ModelSetContainer set = this.getModelSet();
				EntityCargo cargo = new EntityContainer(this.worldObj, itemstack.copy(), 0, 0, 0);
				cargo.setPositionAndRotation(this.posX, this.posY + set.getConfig().containerHeight, this.posZ, this.rotationYaw, 0.0F);
				if (!this.worldObj.isRemote) {
					this.worldObj.spawnEntityInWorld(cargo);
				}
				--itemstack.stackSize;
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
		ContainerConfig cfg = this.getModelSet().getConfig();
		CargoPos cp = CargoPos.getCargoPos(cfg.containerLength);
		float zPos = cp.zPos[this.getCargoId()];
		if (zPos == 20.0F) {
			zPos = 0.0F;
		}
		float x = this.dataWatcher.getWatchableObjectFloat(21);
		float y = this.dataWatcher.getWatchableObjectFloat(22);
		float z = zPos;
		return Vec3.createVectorHelper(x, y, z);
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
				return itemstack;
			} else {
				itemstack = this.containerSlots[par1].splitStack(par2);
				if (this.containerSlots[par1].stackSize == 0) {
					this.containerSlots[par1] = null;
				}
				return itemstack;
			}
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
}