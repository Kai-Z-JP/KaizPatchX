package jp.ngt.ngtlib.block;

import com.google.common.collect.HashBiMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ブロックの集合<br>
 * RenderBlocksによるブロックの描画等に使用
 */
public class NGTObject {
	private static final List<NGTObject> loadedNGTO = new ArrayList<NGTObject>();

	public long objId;
	public HashBiMap<Integer, BlockSet> blockIdMap = HashBiMap.create();
	public List<BlockSet> blockList;
	private final NBTTagList entityList;
	public int xSize, ySize, zSize;
	/**
	 * コピー元の位置
	 */
	public int origX, origY, origZ;
	@Deprecated
	private int lightValue = -1;

	public static NGTObject createNGTO(List<BlockSet> blocks, int w, int h, int d, int x, int y, int z) {
		return createNGTO(blocks, new NBTTagList(), w, h, d, x, y, z);
	}

	public static NGTObject createNGTO(List<BlockSet> blocks, NBTTagList nbt, int w, int h, int d, int x, int y, int z) {
		return createNGTO(NGTUtil.getUniqueId(), blocks, nbt, w, h, d, x, y, z);
	}

	public static NGTObject createNGTO(long id, List<BlockSet> blocks, NBTTagList nbt, int w, int h, int d, int x, int y, int z) {
		NGTObject ngto = new NGTObject(id, blocks, nbt, w, h, d, x, y, z);
		int index = loadedNGTO.indexOf(ngto);
		if (index >= 0) {
			return loadedNGTO.get(index);
		}
		return ngto;
	}

	private NGTObject(long id, List<BlockSet> blocks, NBTTagList nbt, int w, int h, int d, int x, int y, int z) {
		this.objId = id;
		this.blockList = blocks;
		this.entityList = nbt;
		this.xSize = w;
		this.ySize = h;
		this.zSize = d;
		this.origX = x;
		this.origY = y;
		this.origZ = z;

		loadedNGTO.add(this);
	}

