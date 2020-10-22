package jp.ngt.mcte.gui;

import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiButtonItem extends GuiButton {
	public ItemStack displayItem;

	public GuiButtonItem(int id, int xPos, int yPos, int width, String string, BlockSet set) {
		super(id, xPos, yPos, width, 20, string);
		this.displayItem = new ItemStack(set.block, 1, set.metadata);
		if (this.displayItem.getItem() == null) {
			this.displayItem = null;
		}
	}

	@Override
	public void drawButton(Minecraft par1, int par2, int par3) {
		if (this.visible) {
			FontRenderer fontrenderer = par1.fontRenderer;
			par1.getTextureManager().bindTexture(buttonTextures);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
			int k = this.getHoverState(this.field_146123_n);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
			this.mouseDragged(par1, par2, par3);
			int l = 14737632;

			if (packedFGColour != 0) {
				l = packedFGColour;
			} else if (!this.enabled) {
				l = 10526880;
			} else if (this.field_146123_n) {
				l = 16777120;
			}

			this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);

			if (this.displayItem != null) {
				this.drawItem(par1, this.xPosition, this.yPosition);
			}
		}
	}

	private void drawItem(Minecraft par1, int par2, int par3) {
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();
		NGTRenderHelper.getItemRenderer().renderItemIntoGUI(par1.fontRenderer, par1.getTextureManager(), this.displayItem, par2 + 2, par3 + 2);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	public void setItem(ItemStack par1) {
		this.displayItem = par1;
	}

	public BlockSet getBlockSet() {
		if (this.displayItem == null) {
			return new BlockSet(Blocks.air, 0);
		}

		Block block = Block.getBlockFromItem(this.displayItem.getItem());
		int meta = this.displayItem.getItemDamage();
		return new BlockSet(block, meta);
	}
}