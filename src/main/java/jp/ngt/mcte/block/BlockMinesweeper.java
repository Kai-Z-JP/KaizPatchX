package jp.ngt.mcte.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockMinesweeper extends BlockContainer {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockMinesweeper() {
		super(Material.rock);
		this.setHardness(0.0F);
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return MinesweeperType.getType(meta).isBreakable ? 0.1F : 10.0F;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityMinesweeper();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);

			if (meta == MinesweeperType.NONE.id) {
				this.setMinesweeper(world, x, y, z, MinesweeperType.NONE_FLAG.id);
			} else if (meta == MinesweeperType.MINE.id) {
				this.setMinesweeper(world, x, y, z, MinesweeperType.MINE_FLAG.id);
			} else if (meta == MinesweeperType.NONE_FLAG.id) {
				this.setMinesweeper(world, x, y, z, MinesweeperType.NONE.id);
			} else if (meta == MinesweeperType.MINE_FLAG.id) {
				this.setMinesweeper(world, x, y, z, MinesweeperType.MINE.id);
			}

			TileEntityMinesweeper tile2 = (TileEntityMinesweeper) world.getTileEntity(x, y, z);
			tile2.check();
		}

		return true;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta == MinesweeperType.NONE.id) {
				this.open(world, x, y, z, true);
			} else if (meta == MinesweeperType.MINE.id) {
				world.newExplosion(null, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, 4.0F, true, world.getGameRules().getGameRuleBooleanValue("mobGriefing"));
			} else {
				this.setMinesweeper(world, x, y, z, meta);
			}
		}

		return true;
	}

	private void open(World world, int x, int y, int z, boolean byPlayer) {
		int bombCount = 0;
		for (int i = -1; i < 2; ++i) {
			for (int j = -1; j < 2; ++j) {
				int meta = world.getBlockMetadata(x + i, y, z + j);
				if (world.getBlock(x + i, y, z + j) == this && (meta == MinesweeperType.MINE.id || meta == MinesweeperType.MINE_FLAG.id)) {
					++bombCount;
				}
			}
		}

		if (bombCount > 0) {
			this.setMinesweeper(world, x, y, z, bombCount);
		} else {
			this.setMinesweeper(world, x, y, z, 0);
			for (int i = -1; i < 2; ++i) {
				for (int j = -1; j < 2; ++j) {
					if (world.getBlock(x + i, y, z + j) == this && world.getBlockMetadata(x + i, y, z + j) == MinesweeperType.NONE.id) {
						this.open(world, x + i, y, z + j, false);
					}
				}
			}
		}
	}

	private void setMinesweeper(World world, int x, int y, int z, int meta) {
		TileEntityMinesweeper tile = (TileEntityMinesweeper) world.getTileEntity(x, y, z);
		NBTTagCompound nbt = new NBTTagCompound();
		tile.writeToNBT(nbt);
		world.setBlock(x, y, z, this, meta, 3);
		TileEntityMinesweeper tile2 = (TileEntityMinesweeper) world.getTileEntity(x, y, z);
		tile2.readFromNBT(nbt);
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		boolean flag = false;
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			if (meta == MinesweeperType.MINE.id || meta == MinesweeperType.MINE_FLAG.id) {
				flag = true;
			}
		}

		super.onBlockExploded(world, x, y, z, explosion);

		if (flag) {
			world.newExplosion(null, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, 4.0F, true, world.getGameRules().getGameRuleBooleanValue("mobGriefing"));
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
		;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return this.icons[meta];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		this.icons = new IIcon[14];
		for (int i = 0; i < this.icons.length; ++i) {
			this.icons[i] = register.registerIcon("mcte:ms_" + i);
		}
	}

	public enum MinesweeperType {
		NUM0(0, false),
		NUM1(1, false),
		NUM2(2, false),
		NUM3(3, false),
		NUM4(4, false),
		NUM5(5, false),
		NUM6(6, false),
		NUM7(7, false),
		NUM8(8, false),
		N(9, false),
		NONE(10, true),//なし(未開封)
		MINE(11, true),//地雷
		NONE_FLAG(12, false),//なし(旗)
		MINE_FLAG(13, false),//地雷(旗)
		;

		public final byte id;
		public final boolean isBreakable;

		private MinesweeperType(int p1, boolean p2) {
			this.id = (byte) p1;
			this.isBreakable = p2;
		}

		public static MinesweeperType getType(int meta) {
			return values()[meta];
		}
	}
}