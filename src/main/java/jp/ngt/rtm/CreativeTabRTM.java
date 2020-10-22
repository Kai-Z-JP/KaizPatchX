package jp.ngt.rtm;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabRTM extends CreativeTabs {
	public static final CreativeTabs tabRailway = new CreativeTabRTM("rtm_railway");
	public static final CreativeTabs tabIndustry = new CreativeTabRTM("rtm_industry");
	public static final CreativeTabs tabRTMTools = new CreativeTabRTM("rtm_tools");

	public CreativeTabRTM(String label) {
		super(label);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		if (this == tabRailway) {
			return RTMItem.itemtrain;
		} else if (this == tabIndustry) {
			return RTMItem.steel_ingot;
		} else if (this == tabRTMTools) {
			return RTMItem.crowbar;
		}
		return RTMItem.crowbar;
	}
}