package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.entity.train.parts.EntityCargo;
import jp.ngt.rtm.entity.train.parts.EntityContainer;
import jp.ngt.rtm.entity.train.parts.EntityTie;
import jp.ngt.rtm.modelpack.cfg.ContainerConfig;
import jp.ngt.rtm.modelpack.cfg.FirearmConfig;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.IntStream;

public class ItemCargo extends ItemWithModel {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	private static final int ICON_COUNT = 3;

	public ItemCargo() {
		super();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if (itemStack.getItemDamage() != 2)//枕木以外
		{
			return super.onItemRightClick(itemStack, world, player);
		}
		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (!world.isRemote) {
			if (par7 == 0)//up
			{
				--par5;
			} else if (par7 == 1)//down
			{
				++par5;
			} else if (par7 == 2)//south
			{
				--par6;
			} else if (par7 == 3)//north
			{
				++par6;
			} else if (par7 == 4)//east
			{
				--par4;
			} else if (par7 == 5)//west
			{
				++par4;
			}

			ItemStack itemstack = itemStack.copy();
			int damage = itemstack.getItemDamage();
			EntityCargo cargo = this.createCargoEntity(world, itemstack, par4, par5, par6, damage);
			float rotationInterval = 15.0F;
			int yaw = MathHelper.floor_double(NGTMath.normalizeAngle(-player.rotationYaw + 180.0D + (rotationInterval / 2.0D)) / (double) rotationInterval);
			float yawF = (float) yaw * rotationInterval;
			cargo.setPositionAndRotation((double) par4 + 0.5D, par5, (double) par6 + 0.5D, yawF, 0.0F);
			cargo.readCargoFromItem();

			if (damage == 1 && ((EntityArtillery) cargo).getModelName().isEmpty()) {
				((EntityArtillery) cargo).setModelName(getModelName(itemstack));
			} else if (damage == 0 && ((EntityContainer) cargo).getModelName().isEmpty()) {
				((EntityContainer) cargo).setModelName(getModelName(itemstack));
			}

			world.spawnEntityInWorld(cargo);
			--itemStack.stackSize;
		}
		return true;
	}

	public EntityCargo createCargoEntity(World world, ItemStack itemstack, int x, int y, int z, int damage) {
		switch (damage) {
			case 1:
				return new EntityArtillery(world, itemstack, x, y, z);
			case 2:
				return new EntityTie(world, itemstack, x, y, z);
			default:
				return new EntityContainer(world, itemstack, x, y, z);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int i = MathHelper.clamp_int(itemStack.getItemDamage(), 0, ICON_COUNT - 1);
		return super.getUnlocalizedName() + "." + itemStack.getItemDamage();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int par1) {
		int j = MathHelper.clamp_int(par1, 0, ICON_COUNT - 1);
		return this.icons[j];
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item par1, CreativeTabs tabs, List list) {
        IntStream.range(0, ICON_COUNT).mapToObj(j -> new ItemStack(par1, 1, j)).forEach(list::add);
    }

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register) {
		this.icons = new IIcon[ICON_COUNT];
		this.icons[0] = register.registerIcon("rtm:itemContainer");
		this.icons[1] = register.registerIcon("rtm:itemArtillery");
		this.icons[2] = register.registerIcon("rtm:itemTie");
	}

	@Override
	public String getModelType(ItemStack itemStack) {
		switch (itemStack.getItemDamage()) {
			case 0:
				return ContainerConfig.TYPE;
			case 1:
				return FirearmConfig.TYPE;
			default:
				return "";
		}
	}

	@Override
	protected String getDefaultModelName(ItemStack itemStack) {
		switch (itemStack.getItemDamage()) {
			case 0:
				return "19g_JRF_0";
			case 1:
				return "40cmArtillery";
			default:
				return "";
		}
	}
}