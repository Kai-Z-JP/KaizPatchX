package jp.ngt.ngtlib.protection;

import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.network.PacketProtection;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;

public class ProtectionManager {
	public static final ProtectionManager INSTANCE = new ProtectionManager();

	public static final String DATA_NAME = "ProtectedObjects";
	public static final String KEY_ID = "ID";
	public static final String KEY_UNBREAKABLE = "Unbreakable";
	public static final String KEY_UNEDITABLE = "Uneditable";

	private ProtectionData lockObjs;

	private ProtectionManager() {
	}

	public ProtectionData getLockObj() {
		if (this.lockObjs == null) {
			this.lockObjs = new ProtectionData(DATA_NAME);
		}
		return this.lockObjs;
	}

	public boolean leftClickBlock(EntityPlayer player, int x, int y, int z) {
		Object target = this.getTarget(player.worldObj, x, y, z);
		return this.getLockState(player, target, KEY_UNBREAKABLE);
	}

	public boolean rightClickBlock(EntityPlayer player, int x, int y, int z) {
		Object target = this.getTarget(player.worldObj, x, y, z);
		return this.rightClickObject(player, target);
	}

	public boolean leftClickEntity(EntityPlayer player, Entity entity) {
		Object target = this.getTarget(entity);
		return this.getLockState(player, target, KEY_UNBREAKABLE);
	}

	public boolean rightClickEntity(EntityPlayer player, Entity entity) {
		Object target = this.getTarget(entity);
		return this.rightClickObject(player, target);
	}

	public boolean rightClickObject(EntityPlayer player, Object target) {
		NBTTagCompound nbt = this.getKeyNBT(player);
		if (nbt != null) {
			return this.useKey(player, nbt, target);
		} else {
			return this.getLockState(player, target, KEY_UNEDITABLE);
		}
	}

	private boolean useKey(EntityPlayer player, NBTTagCompound keyNBT, Object target) {
		if (player.worldObj.isRemote) {
			return true;
		}

		String name = this.getObjectName(target);
		String id2 = keyNBT.getString(KEY_ID);
		if (this.getLockObj().hasObject(name)) {
			NBTTagCompound nbt = this.getLockObj().getObject(name);
			String id1 = nbt.getString(KEY_ID);
			if (id1.equals(id2)) {
				boolean flag = !(target instanceof Lockable) || ((Lockable) target).unlock(player, id2);
				if (flag) {
					this.unlockObject(player, name);
					return true;
				}
			} else {
				NGTLog.sendChatMessage(player, "Invalid key.");
			}
		} else {
			boolean flag = !(target instanceof Lockable) || ((Lockable) target).lock(player, id2);
			if (flag) {
				this.lockObject(player, name, id2);
				return true;
			} else {
				NGTLog.sendChatMessage(player, "Unable to lock this object.");
			}
		}
		return false;
	}

	private boolean getLockState(EntityPlayer player, Object target, String state) {
		String name = this.getObjectName(target);
		if (this.getLockObj().hasObject(name)) {
			NBTTagCompound nbt = this.getLockObj().getObject(name);
			if (nbt.getBoolean(state)) {
				NGTLog.sendChatMessage(player, "This object is locked. -> %s", name);
				return true;
			}
		}
		return false;
	}

	private Object getTarget(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		Object target = (tileEntity != null) ? tileEntity : block;
		if (target instanceof Lockable) {
			target = ((Lockable) target).getTarget(world, x, y, z);
		}

		if (target instanceof Block) {
			target = new int[]{x, y, z};
		}

		return target;
	}

	private Object getTarget(Entity entity) {
		Object target = entity;
		if (entity instanceof Lockable) {
			target = ((Lockable) entity).getTarget(entity.worldObj, 0, -1, 0);
		}
		return target;
	}

	private String getObjectName(Object object) {
		if (object instanceof int[]) {
			int[] pos = (int[]) object;
			return String.format("Block:%d,%d,%d", pos[0], pos[1], pos[2]);
		} else if (object instanceof TileEntity) {
			TileEntity tileEntity = (TileEntity) object;
			return String.format("TileEntity:%d,%d,%d", tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
		} else if (object instanceof Entity) {
			Entity entity = (Entity) object;
			UUID uuid = entity.getUniqueID();
			return String.format("Entity:%s", uuid.toString());
		}

		return null;
	}

	private NBTTagCompound getKeyNBT(EntityPlayer player) {
		ItemStack stack = player.inventory.getCurrentItem();
		if (stack != null && stack.getItem() == NGTCore.protection_key) {
			if (stack.hasTagCompound()) {
				return stack.getTagCompound();
			}
		}
		return null;
	}

	private void lockObject(EntityPlayer player, String name, String id) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(KEY_ID, id);
		nbt.setBoolean(KEY_UNBREAKABLE, true);
		nbt.setBoolean(KEY_UNEDITABLE, true);
		this.getLockObj().setObject(name, nbt);
		NGTCore.NETWORK_WRAPPER.sendToAll(new PacketProtection(name, nbt));
		NGTLog.sendChatMessage(player, "Object locked. -> %s", name);
	}

	private void unlockObject(EntityPlayer player, String name) {
		this.getLockObj().removeObject(name);
		NGTCore.NETWORK_WRAPPER.sendToAll(new PacketProtection(name, new NBTTagCompound()));
		NGTLog.sendChatMessage(player, "Object unlocked. -> %s", name);
	}

	public void loadData(World world) {
		//ディメンションの数だ呼ばれる
		if (world instanceof WorldServer && world.provider.dimensionId == 0) {
			this.lockObjs = (ProtectionData) world.mapStorage.loadData(ProtectionData.class, DATA_NAME);

			if (this.lockObjs == null) {
				this.lockObjs = new ProtectionData(DATA_NAME);
				world.mapStorage.setData(DATA_NAME, this.lockObjs);
			}
		}
	}

	public void sendDataToClient() {
		Map<String, NBTTagCompound> map = this.getLockObj().getDatas();

		map.forEach((key, value) -> NGTCore.NETWORK_WRAPPER.sendToAll(new PacketProtection(key, value)));
	}

	public void receivePacket(String name, NBTTagCompound data) {
		if (data.hasKey(KEY_ID)) {
			this.getLockObj().setObject(name, data);
		} else {
			this.getLockObj().removeObject(name);
		}
	}
}