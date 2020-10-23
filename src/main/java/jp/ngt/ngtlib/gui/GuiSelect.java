package jp.ngt.ngtlib.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiSlotCustom.SlotElement;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiSelect extends GuiScreenCustom {
	protected GuiScreen prevScreen;
	protected GuiSlotCustom slotCustom;

	public GuiSelect(GuiScreen par1, SlotElement[] par2) {
		this.prevScreen = par1;
		this.mc = NGTUtilClient.getMinecraft();
		int w = 0;
		int h = 0;
		if (par1 == null) {
			ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
			w = scaledresolution.getScaledWidth();
			h = scaledresolution.getScaledHeight();
		} else {
			w = par1.width;
			h = par1.height;
		}
		this.slotCustom = new GuiSlotCustom(this, 10, h - 30, 40, w - 40, w - 80, 24, par2);
		this.slotCustom.registerScrollButtons(4, 5);
		this.slotList.add(this.slotCustom);
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 28, 150, 20, I18n.format("gui.cancel")));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			this.closeScreen();
		}

		super.actionPerformed(button);
	}

	@Override
	protected void onElementClicked(int par1, boolean par2) {
		if (par2) {
			this.closeScreen();
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);
	}

	protected void closeScreen() {
		this.mc.displayGuiScreen(this.prevScreen);
	}

	@SideOnly(Side.CLIENT)
	public static class SlotElementItem<T> extends SlotElement {
		public ISelector selector;
		public T item;
		public String name;
		public ItemStack iconItem;

		/**
		 * @param par1
		 * @param par2 選択アイテム
		 * @param par3 表示される名前
		 * @param par4 アイコン用アイテム
		 */
		public SlotElementItem(ISelector par1, T par2, String par3, ItemStack par4) {
			this.selector = par1;
			this.item = par2;
			this.name = par3;
			this.iconItem = par4;
		}

		@Override
		public void draw(Minecraft par1, int par2, int par3, float par4) {
			func_148171_c(par1, par2 + 1, par3 + 1, 0, 0, par4);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			NGTRenderHelper.getItemRenderer().renderItemIntoGUI(par1.fontRenderer, par1.getTextureManager(), this.iconItem, par2 + 2, par3 + 2);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			par1.fontRenderer.drawString(this.name, par2 + 23, par3 + 6, 0xffffff);
		}

		public static void func_148171_c(Minecraft par1, int par2, int par3, int par4, int par5, float par6) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			par1.getTextureManager().bindTexture(Gui.statIcons);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(par2 + 0, par3 + 18, par6, (float) (par4 + 0) * 0.0078125F, (float) (par5 + 18) * 0.0078125F);
			tessellator.addVertexWithUV(par2 + 18, par3 + 18, par6, (float) (par4 + 18) * 0.0078125F, (float) (par5 + 18) * 0.0078125F);
			tessellator.addVertexWithUV(par2 + 18, par3 + 0, par6, (float) (par4 + 18) * 0.0078125F, (float) (par5 + 0) * 0.0078125F);
			tessellator.addVertexWithUV(par2 + 0, par3 + 0, par6, (float) (par4 + 0) * 0.0078125F, (float) (par5 + 0) * 0.0078125F);
			tessellator.draw();
		}

		@Override
		public void onClicked(int par1, boolean par2) {
			if (par2) {
				this.selector.select(this.item);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public interface ISelector {
		void select(Object par1);
	}
}