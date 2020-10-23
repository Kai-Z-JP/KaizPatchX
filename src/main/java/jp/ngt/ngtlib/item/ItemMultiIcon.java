package jp.ngt.ngtlib.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemMultiIcon extends Item {
	private final Map<Integer, String> iconNameMap;
	@SideOnly(Side.CLIENT)
	private Map<Integer, IIcon> iconMap;
	@SideOnly(Side.CLIENT)
	public static IIcon MISSING_ICON;//TextureMap.missingImage

	public ItemMultiIcon(Map<Integer, String> par1) {
		super();
		this.setHasSubtypes(true);
		this.iconNameMap = par1;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1) {
		return super.getUnlocalizedName() + "." + par1.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		if (this.iconMap.containsKey(par1)) {
			return this.iconMap.get(par1);
		}
		return MISSING_ICON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs tab, List list) {
		for (Entry<Integer, IIcon> entry : this.iconMap.entrySet()) {
			list.add(new ItemStack(par1, 1, entry.getKey()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		this.iconMap = new HashMap<Integer, IIcon>();
		for (Entry<Integer, String> entry : this.iconNameMap.entrySet()) {
			this.iconMap.put(entry.getKey(), register.registerIcon(entry.getValue()));
		}

		if (MISSING_ICON == null) {
			MISSING_ICON = register.registerIcon("ngtlib:missing");
		}
	}
}