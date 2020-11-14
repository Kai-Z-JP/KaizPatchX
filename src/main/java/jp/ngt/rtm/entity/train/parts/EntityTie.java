package jp.ngt.rtm.entity.train.parts;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.entity.EntityInstalledObject;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class EntityTie extends EntityCargo {
	public EntityTie(World world) {
		super(world);
		this.setSize(3.0F, 0.125F);
	}

	public EntityTie(World world, ItemStack itemStack, int x, int y, int z) {
		super(world, itemStack, x, y, z);
	}

	public EntityTie(World world, EntityVehicleBase vehicle, ItemStack itemStack, float[] par4Pos, byte id) {
		super(world, vehicle, itemStack, par4Pos, id);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
	}

	@Override
	protected void readCargoFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("riderUUID_Most", 4) && nbt.hasKey("riderUUID_Least", 4)) {
			long l0 = nbt.getLong("riderUUID_Most");
			long l1 = nbt.getLong("riderUUID_Least");
			if (l0 != 0L && l1 != 0L) {
                UUID uuid = new UUID(l0, l1);
                IntStream.range(0, this.worldObj.loadedEntityList.size()).mapToObj(j -> (Entity) this.worldObj.loadedEntityList.get(j)).filter(entity -> uuid.equals(entity.getUniqueID())).forEach(entity -> entity.mountEntity(this));
            }
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
	}

	@Override
	protected void writeCargoToNBT(NBTTagCompound nbt) {
		if (this.riddenByEntity != null) {
			long l0 = 0L;
			long l1 = 0L;
			UUID uuid = this.riddenByEntity.getUniqueID();
			if (uuid != null) {
				l0 = uuid.getMostSignificantBits();
				l1 = uuid.getLeastSignificantBits();
			}
			nbt.setLong("riderUUID_Most", l0);
			nbt.setLong("riderUUID_Least", l1);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.riddenByEntity != null && !(this.riddenByEntity instanceof EntityLivingBase)) {
			this.riddenByEntity.rotationYaw = this.rotationYaw;
			this.riddenByEntity.rotationPitch = this.rotationPitch;
		}
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (this.riddenByEntity == null) {
			if (this.worldObj.isRemote) {
				return true;
			} else {
				double d0 = 1.5D;
				List list = this.worldObj.selectEntitiesWithinAABB(Entity.class,
                        AxisAlignedBB.getBoundingBox(this.posX - d0, this.posY - 0.5D, this.posZ - d0, this.posX + d0, this.posY + 4.5D, this.posZ + d0),
                        entity -> {
                            if (entity instanceof EntityVehiclePart || entity instanceof EntityBogie || entity instanceof EntityInstalledObject) {
                                return false;
                            } else if (entity instanceof EntityVehicleBase) {
                                return EntityTie.this.getVehicle() != entity;
                            }
                            return true;
                        }
                );

				if (!list.isEmpty()) {
                    for (Object o : list) {
                        Entity entity = (Entity) o;
                        entity.mountEntity(this);
                        NGTLog.sendChatMessage(player, entity.toString() + " was fixed.");
                        return true;
                    }
                }
				NGTLog.sendChatMessage(player, "Fixable entity not found.");
				return false;
			}
		} else {
			this.riddenByEntity.mountEntity(null);
			return true;
		}
	}
}