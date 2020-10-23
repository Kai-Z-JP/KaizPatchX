package jp.ngt.mcte.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.block.MiniatureBlockState;
import jp.ngt.mcte.block.TileEntityMiniature;
import jp.ngt.ngtlib.block.NGTObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class ItemMiniature extends Item {
	private static final float[] offset = new float[3];

	public ItemMiniature() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 0 || armorType == 1;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		if (world.isRemote) {
			player.openGui(MCTE.instance, MCTE.guiIdItemMiniature, player.worldObj, 0, 0, 0);
		}
		return itemstack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		if (itemStack.hasTagCompound()) {
			if (!world.isRemote) {
				if (par7 == 0)//up
				{
					--y;
				} else if (par7 == 1)//down
				{
					++y;
				} else if (par7 == 2)//south
				{
					--z;
				} else if (par7 == 3)//north
				{
					++z;
				} else if (par7 == 4)//east
				{
					--x;
				} else if (par7 == 5)//west
				{
					++x;
				}

				if (!player.canPlayerEdit(x, y, z, par7, itemStack)) {
					return true;
				}
				if (!world.getBlock(x, y, z).isReplaceable(world, x, y, z)) {
					return true;
				}

				NBTTagCompound nbt = itemStack.getTagCompound();

				NGTObject object = getNGTObject(nbt);
				if (object != null) {
					float scale = getScale(nbt);
					float[] fa = getOffset(nbt);
					MiniatureMode mode = getMode(nbt);
					MiniatureBlockState state = getMiniatureBlockState(nbt);

					world.setBlock(x, y, z, MCTE.miniature, 0, 3);
					TileEntityMiniature tile = (TileEntityMiniature) world.getTileEntity(x, y, z);
					tile.setRotation(player, MCTE.rotationInterval, false);
					tile.attachSide = (byte) par7;
					tile.setMBState(state);
					tile.setBlockState(object, scale, fa[0], fa[1], fa[2], mode);
					world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, MCTE.miniature.stepSound.func_150496_b(), (MCTE.miniature.stepSound.getVolume() + 1.0F) / 2.0F, MCTE.miniature.stepSound.getPitch() * 0.8F);
					--itemStack.stackSize;
				}
			}
		} else {
			if (world.isRemote) {
				player.openGui(MCTE.instance, MCTE.guiIdItemMiniature, player.worldObj, 0, 0, 0);
			}
		}

		return true;
	}

	public static NGTObject getNGTObject(NBTTagCompound nbt) {
		if (nbt.hasKey("BlocksData")) {
			NBTTagCompound data = nbt.getCompoundTag("BlocksData");
			return NGTObject.readFromNBT(data);
		}
		return null;
	}

	public static void setNGTObject(NGTObject obj, NBTTagCompound nbt) {
		NBTTagCompound data = obj.writeToNBT();
		nbt.setTag("BlocksData", data);
	}

	public static float getScale(NBTTagCompound nbt) {
		if (nbt.hasKey("Scale")) {
			return nbt.getFloat("Scale");
		} else if (nbt.hasKey("MinimizeRate"))//互換性
		{
			int i = nbt.getInteger("MinimizeRate");
			if (i <= 0) {
				i = 1;
			}
			return 1.0F / (float) i;
		}
		return 1.0F;
	}

	public static void setScale(float scale, NBTTagCompound nbt) {
		nbt.setFloat("Scale", scale);
	}

	public static float[] getOffset(NBTTagCompound nbt) {
		offset[0] = nbt.getFloat("OffsetX");
		offset[1] = nbt.getFloat("OffsetY");
		offset[2] = nbt.getFloat("OffsetZ");
		return offset;
	}

	public static void setOffset(NBTTagCompound nbt, float x, float y, float z) {
		nbt.setFloat("OffsetX", x);
		nbt.setFloat("OffsetY", y);
		nbt.setFloat("OffsetZ", z);
	}

	public static MiniatureMode getMode(NBTTagCompound nbt) {
		int i = nbt.getByte("Mode");
		return MiniatureMode.values()[i];
	}

	public static void setMode(NBTTagCompound nbt, MiniatureMode mode) {
		nbt.setByte("Mode", (byte) mode.id);
	}

	public static MiniatureBlockState getMiniatureBlockState(NBTTagCompound nbt) {
		MiniatureBlockState state;
		if (nbt.hasKey("MBState")) {
			state = MiniatureBlockState.readFromNBT(nbt.getCompoundTag("MBState"));
		} else {
			state = new MiniatureBlockState();
			state.lightValue = nbt.getByte("LightValue");
		}
		return state;
	}

	public static void setMiniatureBlockState(NBTTagCompound nbt, MiniatureBlockState state) {
		nbt.setTag("MBState", state.writeToNBT());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		if (itemStack.hasTagCompound()) {
			NBTTagCompound nbt = itemStack.getTagCompound();
			NBTTagCompound data = nbt.getCompoundTag("BlocksData");
			float scale = getScale(nbt);
			NGTObject.addInformation(list, data, scale);
		}
	}

	/**
	 * @param par1
	 * @param par2 スケール
	 */
	public static ItemStack createMiniatureItem(NGTObject par1, float par2, float x, float y, float z, MiniatureMode mode, MiniatureBlockState state) {
		ItemStack stack = new ItemStack(MCTE.itemMiniature, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("BlocksData", par1.writeToNBT());
		setScale(par2, nbt);
		setOffset(nbt, x, y, z);
		setMode(nbt, mode);
		setMiniatureBlockState(nbt, state);
		stack.setTagCompound(nbt);
		return stack;
	}

	/**
	 * 武器として使用
	 */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			NGTObject ngto = getNGTObject(nbt);
			float scale = getScale(nbt);
			float power = (ngto.blockList.size() / 16) * scale;
			entity.attackEntityFrom(DamageSource.causePlayerDamage(player), power);
			//NGTLog.debug("at:%f", power);
		}
		return false;
	}

	private static int nextId;

	public enum MiniatureMode {
		miniature(),
		sculpture();

		public final int id;

		MiniatureMode() {
			this.id = nextId++;
		}
	}
}