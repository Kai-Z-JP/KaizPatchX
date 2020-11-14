package jp.ngt.mcte.editor;

import jp.ngt.mcte.editor.filter.Repeatable;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.math.AABBInt;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

/**
 * World上のBlockとEntityを保存
 */
public class WorldSnapshot {
	public static final String IGNORE_AIR = "IgnoreAir";
	public static final String IGNORE_WATER = "IgnoreWater";

	private final List<BlockSet> blockList = new ArrayList<>();
	//Entityを回転に対応させてない
	private final List<Entity> entityList = new ArrayList<>();
	private final AABBInt origBox;
	/**
	 * インポートデータはfalse
	 */
	private final boolean hasOrigPos;

	public WorldSnapshot(NGTObject ngto) {
		this.blockList.addAll(ngto.blockList);
		this.origBox = new AABBInt(ngto.origX, ngto.origY, ngto.origZ,
				ngto.origX + ngto.xSize, ngto.origY + ngto.ySize, ngto.origZ + ngto.zSize);
		this.hasOrigPos = false;
	}

	public WorldSnapshot(List<BlockSet> list, AABBInt box) {
		this.blockList.addAll(list);
		this.origBox = box;
		this.hasOrigPos = false;
	}

	public WorldSnapshot(Editor editor, AABBInt box, String options) {
		this.save(editor, box, options);
		this.origBox = box;
		this.hasOrigPos = true;
	}

	private void save(final Editor editor, AABBInt box, final String options) {
		this.repeat(box, (box1, index, x, y, z) -> {
			BlockSet blockSet = editor.getBlockSet(x, y, z);
			if (options.contains(IGNORE_WATER)) {
				if (blockSet.block.getMaterial().isLiquid()) {
					blockSet = BlockSet.AIR;
				}
			}
			WorldSnapshot.this.blockList.add(blockSet);
		});

		List list = editor.getWorld().getEntitiesWithinAABBExcludingEntity(
				editor.getEntity(), AxisAlignedBB.getBoundingBox(
						box.minX, box.minY, box.minZ,
						box.maxX, box.maxY, box.maxZ));
		this.entityList.addAll(list);
	}

	/**
	 * ブロックの配置を復元
	 */
	public void restore(Editor editor) {
		if (this.hasOrigPos) {
			this.blockList.forEach(blockSet -> editor.setBlock(blockSet.x, blockSet.y, blockSet.z, blockSet));
		}
	}

	public void setBlocks(final Editor editor, int x, int y, int z, final String options) {
		Repeatable repeater = (box, index, x1, y1, z1) -> {
			BlockSet blockSet = WorldSnapshot.this.blockList.get(index);
			if (options.contains(IGNORE_AIR)) {
				if (blockSet.block == Blocks.air) {
					return;
				}
			}
			editor.setBlock(x1, y1, z1, blockSet);
		};
		AABBInt box = new AABBInt(x, y, z,
				x + this.origBox.sizeX(), y + this.origBox.sizeY(), z + this.origBox.sizeZ());
		this.repeat(box, repeater);
		this.repeat(box, repeater);//松明やレールが壊れないように
	}

	public void repeat(AABBInt box, Repeatable repeater) {
		int index = 0;
		for (int x = box.minX; x < box.maxX; ++x) {
			for (int y = box.minY; y < box.maxY; ++y) {
				for (int z = box.minZ; z < box.maxZ; ++z) {
					repeater.processing(box, index, x, y, z);
					++index;
				}
			}
		}
	}

	public NGTObject convertNGTO() {
		NBTTagList tagList = NGTWorld.writeEntitiesToNBT(this.entityList);
		return NGTObject.createNGTO(this.blockList, tagList,
				this.origBox.sizeX(), this.origBox.sizeY(), this.origBox.sizeZ(),
				this.origBox.minX, this.origBox.minY, this.origBox.minZ);
	}

	public int getSize() {
		return this.blockList.size();
	}

	public List<BlockSet> getBlocks() {
		return this.blockList;
	}

	public List<Entity> getEntities() {
		return this.entityList;
	}
}