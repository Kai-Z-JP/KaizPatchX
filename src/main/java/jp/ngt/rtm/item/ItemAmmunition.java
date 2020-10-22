package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * 弾薬
 */
public class ItemAmmunition extends Item {
	@SideOnly(Side.CLIENT)
	private IIcon[] iconarray;
	public static final int num = BulletType.values().length;

	public ItemAmmunition() {
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack par1) {
		int i = par1.getItemDamage() / 4;
		return super.getUnlocalizedName() + "." + i;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1) {
		int i0 = par1.getItemDamage() % 4;
		String s0 = i0 == 0 ? "item.ammo.name" : (i0 == 1) ? "item.bullet.name" : "item.case.name";
		String s1 = StatCollector.translateToLocal(this.getUnlocalizedName(par1) + ".name");
		String s2 = StatCollector.translateToLocal(s0);
		return (s1 + s2).trim();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		int i0 = MathHelper.clamp_int(par1, 0, (num * 4) - 1);
		int i1 = i0 / 4;
		int i2 = 0;
		switch (i1) {
			case 0:
				i2 = 0;
				break;
			case 1:
				i2 = 1;
				break;
			case 2:
				i2 = 2;
				break;
			case 3:
				i2 = 2;
				break;
			case 4:
				i2 = 2;
				break;
			case 5:
				i2 = 0;
				break;
		}
		int i3 = i0 % 4;
		return this.iconarray[(i2 * 4) + i3];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (int i = 0; i < num; ++i) {
			if (i == 2) {
				continue;
			}
			int i0 = i * 4;
			par3List.add(new ItemStack(par1, 1, i0));//弾薬
			par3List.add(new ItemStack(par1, 1, i0 + 1));//弾
			par3List.add(new ItemStack(par1, 1, i0 + 2));//薬莢
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.iconarray = new IIcon[12];
		this.iconarray[0] = par1IconRegister.registerIcon("rtm:ammo_cannonball");
		this.iconarray[1] = par1IconRegister.registerIcon("rtm:bullet_cannonball");
		this.iconarray[2] = par1IconRegister.registerIcon("rtm:case_cannonball");

		this.iconarray[4] = par1IconRegister.registerIcon("rtm:ammo_handgun");
		this.iconarray[5] = par1IconRegister.registerIcon("rtm:bullet_handgun");
		this.iconarray[6] = par1IconRegister.registerIcon("rtm:case_handgun");

		this.iconarray[8] = par1IconRegister.registerIcon("rtm:ammo_rifle");
		this.iconarray[9] = par1IconRegister.registerIcon("rtm:bullet_rifle");
		this.iconarray[10] = par1IconRegister.registerIcon("rtm:case_rifle");
	}

	public enum BulletType {
		cannon_40cm(0, 40.0F, false),
		handgun_9mm(1, 8.0F, true),
		rifle_5_56mm(2, 12.0F, true),
		rifle_7_62mm(3, 12.0F, true),
		rifle_12_7mm(4, 24.0F, true),
		cannon_Atomic(5, 100.0F, false);

		public final byte id;
		public final float damage;
		public final boolean muzzleFlash;


		private BulletType(int par1, float par2, boolean par3) {
			this.id = (byte) par1;
			this.damage = par2;
			this.muzzleFlash = par3;
		}

		public static BulletType getBulletType(int id) {
			switch (id) {
				case 0:
					return cannon_40cm;
				case 1:
					return handgun_9mm;
				case 2:
					return rifle_5_56mm;
				case 3:
					return rifle_7_62mm;
				case 4:
					return rifle_12_7mm;
				case 5:
					return cannon_Atomic;
				default:
					return handgun_9mm;
			}
		}
	}
}