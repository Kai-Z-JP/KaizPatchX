package jp.ngt.mcte.editor;

import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.block.BlockMinesweeper.MinesweeperType;
import jp.ngt.mcte.block.TileEntityMinesweeper;
import jp.ngt.mcte.editor.filter.Repeatable;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.block.TileEntityCustom;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.AABBInt;
import jp.ngt.ngtlib.util.Stack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Editor {
	//public static final byte EditType_Delete = 0;
	//public static final byte EditType_Cut = 1;
	//public static final byte EditType_Copy = 2;
	//public static final byte EditType_Paste = 3;
	//public static final byte EditType_Fill_1 = 4;
	//public static final byte EditType_Fill_2 = 5;
	public static final byte EditType_Replace = 6;
	public static final byte EditType_Clone = 7;
	//public static final byte EditType_DelEntity = 8;
	public static final byte EditType_Minesweeper = 9;
	public static final byte EditType_Miniature = 10;

	public static final byte EditMode_0 = 0;
	public static final byte EditMode_1 = 1;
	public static final byte EditMode_VisibleBox_0 = 2;
	public static final byte EditMode_VisibleBox_1 = 3;
	public static final byte EditMode_Max = 3;

	public static final byte Transform_RotateX = 0;
	public static final byte Transform_RotateY = 1;
	public static final byte Transform_RotateZ = 2;
	public static final byte Transform_MirrorX = 3;
	public static final byte Transform_MirrorY = 4;
	public static final byte Transform_MirrorZ = 5;

	private final EntityEditor editorEntity;

	private WorldSnapshot clipboard;
	private Stack<WorldSnapshot> history = new Stack<WorldSnapshot>(MCTE.numberOfUndo);

	public Editor(EntityEditor par1) {
		this.editorEntity = par1;
	}

	public static EntityEditor getNewEditor(World world, EntityPlayer player, int x, int y, int z) {
		if (world.isRemote) {
			return null;
		}
		EntityEditor entity = new EntityEditor(world, player, x, y, z);
		Editor editor = new Editor(entity);
		EditorManager.INSTANCE.add(player.getCommandSenderName(), editor);
		return entity;
	}

	public EntityEditor getEntity() {
		return this.editorEntity;
	}

	public World getWorld() {
		return this.getEntity().worldObj;
	}

	public AABBInt getSelectBox() {
		int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;

		int[] start = this.getEntity().getPos(true);
		int[] end = this.getEntity().getPos(false);
		if ((start[0] == 0 && start[1] == 0 && start[2] == 0) || (end[0] == 0 && end[1] == 0 && end[2] == 0)) {
			return null;//0バグ回避
		}

		minX = (start[0] < end[0]) ? start[0] : end[0];
		maxX = (start[0] < end[0]) ? end[0] : start[0];
		minY = (start[1] < end[1]) ? start[1] : end[1];
		maxY = (start[1] < end[1]) ? end[1] : start[1];
		minZ = (start[2] < end[2]) ? start[2] : end[2];
		maxZ = (start[2] < end[2]) ? end[2] : start[2];
		maxX += 1;
		maxY += 1;
		maxZ += 1;
		return new AABBInt(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public AABBInt getPasteBox() {
		int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;

		byte b = this.getEntity().getEditMode();
		if (!(b == EditMode_VisibleBox_0 || b == EditMode_VisibleBox_1)) {
			NGTLog.debug("[MCTE](Edit) Not paste mode");
			return null;
		}

		MovingObjectPosition target = this.getEntity().getTarget(true);
		if (target == null) {
			NGTLog.debug("[MCTE](Edit) MovingObjectPosition not found");
			return null;
		}

		//貼り付け範囲とクリップボードの範囲が異なる場合
		int[] box = this.getEntity().getPasteBox();
		if (this.clipboard.getSize() != box[0] * box[1] * box[2]) {
			this.getEntity().updateBlockList(null);
			NGTLog.debug("[MCTE](Edit) Illegal block list size");
			return null;
		}

		minX = target.blockX;
		minY = target.blockY;
		minZ = target.blockZ;
		maxX = minX + box[0];
		maxY = minY + box[1];
		maxZ = minZ + box[2];

		return new AABBInt(minX, minY, minZ, maxX, maxY, maxZ);
	}

	/**
	 * 選択領域の編集
	 *
	 * @return 選択領域のサイズ+始点 (NGTO出力で使用)
	 */
	public boolean editBlocks(byte editType, float par2) {
		World world = this.getWorld();

		if (!this.getEntity().isSelectEnd()) {
			NGTLog.debug("[MCTE](Edit) Not select end");
			return false;
		}

		if (world.isRemote) {
			NGTLog.debug("[MCTE](Edit) Can't edit in Client");
			return false;
		}

		AABBInt box = this.getSelectBox();
		if (box == null) {
			return false;
		}

		if (editType == EditType_Minesweeper) {
			if (box.maxY - box.minY != 1) {
				return false;
			}
		}

		if (editType == EditType_Replace || editType == EditType_Minesweeper) {
			this.record(box);
		}

		List<BlockSet> list = new ArrayList<BlockSet>();//コピー用

		int index = 0;
		for (int i = box.minX; i < box.maxX; ++i) {
			for (int j = box.minY; j < box.maxY; ++j) {
				for (int k = box.minZ; k < box.maxZ; ++k) {
					if (editType == EditType_Replace) {
						Block block0 = this.getEntity().getSlotBlock(0);
						Block block1 = this.getEntity().getSlotBlock(1);

						if (block0 != null && block1 != null) {
							int meta0 = this.getEntity().getSlotBlockMetadata(0);
							int meta1 = this.getEntity().getSlotBlockMetadata(1);

							boolean flag0 = (world.getBlock(i, j, k) == block0);
							boolean flag1 = (world.getBlockMetadata(i, j, k) == meta0);
							if (flag0 && flag1) {
								this.setBlock(i, j, k, block1, meta1);
							}
						}
					} else if (editType == EditType_Clone && this.getEntity().hasCloneBox()) {
						BlockSet blockSet = this.getBlockSet(i, j, k);
						int[] box1 = this.getEntity().getCloneBox();
						for (int l = 1; l < box1[3] + 1; ++l) {
							int x = i + (box1[0] * l);
							int y = j + (box1[1] * l);
							int z = k + (box1[2] * l);
							this.setBlock(x, y, z, blockSet);
						}
					} else if (editType == EditType_Minesweeper) {
						int random = world.rand.nextInt((int) par2);
						int meta = (random == 0) ? MinesweeperType.MINE.id : MinesweeperType.NONE.id;
						this.setBlock(i, j, k, MCTE.minesweeper, meta);
						TileEntityMinesweeper tile = (TileEntityMinesweeper) world.getTileEntity(i, j, k);
						tile.setCenter(box.minX, box.minZ);
						tile.setSize(box.maxX - box.minX, box.maxZ - box.minZ);
					}

					++index;
				}
			}
		}

		if (editType == EditType_Miniature) {
			NGTObject object = this.copy(this.getSelectBox(), "").convertNGTO();
			this.getEntity().dropMiniature(object, par2);
		}

		return true;
	}

	public void transformBlocks(byte type) {
		BlockSet[] blocks = new BlockSet[this.clipboard.getSize()];
		int[] box = this.getEntity().getPasteBox();
		int xSize = box[0];
		int ySize = box[1];
		int zSize = box[2];
		//元のサイズ
		int xSize2 = xSize;
		int ySize2 = ySize;
		int zSize2 = zSize;

		if (type == Transform_RotateX) {
			ySize = zSize2;
			zSize = ySize2;
		} else if (type == Transform_RotateY) {
			xSize = zSize2;
			zSize = xSize2;
		} else if (type == Transform_RotateZ) {
			xSize = ySize2;
			ySize = xSize2;
		}

		int index = 0;
		for (int i = 0; i < xSize2; ++i) {
			for (int j = 0; j < ySize2; ++j) {
				for (int k = 0; k < zSize2; ++k) {
					BlockSet set = this.clipboard.getBlocks().get(index);
					int x1 = i;
					int y1 = j;
					int z1 = k;
					if (type == Transform_RotateX) {
						y1 = zSize2 - k - 1;
						z1 = j;
					} else if (type == Transform_RotateY) {
						z1 = xSize2 - i - 1;
						x1 = k;
					} else if (type == Transform_RotateZ) {
						x1 = ySize2 - j - 1;
						y1 = i;
					} else if (type == Transform_MirrorX) {
						x1 = xSize2 - i - 1;
					} else if (type == Transform_MirrorY) {
						y1 = ySize2 - j - 1;
						;
					} else if (type == Transform_MirrorZ) {
						z1 = zSize2 - k - 1;
					}

					int index2 = (x1 * ySize * zSize) + (y1 * zSize) + z1;
					blocks[index2] = set;
					++index;
				}
			}
		}

		this.clipboard = new WorldSnapshot(Arrays.asList(blocks), new AABBInt(0, 0, 0, xSize, ySize, zSize));
		this.getEntity().setPasteBox(xSize, ySize, zSize);
		this.getEntity().updateBlockList(this.clipboard.convertNGTO());
	}

	/**
	 * エディタのスロットにブロックがあればそれを優先、無ければ手に持ったアイテムを使用
	 */
	public BlockSet getFillItem() {
		Block block = this.getEntity().getSlotBlock(0);
		int meta = this.getEntity().getSlotBlockMetadata(0);
		if (block == Blocks.air) {
			ItemStack stack = this.getEntity().getPlayer().inventory.getCurrentItem();
			if (stack != null) {
				if (stack.getItem() == Items.water_bucket) {
					block = Blocks.water;
				} else if (stack.getItem() == Items.lava_bucket) {
					block = Blocks.lava;
				} else {
					block = Block.getBlockFromItem(stack.getItem());
				}

				meta = stack.getItemDamage();
			}
		}

		if (block != null) {
			return new BlockSet(block, meta);
		}

		return BlockSet.AIR;
	}

	public BlockSet getBlockSet(int x, int y, int z) {
		Block block = this.getWorld().getBlock(x, y, z);
		int meta = this.getWorld().getBlockMetadata(x, y, z);
		NBTTagCompound nbt = null;
		if (block.hasTileEntity(meta)) {
			TileEntity tile = this.getWorld().getTileEntity(x, y, z);
			if (tile != null) {
				nbt = new NBTTagCompound();
				tile.writeToNBT(nbt);
			}
		}
		return new BlockSet(x, y, z, block, meta, nbt);
	}

	public void setBlock(int px, int py, int pz, Block block, int metadata) {
		this.setBlock(px, py, pz, new BlockSet(block, metadata));
	}

	public void setBlock(BlockSet blockSet) {
		this.setBlock(blockSet.x, blockSet.y, blockSet.z, blockSet);
	}

	public void setBlock(int px, int py, int pz, BlockSet blockSet) {
		int meta = blockSet.metadata;

		if (blockSet.block instanceof BlockLeavesBase && (meta < 4 || meta > 7)) {
			meta = (meta & 3) + 4;//4~7:手置き, 8~11:コピペ(消える)
		}
		this.getWorld().setBlock(px, py, pz, blockSet.block, meta, 2);

		if (blockSet.block != Blocks.air) {
			this.getWorld().func_147451_t(px, py, pz);//明るさ更新
		}

		if (blockSet.block.hasTileEntity(meta)) {
			TileEntity tile = this.getWorld().getTileEntity(px, py, pz);
			if (tile != null) {
				this.setTileEntityData(tile, blockSet.nbt, px, py, pz);
			}
		}
	}

	private void setTileEntityData(TileEntity tile, NBTTagCompound nbt, int x, int y, int z) {
		int prevX = 0;
		int prevY = 0;
		int prevZ = 0;

		if (nbt != null) {
			NBTTagCompound nbt0 = (NBTTagCompound) nbt.copy();
			prevX = nbt0.getInteger("x");
			prevY = nbt0.getInteger("y");
			prevZ = nbt0.getInteger("z");
			nbt0.setInteger("x", x);
			nbt0.setInteger("y", y);
			nbt0.setInteger("z", z);
			tile.readFromNBT(nbt0);
		}

		if (tile instanceof TileEntityCustom) {
			((TileEntityCustom) tile).setPos(x, y, z, prevX, prevY, prevZ);
		} else {
			tile.xCoord = x;
			tile.yCoord = y;
			tile.zCoord = z;
		}
	}

	/**
	 * クリップボードにコピー
	 */
	public WorldSnapshot copy(AABBInt box, String options) {
		this.clipboard = new WorldSnapshot(this, box, options);
		this.getEntity().setPasteBox(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
		this.getEntity().updateBlockList(this.clipboard.convertNGTO());
		return this.clipboard;
	}

	public void loadData(NGTObject ngto) {
		this.clipboard = new WorldSnapshot(ngto);
	}

	/**
	 * クリップボードの内容を貼り付け
	 */
	public void paste(AABBInt box, String options) {
		this.clipboard.setBlocks(this, box.minX, box.minY, box.minZ, options);
	}

	public void delete(AABBInt box, String options) {
		this.fill(box, BlockSet.AIR, options);
	}

	public void fill(AABBInt box, final BlockSet blockSet, final String options) {
		this.repeat(box, new Repeatable() {
			@Override
			public void processing(AABBInt box, int index, int x, int y, int z) {
				if (options.contains(WorldSnapshot.IGNORE_WATER)) {
					Block block = Editor.this.getWorld().getBlock(x, y, z);
					if (block.getMaterial().isLiquid()) {
						return;
					}
				}
				Editor.this.setBlock(x, y, z, blockSet);
			}
		});
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

	/**
	 * 指定範囲の状態を保存
	 */
	public void record(AABBInt box) {
		this.history.push(new WorldSnapshot(this, box, ""));
	}

	/**
	 * 操作前の状態に戻す
	 */
	public void undo() {
		WorldSnapshot snapshot = this.history.pop();
		if (snapshot != null) {
			snapshot.restore(this);
		}
	}
}
