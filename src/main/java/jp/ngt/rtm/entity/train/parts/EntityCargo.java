package jp.ngt.rtm.entity.train.parts;

import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityCargo extends EntityVehiclePart {
	/**
	 * Server Only
	 */
	protected ItemStack itemCargo;

	public EntityCargo(World par1) {
		super(par1);
		this.ignoreFrustumCheck = true;
		this.preventEntitySpawning = true;
		this.yOffset = 0.0F;
	}

	public EntityCargo(World par1, ItemStack itemStack, int x, int y, int z) {
		this(par1);
		this.itemCargo = itemStack;
		this.isIndependent = true;
	}

	public EntityCargo(World par1, EntityVehicleBase par2, ItemStack itemStack, float[] par4Pos, byte id) {
		super(par1, par2, par4Pos);
		this.itemCargo = itemStack;
		this.setCargoId(id);
		this.isIndependent = false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(24, Byte.valueOf((byte) 0));
	}

	protected byte getCargoId() {
		return this.dataWatcher.getWatchableObjectByte(24);
	}

	protected void setCargoId(byte id) {
		this.dataWatcher.updateObject(24, id);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		NBTTagCompound nbt0 = nbt.getCompoundTag("ContainerItem");
		this.itemCargo = ItemStack.loadItemStackFromNBT(nbt0);
		this.setCargoId(nbt.getByte("cargoId"));
		this.readCargoFromItem();
		super.readEntityFromNBT(nbt);
	}

	/**
	 * @param nbt ItemCargoのNBT
	 */
	protected abstract void readCargoFromNBT(NBTTagCompound nbt);

	public void readCargoFromItem() {
		if (this.itemCargo.hasTagCompound()) {
			this.readCargoFromNBT(this.itemCargo.getTagCompound());
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		this.writeCargoToItem();
		NBTTagCompound nbt0 = new NBTTagCompound();
		this.itemCargo.writeToNBT(nbt0);
		nbt.setTag("ContainerItem", nbt0);
		nbt.setByte("cargoId", this.getCargoId());
		super.writeEntityToNBT(nbt);
	}

	/**
	 * @param nbt ItemCargoのNBT
	 */
	protected abstract void writeCargoToNBT(NBTTagCompound nbt);

	public void writeCargoToItem() {
		if (!this.itemCargo.hasTagCompound()) {
			this.itemCargo.setTagCompound(new NBTTagCompound());
		}
		this.writeCargoToNBT(this.itemCargo.getTagCompound());
	}

	@Override
	public void onLoadVehicle() {
		this.setDead();
	}

	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1, float par2) {
		if (this.isEntityInvulnerable() || this.isDead) {
			return false;
		} else {
			if (!par1.isExplosion() && par1.getEntity() instanceof EntityPlayer) {
				if (!this.worldObj.isRemote) {
					if (this.isIndependent || this.getVehicle() == null) {
						this.setDead();
						this.dropCargoItem();
					}
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * 貨物をドロップ
	 */
	protected void dropCargoItem() {
		this.entityDropItem(this.itemCargo, 1.0F);
	}
}