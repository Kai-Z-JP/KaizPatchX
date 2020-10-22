package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.item.PainterSetting;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiSelect;
import jp.ngt.ngtlib.gui.GuiSelect.ISelector;
import jp.ngt.ngtlib.gui.GuiSelect.SlotElementItem;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPainter extends GuiScreenCustom {
	private final EntityPlayer player;
	private final ItemStack painterItem;
	private PainterSetting setting;

	private GuiButtonItem buttonFillBlock;
	private GuiButtonItem buttonRewriteBlock;
	private GuiButton buttonFill;
	private GuiButton buttonRewrite;
	private GuiButton buttonDrawMode;
	private GuiTextField fieldSize;

	public GuiPainter(EntityPlayer par1) {
		this.player = par1;
		this.painterItem = par1.inventory.getCurrentItem();
		this.setting = PainterSetting.getPainterSettingFromItem(this.painterItem);
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));

		if (this.buttonFillBlock == null)//ブロック選択画面での内容の保持のため
		{
			this.buttonFillBlock = new GuiButtonItem(10, 30, 10, 60, "fill", this.setting.fillBlock);
		}
		this.buttonList.add(this.buttonFillBlock);

		if (this.buttonRewriteBlock == null) {
			this.buttonRewriteBlock = new GuiButtonItem(11, 30, 30, 60, "rewrite", this.setting.rewriteBlock);
		}
		this.buttonList.add(this.buttonRewriteBlock);

		this.buttonFill = new GuiButton(20, 30, 50, 60, 20, "Fill:" + this.setting.fill);
		this.buttonList.add(this.buttonFill);
		this.buttonRewrite = new GuiButton(21, 30, 70, 60, 20, "Rewrite:" + this.setting.rewrite);
		this.buttonList.add(this.buttonRewrite);
		this.buttonDrawMode = new GuiButton(22, 30, 90, 60, 20, "DrawMode:" + this.setting.drawMode);
		this.buttonList.add(this.buttonDrawMode);

		this.textFields.clear();
		this.fieldSize = this.setTextField(30, 110, 60, 20, String.valueOf(this.setting.size));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.sendPacket();
			this.mc.displayGuiScreen(null);
		} else if (button.id == 1) {
			this.mc.displayGuiScreen(null);
		} else if (button.id == 10) {
			this.selectBlock(this.buttonFillBlock);
		} else if (button.id == 11) {
			this.selectBlock(this.buttonRewriteBlock);
		} else if (button.id == 20) {
			this.setting.fill = !this.setting.fill;
			this.buttonFill.displayString = "Fill:" + this.setting.fill;
		} else if (button.id == 21) {
			this.setting.rewrite = !this.setting.rewrite;
			this.buttonRewrite.displayString = "Rewrite:" + this.setting.rewrite;
		} else if (button.id == 22) {
			this.setting.drawMode = (byte) ((this.setting.drawMode + 1) % 2);
			this.buttonDrawMode.displayString = "DrawMode:" + this.setting.drawMode;
		}
	}

	private void sendPacket() {
		this.setting.fillBlock = this.buttonFillBlock.getBlockSet();
		this.setting.rewriteBlock = this.buttonRewriteBlock.getBlockSet();

		try {
			this.setting.size = Integer.parseInt(this.fieldSize.getText());
		} catch (NumberFormatException e) {
			this.fieldSize.setText(String.valueOf(this.setting.size));
		}

		this.writeNBT();
		NGTUtil.sendPacketToServer(this.player, this.painterItem);
	}

	private void writeNBT() {
		this.setting.writePainterSettingToItem(this.painterItem);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);
	}

	private void selectBlock(final GuiButtonItem button) {
		Iterator<Block> iterator0 = Block.blockRegistry.iterator();
		List<ItemStack> list0 = new ArrayList<ItemStack>();
		while (iterator0.hasNext()) {
			Block block = iterator0.next();
			Item item = Item.getItemFromBlock(block);
			if (item != null) {
				block.getSubBlocks(item, CreativeTabs.tabAllSearch, list0);
			}
		}

		ISelector selector = new ISelector() {
			@Override
			public void select(Object par1) {
				button.setItem((ItemStack) par1);
			}

		};

		SlotElementItem[] slots = new SlotElementItem[list0.size()];
		for (int i = 0; i < slots.length; ++i) {
			ItemStack stack = list0.get(i);
			String s = stack.getDisplayName();
			slots[i] = new SlotElementItem<ItemStack>(selector, stack, s, stack);
		}

		this.mc.displayGuiScreen(new GuiSelect(this, slots));
	}
}