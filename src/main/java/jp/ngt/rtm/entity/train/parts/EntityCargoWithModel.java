package jp.ngt.rtm.entity.train.parts;

import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class EntityCargoWithModel<T extends ModelSetBase> extends EntityCargo implements IModelSelector {
	private final ResourceState state = new ResourceState(this);
	/**
	 * 直接参照は非推奨
	 */
	private T myModelSet;

	public EntityCargoWithModel(World par1) {
		super(par1);
	}

	public EntityCargoWithModel(World par1, ItemStack itemStack, int x, int y, int z) {
		super(par1, itemStack, x, y, z);
	}

	public EntityCargoWithModel(World par1, EntityVehicleBase par2, ItemStack par3, float[] par4Pos, byte id) {
		super(par1, par2, par3, par4Pos, id);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(27, this.getDefaultName());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setTag("State", this.getResourceState().writeToNBT());
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (player.isSneaking()) {
			if (this.worldObj.isRemote) {
				player.openGui(RTMCore.instance, RTMCore.guiIdSelectEntityModel, player.worldObj, this.getEntityId(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public String getModelName() {
		return this.dataWatcher.getWatchableObjectString(27);
	}

	@Override
	public void setModelName(String par1) {
		this.dataWatcher.updateObject(27, (par1.length() == 0) ? this.getDefaultName() : par1);
		this.writeCargoToItem();

		if (this.getVehicle() != null) {
			this.updatePartPos(this.getVehicle());
		}
	}

	@Override
	public int[] getPos() {
		return new int[]{this.getEntityId(), -1, 0};
	}

	@Override
	public boolean closeGui(String par1, ResourceState par2) {
		return true;
	}

	public T getModelSet() {
		if (this.myModelSet == null || this.myModelSet.isDummy() || !this.myModelSet.getConfig().getName().equals(this.getModelName())) {
			this.myModelSet = ModelPackManager.INSTANCE.getModelSet(this.getModelType(), this.getModelName());
			this.onSetNewModel(this.myModelSet);
			if (this.worldObj == null || !this.worldObj.isRemote) {
				PacketNBT.sendToClient(this);
			}
		}
		return this.myModelSet;
	}

	@Override
	public ResourceState getResourceState() {
		return this.state;
	}

	protected void onSetNewModel(T modelSet) {
	}

	public abstract String getDefaultName();
}