	/**
	 * 存在しない場合はBlockAirを返す
	 */
	public BlockSet getBlockSet(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < this.xSize && y < this.ySize && z < this.zSize) {
			int index = (x * this.ySize * this.zSize) + (y * this.zSize) + z;
			if (index < this.blockList.size()) {
				return this.blockList.get(index);
			}
		}
		return BlockSet.AIR;
	}

	/**
	 * 座標が範囲外、もしくはblockとmetadataが同一ならfalse
	 */
	public boolean setBlockSet(int x, int y, int z, Block block, int meta) {
		if (x >= 0 && y >= 0 && z >= 0 && x < this.xSize && y < this.ySize && z < this.zSize) {
			int index = (x * this.ySize * this.zSize) + (y * this.zSize) + z;
			if (index < this.blockList.size()) {
				BlockSet set = this.getBlockSet(x, y, z);
				if (block == set.block && meta == set.metadata) {
					return false;
				}
				this.blockList.set(index, new BlockSet(x, y, z, block, meta));
				return true;
			}
		}
		return false;
	}

	public NBTTagCompound writeToNBT() {
		//NGTLog.startTimer();

		Map<BlockSet, Integer> idMap = new HashMap<BlockSet, Integer>();
		idMap.put(BlockSet.AIR, 0);
		int idCount = 1;

		//BlockSetをIdに変換
		//NGTLog.startTimer();
		NBTTagCompound nbts = new NBTTagCompound();
		int[] blockIds = new int[this.blockList.size()];
		for (int i = 0; i < this.blockList.size(); ++i) {
			BlockSet set = this.blockList.get(i).asKey();
			Integer val = idMap.get(set);
			if (val == null) {
				val = idCount;
				idMap.put(set, val);
				++idCount;
			}
			blockIds[i] = val;

			if (set.hasNBT()) {
				nbts.setTag(String.valueOf(i), set.nbt);
			}
		}
		//NGTLog.stopTimer("write nbt (BlockSet->ID)");

		//変換済みデータ書き込み、一瞬
		//NGTLog.startTimer();
		NBTTagCompound data = new NBTTagCompound();
		if (idCount > 255) {
			data.setIntArray("IData", blockIds);
		} else {
			byte[] bytes = new byte[blockIds.length];
			for (int i = 0; i < bytes.length; ++i) {
				bytes[i] = (byte) (blockIds[i] - 128);
			}
			data.setByteArray("BData", bytes);
		}
		data.setTag("NBTs", nbts);
		//NGTLog.stopTimer("write nbt (ID->NBT)");

		//Idリスト書き込み、数ms
		//NGTLog.startTimer();
		NBTTagList tagList2 = new NBTTagList();
		for (Entry<BlockSet, Integer> set : idMap.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("Set", set.getKey().writeToNBT());
			tag.setInteger("Id", set.getValue());
			tagList2.appendTag(tag);
		}
		data.setTag("IdList", tagList2);
		//NGTLog.stopTimer("write nbt (IdList)");

		//座標・Entity等、一瞬で終わる
		//NGTLog.startTimer();
		data.setInteger("SizeX", this.xSize);
		data.setInteger("SizeY", this.ySize);
		data.setInteger("SizeZ", this.zSize);
		data.setInteger("OrigX", this.origX);
		data.setInteger("OrigY", this.origY);
		data.setInteger("OrigZ", this.origZ);
		data.setLong("ObjId", this.objId);
		data.setTag("Entities", this.entityList);
		//NGTLog.stopTimer("write nbt (OtherData)");

		//NGTLog.stopTimer("write nbt");
		return compress(data);
	}

	public static NGTObject readFromNBT(NBTTagCompound data) {
		if (data.hasKey("ByteData")) {
			data = decompress(data);
		}

		Map<Integer, BlockSet> idMap = new HashMap<Integer, BlockSet>();
		idMap.put(0, BlockSet.AIR);

		//Idリストを読み出す
		NBTTagList tagList2 = data.getTagList("IdList", 10);
		for (int i = 0; i < tagList2.tagCount(); ++i) {
			NBTTagCompound tag = tagList2.getCompoundTagAt(i);
			BlockSet set = BlockSet.readFromNBT(tag.getCompoundTag("Set"));
			int id = tag.getInteger("Id");
			idMap.put(id, set);
		}

		//IdからBlockSetを読み出す
		int[] ids;
		List<BlockSet> list = new ArrayList<BlockSet>();
		if (data.hasKey("IData") || data.hasKey("Blocks")) {
			ids = data.hasKey("IData") ? data.getIntArray("IData") : data.getIntArray("Blocks");//互換性
		} else {
			byte[] bytes = data.getByteArray("BData");
			ids = new int[bytes.length];
			for (int i = 0; i < bytes.length; ++i) {
				ids[i] = (int) bytes[i] + 128;
			}
		}

		if (ids != null) {
			NBTTagCompound nbts = data.getCompoundTag("NBTs");
			for (int i = 0; i < ids.length; ++i) {
				int id = ids[i];
				BlockSet set = idMap.containsKey(id) ? idMap.get(id) : BlockSet.AIR;
				if (nbts.hasKey(String.valueOf(i))) {
					NBTTagCompound tagData = nbts.getCompoundTag(String.valueOf(i));
					list.add(set.setNBT(tagData));
				} else {
					list.add(set);
				}
			}
		}

		int x = data.getInteger("SizeX");
		int y = data.getInteger("SizeY");
		int z = data.getInteger("SizeZ");
		int ox = data.getInteger("OrigX");
		int oy = data.getInteger("OrigY");
		int oz = data.getInteger("OrigZ");
		long objId = data.getLong("ObjId");

		NBTTagList tagList = data.getTagList("Entities", 10);
		if (tagList == null) {
			tagList = new NBTTagList();
		}

		return createNGTO(objId, list, tagList, x, y, z, ox, oy, oz);
	}

	/**
	 * gzipで圧縮
	 */
	private static NBTTagCompound compress(NBTTagCompound data) {
		byte[] compressedData = null;

		try {
			compressedData = CompressedStreamTools.compress(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (compressedData != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setByteArray("ByteData", compressedData);
			return nbt;
		}

		return data;
	}

	/**
	 * gzip圧縮データの読み込み
	 */
	private static NBTTagCompound decompress(NBTTagCompound data) {
		byte[] compressedData = data.getByteArray("ByteData");

		try {
			//return CompressedStreamTools.func_152457_a(compressedData, new NBTSizeTracker(0x400000L));
			return CompressedStreamTools.func_152457_a(compressedData, new NBTSizeTracker(Long.MAX_VALUE));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public void exportToFile(File file) {
		NBTTagCompound data = this.writeToNBT();

		try {
			CompressedStreamTools.write(data, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static NGTObject importFromFile(File file) {
		NBTTagCompound data;
		try {
			data = CompressedStreamTools.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return readFromNBT(data);
	}

	public static NGTObject load(InputStream stream) {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(stream));
		NBTTagCompound data;
		try {
			data = CompressedStreamTools.read(dis);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return readFromNBT(data);
	}

	/**
	 * アイテムに情報を表示
	 */
	@SideOnly(Side.CLIENT)
	public static void addInformation(List list, NBTTagCompound data, float scale) {
		if (data.hasKey("ByteData")) {
			data = decompress(data);
		}
		//list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("usage.editor."));
		int x = data.getInteger("SizeX");
		int y = data.getInteger("SizeY");
		int z = data.getInteger("SizeZ");
		list.add(EnumChatFormatting.GRAY + "Size : " + x + " x " + y + " x " + z);
		list.add(EnumChatFormatting.GRAY + "Scale : " + scale);
	}

	@Deprecated
	public int getLightValue() {
		if (this.lightValue < 0) {
			int brightness = 0;
			for (BlockSet set : this.blockList) {
				int i = set.block.getLightValue();
				if (i > 0) {
					brightness += i;
				} else {
					--brightness;
				}
			}

			this.lightValue = brightness > 15 ? 15 : (brightness < 0 ? 0 : brightness);

			/*int totalLight = 0;
			int count = 0;
			for(BlockSet set : this.blockList)
			{
				totalLight += set.block.getLightValue();
				++count;
			}
			this.lightValue = totalLight / count;*/
		}
		return this.lightValue;
	}

	public NBTTagList getEntityList() {
		return this.entityList;
	}

	@Override
	public int hashCode() {
		return ((this.xSize << 20) & 1024) | ((this.ySize << 10) & 1024) | ((this.zSize) & 1024);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NGTObject) {
			NGTObject ngto = (NGTObject) obj;
			if (ngto.xSize == this.xSize && ngto.ySize == this.ySize && ngto.zSize == this.zSize) {
				for (int i = 0; i < this.blockList.size(); ++i) {
					BlockSet set0 = this.blockList.get(i);
					BlockSet set1 = ngto.blockList.get(i);
					if (!set0.equals(set1)) {
						return false;
					}
				}

				if (this.entityList != null) {
					return this.entityList.equals(ngto.entityList);
				} else return ngto.entityList == null;//Entityなし
			}
		}
		return false;
	}
}