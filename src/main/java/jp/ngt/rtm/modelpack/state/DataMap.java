package jp.ngt.rtm.modelpack.state;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class DataMap {
	public static final byte SYNC_FLAG = 1;
	public static final byte SAVE_FLAG = 2;

	//[英字1字以上]
	private static final Pattern VAL_TYPE = Pattern.compile("\\([a-zA-Z]+\\)");

	private final Map<String, DataEntry> map = new HashMap<>();
	private Object entity;
	private DataFormatter dataFormatter = new DataFormatter();//ダミー

	public void setEntity(Object par1) {
		this.entity = par1;
	}

	public void setFormatter(DataFormatter formatter) {
		this.dataFormatter = formatter;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("DataList", 10);
		IntStream.range(0, list.tagCount()).mapToObj(list::getCompoundTagAt).forEach(entry -> {
			String type = entry.getString("Type");
			String name = entry.getString("Name");
			int flag = entry.getInteger("Flag");
			DataEntry de = DataEntry.getEntry(type, "", flag);
			de.readFromNBT(entry);
			this.set(name, de, flag);
		});
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();

		NBTTagList list = new NBTTagList();
		this.map.entrySet().stream().filter(entry -> (entry.getValue().flag & SAVE_FLAG) != 0).forEach(entry -> {
			NBTTagCompound nbt2 = new NBTTagCompound();
			nbt2.setString("Name", entry.getKey());
			nbt2.setInteger("Flag", entry.getValue().flag);
			entry.getValue().writeToNBT(nbt2);
			list.appendTag(nbt2);
		});
		nbt.setTag("DataList", list);
		return nbt;
	}

	private void sendPacket(String key, DataEntry value, boolean toClient) {
		PacketNotice packet = null;

		if (this.entity instanceof Entity) {
			Entity entity = (Entity) this.entity;
			String msg = String.format("DM,%s,%d,%s,%s,%s,%d",
					"E",
					entity.getEntityId(),
					key,
					value.getType().key,
					value.toString(),
					value.flag);

			packet = toClient ? new PacketNotice(PacketNotice.Side_CLIENT, msg, entity) : new PacketNotice(PacketNotice.Side_SERVER, msg, entity);
		} else if (this.entity instanceof TileEntity) {
			TileEntity te = (TileEntity) this.entity;
			String pos = String.format("%d %d %d", te.xCoord, te.yCoord, te.zCoord);
			String msg = String.format("DM,%s,%s,%s,%s,%s,%d",
					"T",
					pos,
					key,
					value.getType().key,
					value.toString(),
					value.flag);

			packet = toClient ? new PacketNotice(PacketNotice.Side_CLIENT, msg, te) : new PacketNotice(PacketNotice.Side_SERVER, msg, te);
		}

		if (packet != null)//Item状態での値set時にnull
		{
			if (toClient) {
				RTMCore.NETWORK_WRAPPER.sendToAll(packet);
			} else {
				RTMCore.NETWORK_WRAPPER.sendToServer(packet);
			}
		}
	}

	public static void receivePacket(String msg, PacketNotice packet, World world, boolean onClient) {
		String[] sa = msg.split(",");
		String target = sa[1];
		String targetId = sa[2];
		String key = sa[3];
		String type = sa[4];
		String data = sa[5];
		int flag = Integer.parseInt(sa[6]);
		int fSync = flag & SYNC_FLAG;
		int fSave = flag & SAVE_FLAG;
		if (onClient) {
			fSync = 0;
		}
		flag = fSync | fSave;

		DataEntry de = DataEntry.getEntry(type, data, flag);

		if (target.equals("E")) {
			int id = Integer.parseInt(targetId);
			Entity entity = world.getEntityByID(id);
			if (entity instanceof IModelSelector) {
				((IModelSelector) entity).getResourceState().getDataMap().set(key, de, flag);
			}
		} else if (target.equals("T")) {
			String[] sa2 = targetId.split(" ");
			TileEntity entity = world.getTileEntity(Integer.parseInt(sa2[0]), Integer.parseInt(sa2[1]), Integer.parseInt(sa2[2]));
			if (entity instanceof IModelSelector) {
				((IModelSelector) entity).getResourceState().getDataMap().set(key, de, flag);
			}
		}
	}

	public boolean contains(String key) {
		return this.map.containsKey(key);
	}

	private <T extends DataEntry> T get(String key) {
		return (T) this.map.get(key);
	}

	private void set(String key, DataEntry value, int flag) {
		if (!this.dataFormatter.check(key, value)) {
			NGTLog.debug("Invalid data : %s=%s", key, value.toString());
			return;
		}

		boolean sync = ((flag & SYNC_FLAG) != 0);
		boolean onServerSide = NGTUtil.isServer();
		if (onServerSide || !sync || (this.entity == null) || this.entity instanceof Item) {
			//Client側かつ同期する場合は、一旦Server経由でセットするので、ここでは処理しない
			//ただしアイテム状態(entity==null)はここでset
			this.map.put(key, value);
		}

		if (sync) {
			this.sendPacket(key, value, onServerSide);
		}
	}

	/**
	 * コマンドから使用想定
	 *
	 * @param key
	 * @param value "(type)hoge" で型強制
	 * @param flag
	 */
	public boolean set(String key, String value, int flag) {
		Matcher matcher = VAL_TYPE.matcher(value);
		DataEntry entry;
		if (matcher.find()) {
			String type = matcher.group().replace("(", "").replace(")", "");
			String val2 = matcher.replaceAll("");
			entry = DataEntry.getEntry(type, val2, flag);
		} else {
			entry = this.get(key);//型取得のため
			entry = DataEntry.getEntry(entry.getType().key, value, flag);
		}

		if (entry != null) {
			this.set(key, entry, flag);
			return true;
		} else {
			NGTLog.debug("[DataMap] Invalid Data (Key:%s, Value:%s)", key, value);
			return false;
		}
	}

	public Map<String, DataEntry> getEntries() {
		return this.map;
	}

	public String getArg() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (Entry<String, DataEntry> entry : this.map.entrySet()) {
			++count;
			sb.append(entry.getKey());
			sb.append("=(");
			sb.append(entry.getValue().getType().key);
			sb.append(")");
			sb.append(entry.getValue().data.toString());

			if (count < this.map.size()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public void setArg(String par1, boolean overwrite) {
		String[][] array = convertArg(par1);
		Arrays.stream(array).filter(sa -> !this.map.containsKey(sa[0]) || overwrite).forEach(sa -> this.set(sa[0], String.format("(%s)%s", sa[1], sa[2]), DataMap.SYNC_FLAG | DataMap.SAVE_FLAG));
	}

	/**
	 * [key, type, value]
	 */
	public static String[][] convertArg(String par1) {
		String[] sa = par1.split(",");
		String[][] array = new String[sa.length][];
		for (int i = 0; i < array.length; ++i) {
			String s = sa[i];
			int idxEq = s.indexOf('=');
			int idxBr = s.indexOf(')');
			if (idxEq >= 0 && idxBr >= 0) {
				String key = s.substring(0, idxEq);
				String type = s.substring(idxEq + 2, idxBr);
				String value = s.substring(idxBr + 1);
				array[i] = new String[]{key, type, value};
			} else {
				NGTLog.debug("Invalid data : %s", s);
				return new String[0][0];
			}
		}
		return array;
	}

	//以下Script呼び出し用
	//////////////////////////////////////////////////////////////////////

	public int getInt(String key) {
		try {
			DataEntryInt de = this.get(key);
			return (de != null) ? de.get() : 0;
		} catch (Exception e) {
			NGTLog.debug("%s is not Integer", key);
			return 0;
		}
	}

	public void setInt(String key, int value, int flag) {
		this.set(key, new DataEntryInt(value, flag), flag);
	}

	//////////////////////////////////////////////////////////////////////

	public double getDouble(String key) {
		try {
			DataEntryDouble de = this.get(key);
			//NGTLog.debug("get:" + value);
			return (de != null) ? de.get() : 0.0D;
		} catch (Exception e) {
			NGTLog.debug("%s is not Double", key);
			return 0.0D;
		}
	}

	public void setDouble(String key, double value, int flag) {
		this.set(key, new DataEntryDouble(value, flag), flag);//NGTLog.debug("set:" + value);
	}

	//////////////////////////////////////////////////////////////////////

	public boolean getBoolean(String key) {
		try {
			DataEntryBoolean de = this.get(key);
			return (de != null) ? de.get() : false;
		} catch (Exception e) {
			NGTLog.debug("%s is not Boolean", key);
			return false;
		}
	}

	public void setBoolean(String key, boolean value, int flag) {
		this.set(key, new DataEntryBoolean(value, flag), flag);
	}

	//////////////////////////////////////////////////////////////////////

	public String getString(String key) {
		try {
			DataEntryString de = this.get(key);
			return (de != null) ? de.get() : "";
		} catch (Exception e) {
			NGTLog.debug("%s is not String", key);
			return "";
		}
	}

	public void setString(String key, String value, int flag) {
		this.set(key, new DataEntryString(value, flag), flag);
	}

	//////////////////////////////////////////////////////////////////////

	public Vec3 getVec(String key) {
		try {
			DataEntryVec de = this.get(key);
			return (de != null) ? de.get() : Vec3.ZERO;
		} catch (Exception e) {
			NGTLog.debug("%s is not Vec", key);
			return Vec3.ZERO;
		}
	}

	public void setVec(String key, Vec3 value, int flag) {
		this.set(key, new DataEntryVec(value, flag), flag);
	}

	//////////////////////////////////////////////////////////////////////

	public int getHex(String key) {
		try {
			DataEntryHex de = this.get(key);
			return (de != null) ? de.get() : 0;
		} catch (Exception e) {
			NGTLog.debug("%s is not Hex", key);
			return 0;
		}
	}

	public void setHex(String key, int value, int flag) {
		this.set(key, new DataEntryHex(value, flag), flag);
	}
}