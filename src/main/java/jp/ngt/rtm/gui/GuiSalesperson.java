package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.RTMItem.MoneyType;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.entity.npc.Menu;
import jp.ngt.rtm.entity.npc.MenuEntry;
import jp.ngt.rtm.network.PacketSyncItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public final class GuiSalesperson extends GuiNPC {
	private final int MIN_SLOT_INDEX = 4 * 9 + 4;
	private final int MENU_BUTTON_ROW = 7;

    protected final boolean isOwner;
    protected final Menu menu;

    private MenuEntry selectedMenu;
    private int amountMoney = 0;
    private boolean showMenu = false;
    private int pageIndex = 0;
    private int maxPage = 0;
    private int selectedRow = 0;
    private String message;

    private final GuiButton[] delButtons = new GuiButton[MENU_BUTTON_ROW];
    private final MenuEntry[] menuItems = new MenuEntry[MENU_BUTTON_ROW];
    private final GuiButton[] decButtons = new GuiButton[MENU_BUTTON_ROW];
    private final GuiButton[] addButtons = new GuiButton[MENU_BUTTON_ROW];
    private final int[] itemCounts = new int[MENU_BUTTON_ROW];

    public GuiSalesperson(EntityPlayer par1, EntityNPC par2) {
        super(par1, par2);

		this.isOwner = (par1.equals(par2.getOwner()));
		this.menu = new Menu(par2.getMenu());
		this.selectedMenu = this.menu.get(0);
		this.message = I18n.format("gui.npc.put_money");
	}

	@Override
	public void initGui() {
		super.initGui();

		if (!this.showMenu) {
			this.buttonList.add(new GuiButtonItem(200, this.guiLeft + 90, this.guiTop + 26, 20, 20));
			this.buttonList.add(new GuiButton(201, this.guiLeft + 85, this.guiTop + 49, 40, 20, I18n.format("gui.npc.buy")));
			GuiButton button = new GuiButton(202, this.guiLeft + 130, this.guiTop + 49, 40, 20, I18n.format("gui.npc.register"));
			button.enabled = this.isOwner;
			this.buttonList.add(button);
		} else {
			this.buttonList.clear();

			this.buttonList.add(new GuiButton(300, this.guiLeft + (this.xSize / 2) - 20, this.guiTop + this.ySize - 25, 40, 20, I18n.format("gui.npc.close")));
            this.buttonList.add(new GuiButton(301, this.guiLeft + this.xSize - 70, this.guiTop + 5, 20, 20, "<"));
            this.buttonList.add(new GuiButton(302, this.guiLeft + this.xSize - 25, this.guiTop + 5, 20, 20, ">"));
            GuiButton bImport = new GuiButton(303, this.guiLeft + 5, this.guiTop + 5, 40, 20, I18n.format("gui.npc.import"));
            GuiButton bExport = new GuiButton(304, this.guiLeft + 50, this.guiTop + 5, 40, 20, I18n.format("gui.npc.export"));
            bImport.enabled = this.isOwner;
            bExport.enabled = this.isOwner;
            this.buttonList.add(bImport);
            this.buttonList.add(bExport);

            List<MenuEntry> list = this.menu.getList();
            IntStream.range(0, MENU_BUTTON_ROW).forEach(i -> {
                int menuIndex = this.pageIndex * MENU_BUTTON_ROW + i;
                if (menuIndex < list.size()) {
                    int y = this.guiTop + 30 + i * 24;
                    this.delButtons[i] = new GuiButton(1000 + i, this.guiLeft + 5, y, 20, 20, "X");
                    this.buttonList.add(this.delButtons[i]);
                    this.menuItems[i] = list.get(menuIndex);
                    this.decButtons[i] = new GuiButton(2000 + i, this.guiLeft + this.xSize - 60, y, 20, 20, "-");
                    this.buttonList.add(this.decButtons[i]);
                    this.addButtons[i] = new GuiButton(3000 + i, this.guiLeft + this.xSize - 25, y, 20, 20, "+");
                    this.buttonList.add(this.addButtons[i]);
                } else {
                    this.delButtons[i] = null;
                    this.menuItems[i] = null;
                    this.decButtons[i] = null;
                    this.addButtons[i] = null;
                }
                this.itemCounts[i] = 0;
            });

			this.maxPage = (this.menu.getList().size() - 1) / MENU_BUTTON_ROW;
			this.selectRow(0);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);

		if (!this.showMenu) {
			if (button.id == 200)//お品書き
			{
				this.setPage(0);
			} else if (button.id == 201)//購入
			{
				if (!this.buy()) {
					this.message = I18n.format("gui.npc.can_not_buy");
				}
			} else if (button.id == 202)//登録
			{
				if (this.registerItem()) {
					this.message = I18n.format("gui.npc.reg_successful");
				} else {
					this.message = I18n.format("gui.npc.reg_failed");
				}
			}
		} else {
			if (button.id == 300)//close
			{
				MenuEntry entry = this.menuItems[this.selectedRow];
				int count = this.itemCounts[this.selectedRow];
				if (count > 0) {
					ItemStack item = entry.item.copy();
					item.stackSize = item.stackSize * count;
					this.selectedMenu = new MenuEntry(item, entry.price * count);
				}
				this.setPage(-1);
			} else if (button.id == 301)//ページ-
			{
				if (this.pageIndex >= 1) {
					this.setPage(--this.pageIndex);
				}
			} else if (button.id == 302)//ページ+
			{
				if (this.pageIndex < this.maxPage) {
					this.setPage(++this.pageIndex);
				}
			} else if (button.id == 303)//import
			{
				this.menu.importFromText();
				this.setPage(this.pageIndex);
			} else if (button.id == 304)//export
			{
				this.menu.exportToText();
			} else if (button.id >= 1000) {
				int menuButtonType = button.id / 1000;
				int menuIndex = button.id % 1000;
				if (menuButtonType == 1)//del
				{
					int idx = this.pageIndex * MENU_BUTTON_ROW + menuIndex;
					this.menu.remove(idx);
					this.setPage(this.pageIndex);
				} else if (menuButtonType == 2)//-
				{
					int count = this.itemCounts[menuIndex];
					if (count > 0) {
						this.itemCounts[menuIndex] = --count;
						this.selectRow(menuIndex);
					}
				} else if (menuButtonType == 3)//+
				{
					int count = this.itemCounts[menuIndex];
					if (count < this.menuItems[menuIndex].maxCount) {
						this.itemCounts[menuIndex] = ++count;
						this.selectRow(menuIndex);
					}
				}
			}
		}
	}

	private void setPage(int page) {
		if (page < 0) {
			this.showMenu = false;
		} else {
			this.pageIndex = page;
			this.showMenu = true;
		}
		this.initGui();
	}

	private void selectRow(int index) {
		this.selectedRow = index;
		boolean notSelected = (this.itemCounts[this.selectedRow] <= 0);

		for (int i = 0; i < MENU_BUTTON_ROW; ++i) {
			if (this.delButtons[i] == null) {
				break;
			}

			boolean isSelected = (this.selectedRow == i) || notSelected;
			this.delButtons[i].enabled = this.isOwner;
			this.addButtons[i].enabled = (this.itemCounts[i] < this.menuItems[i].maxCount) && isSelected;
			this.decButtons[i].enabled = (this.itemCounts[i] > 0) && isSelected;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE || this.mc.gameSettings.keyBindInventory.getKeyCode() == keyCode) {
			this.npc.setMenu(this.menu.toString());
			PacketNBT.sendToServer(this.npc);
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (!this.showMenu) {
			super.drawScreen(mouseX, mouseY, partialTicks);

			int money = this.selectedMenu.price - this.amountMoney;
			this.fontRendererObj.drawString(String.format("￥%d", money), this.guiLeft + 115, this.guiTop + 32, 0x000000);
			this.fontRendererObj.drawString(this.message, this.guiLeft + 83, this.guiTop + 72, 0xFF0000);
		} else {
            this.drawDefaultBackground();

            int x0 = (this.width - this.xSize) / 2;
            int y0 = (this.height - this.ySize) / 2;
            this.drawGradientRect(x0, y0, x0 + this.xSize, y0 + this.ySize, 0xFFE0E0E0, 0xFFE0E0E0);

            for (Object o : this.buttonList) {
                ((GuiButton) o).drawButton(this.mc, mouseX, mouseY);
            }

            this.fontRendererObj.drawString(String.format("%d/%d", this.pageIndex, this.maxPage),
                    this.guiLeft + this.xSize - 45, this.guiTop + 10, 0x000000);

            for (int i = 0; i < MENU_BUTTON_ROW; ++i) {
                if (this.menuItems[i] == null) {
                    break;
                }

				int y = this.guiTop + 35 + i * 24;

				MenuEntry entry = this.menuItems[i];
				drawItem(this.mc, entry.item, this.guiLeft + 27, y - 3);
				this.fontRendererObj.drawString(entry.item.getDisplayName(), this.guiLeft + 50, y - 5, 0x000000);
				this.fontRendererObj.drawString(String.format("￥%d", entry.price), this.guiLeft + 50, y + 5, 0x000000);
				this.fontRendererObj.drawString(String.format("%d", this.itemCounts[i]), this.guiLeft + this.xSize - 35, y, 0x000000);
			}
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, int type) {
		super.handleMouseClick(slot, slotId, mouseButton, type);

		this.amountMoney = this.countMoney(false);
		if (this.amountMoney > 0) {
			this.message = I18n.format("gui.npc.push_buy");
		} else {
			this.message = I18n.format("gui.npc.put_money");
		}
	}

	private boolean buy() {
		int amount = this.countMoney(false);
		if (amount < this.selectedMenu.price) {
			return false;
		}
		this.countMoney(true);//支払い

		boolean passed = false;
		amount -= this.selectedMenu.price;

		for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);
			if (slot.slotNumber >= MIN_SLOT_INDEX) {
				if (!slot.getHasStack()) {
					if (!passed) {
						//slot.putStack(this.selectedMenu.item.copy());
						this.changeSlot(this.selectedMenu.item.copy(), i);
						passed = true;
					} else if (amount > 0) {
						int maxId = MoneyType.values().length - 1;
						for (int id = maxId; id >= 0; --id) {
							int price = MoneyType.getPrice(id);
							if (amount >= price) {
								int count = amount / price;
								amount -= count * price;
								//slot.putStack(new ItemStack(RTMItem.money, count, id));
								this.changeSlot(new ItemStack(RTMItem.money, count, id), i);
								break;
							}
						}
					} else {
						break;
					}
				}
			}
		}

		return true;
	}

	private boolean registerItem() {
		ItemStack item = ((Slot) this.inventorySlots.inventorySlots.get(MIN_SLOT_INDEX)).getStack();
		int price = this.countMoney(false);
		if (item != null && !this.isMoney(item) && price > 0) {
			return this.menu.add(new MenuEntry(item.copy(), price));
		}
		return false;
	}

	private int countMoney(boolean pay) {
		int amount = 0;
		for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);
			if (slot.slotNumber >= MIN_SLOT_INDEX) {
				ItemStack item = slot.getStack();
				if (this.isMoney(item)) {
					amount += MoneyType.getPrice(item.getItemDamage()) * item.stackSize;

					if (pay) {
						//slot.decrStackSize(item.getCount());
						this.changeSlot(null, i);
					}
				}
			}
		}

		return amount;
	}

	private boolean isMoney(ItemStack item) {
		return (item != null && item.getItem() == RTMItem.money);
	}

	private void changeSlot(ItemStack item, int id) {
		RTMCore.NETWORK_WRAPPER.sendToServer(new PacketSyncItem(this.player, item, id));
		//this.mc.playerController.sendSlotPacket(item, id);
	}

	private static void drawItem(Minecraft mc, ItemStack item, int x, int y) {
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();
		NGTRenderHelper.getItemRenderer().renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);
		NGTRenderHelper.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y, null);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	public class GuiButtonItem extends GuiButton {
		public GuiButtonItem(int buttonId, int x, int y, int widthIn, int heightIn) {
			super(buttonId, x, y, widthIn, heightIn, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			super.drawButton(mc, mouseX, mouseY);

			drawItem(mc, GuiSalesperson.this.selectedMenu.item, this.xPosition + 2, this.yPosition + 2);
		}
	}
}
