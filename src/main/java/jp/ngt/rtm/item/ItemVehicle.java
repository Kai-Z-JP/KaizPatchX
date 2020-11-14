package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.vehicle.EntityCar;
import jp.ngt.rtm.entity.vehicle.EntityPlane;
import jp.ngt.rtm.entity.vehicle.EntityShip;
import jp.ngt.rtm.entity.vehicle.EntityVehicle;
import jp.ngt.rtm.modelpack.cfg.VehicleConfig;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import java.util.List;

public class ItemVehicle extends ItemWithModel {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemVehicle() {
		super();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if (itemStack.getItemDamage() == 1) {
			if (!PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_VEHICLE)) {
				return itemStack;
			}

			MovingObjectPosition mop = BlockUtil.getMOPFromPlayer(player, 5.0D, true);
			if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
				int x = mop.blockX;
				int y = mop.blockY;
				int z = mop.blockZ;
				if (world.getBlock(x, y, z).getMaterial().isLiquid()) {
					if (!world.isRemote) {
						EntityVehicle vehicle = new EntityShip(world);
						vehicle.setPosition((double) x + 0.5D, (double) y + 1.0D, (double) z + 0.5D);
						vehicle.rotationYaw = MathHelper.wrapAngleTo180_float(-player.rotationYaw);
						vehicle.setModelName(getModelName(itemStack));
						world.spawnEntityInWorld(vehicle);
					}

					if (!player.capabilities.isCreativeMode) {
						--itemStack.stackSize;
					}

					return itemStack;
				}
			}
		}
		return super.onItemRightClick(itemStack, world, player);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		if (par7 != 1) {
			return true;
		}

		if (!PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_VEHICLE)) {
			return true;
		}

		EntityVehicle vehicle = null;
		switch (itemStack.getItemDamage()) {
			case 0:
				vehicle = new EntityCar(world);
				break;
			//case 1: vehicle = new EntityShip(world);break;
			case 2:
				vehicle = new EntityPlane(world);
				break;
		}

		if (vehicle != null) {
			if (!world.isRemote) {
				vehicle.setPosition((double) x + 0.5D, (double) y + 1.0D, (double) z + 0.5D);
				vehicle.rotationYaw = MathHelper.wrapAngleTo180_float(-player.rotationYaw);
				vehicle.setModelName(getModelName(itemStack));
				vehicle.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
				world.spawnEntityInWorld(vehicle);
			}

			if (!player.capabilities.isCreativeMode) {
				--itemStack.stackSize;
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1) {
		return this.getUnlocalizedName() + "." + par1.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2, List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		int j = MathHelper.clamp_int(par1, 0, 2);
		return this.icons[j];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		this.icons = new IIcon[3];
		this.icons[0] = register.registerIcon("rtm:itemCar");
		this.icons[1] = register.registerIcon("rtm:itemShip");
		this.icons[2] = register.registerIcon("rtm:itemPlane");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		super.addInformation(itemStack, player, list, par4);
	}

	@Override
	protected String getModelType(ItemStack itemStack) {
		return VehicleConfig.TYPE;
	}

	@Override
	protected String getDefaultModelName(ItemStack itemStack) {
		switch (itemStack.getItemDamage()) {
            case 1:
				return "WoodBoat";
			case 2:
				return "NGT-1";
			default:
				return "CV33";
		}
	}

	@Override
	public String getSubType(ItemStack itemStack) {
		switch (itemStack.getItemDamage()) {
			case 0:
				return "Car";
			case 1:
				return "Ship";
			case 2:
				return "Plane";
			default:
				return "";
		}
	}
}