package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TabTrainControlPanel {
	private static int nextIndex;
	public static TabTrainControlPanel[] tabArray = new TabTrainControlPanel[3];

	private final int tabIndex;
	private final String tabLabel;
	private final Item iconItem;
	private final ResourceLocation tabTexture;
	private ItemStack itemstack;

	public static final TabTrainControlPanel TAB_Inventory = new TabTrainControlPanel("Player_Inventory", Item.getItemFromBlock(Blocks.chest), new ResourceLocation("rtm", "textures/gui/tab_inventory.png"));
	public static final TabTrainControlPanel TAB_Setting = new TabTrainControlPanel("Setting", RTMItem.crowbar, new ResourceLocation("rtm", "textures/gui/tab_setting.png"));
	public static final TabTrainControlPanel TAB_Function = new TabTrainControlPanel("Function", RTMItem.wrench, new ResourceLocation("rtm", "textures/gui/tab_setting.png"));
	public static final TabTrainControlPanel TAB_Formation = new TabTrainControlPanel("Formation", RTMItem.itemtrain, new ResourceLocation("rtm", "textures/gui/tab_formation.png"));

	public TabTrainControlPanel(String par1, Item par2, ResourceLocation par3) {
		this.tabIndex = nextIndex++;
		this.tabLabel = par1;
		this.iconItem = par2;
		this.tabTexture = par3;

		if (this.tabIndex >= tabArray.length) {
			TabTrainControlPanel[] a0 = new TabTrainControlPanel[this.tabIndex + 1];
            System.arraycopy(tabArray, 0, a0, 0, tabArray.length);
            tabArray = a0;
		}

		tabArray[this.tabIndex] = this;
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		if (this.itemstack == null) {
			this.itemstack = new ItemStack(this.getTabIconItem(), 1, 0);
		}
		return this.itemstack;
	}

	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture() {
		return this.tabTexture;
	}

	public int getTabIndex() {
		return this.tabIndex;
	}

	public String getTabLabel() {
		return this.tabLabel;
	}

	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return this.iconItem;
	}

	@SideOnly(Side.CLIENT)
	public String getTranslatedTabLabel() {
		return this.getTabLabel();
	}

	public int getTabPage() {
		if (this.tabIndex > 11) {
			return ((this.tabIndex - 12) / 10) + 1;
		}
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public int getTabColumn() {
		if (this.tabIndex > 11) {
			return ((this.tabIndex - 12) % 10) % 5;
		}
		return this.tabIndex % 6;
	}

	@SideOnly(Side.CLIENT)
	public boolean isTabInFirstRow() {
		if (this.tabIndex > 11) {
			return ((this.tabIndex - 12) % 10) < 5;
		}
		return this.tabIndex < 6;
	}
}