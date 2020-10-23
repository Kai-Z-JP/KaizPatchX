package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.network.PacketSelectModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiNPC extends GuiContainer {
	private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/gui/npc.png");

	protected EntityPlayer player;
	protected EntityNPC npc;
	private final List<ModelSetBase> modelList;
	private int index;

	public GuiNPC(EntityPlayer par1, EntityNPC par2) {
		super(new ContainerNPC(par1, par2));
		this.player = par1;
		this.npc = par2;
		this.modelList = ModelPackManager.INSTANCE.getModelList(par2.getModelType());

		this.xSize = 176;
		this.ySize = 224;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.buttonList.add(new GuiButtonNPC(100, this.guiLeft + 78 - 10, this.guiTop + 78 - 15, 0));
		this.buttonList.add(new GuiButtonNPC(101, this.guiLeft + 26, this.guiTop + 78 - 15, 1));

		this.index = this.modelList.indexOf(this.npc.getModelSet());
		if (this.index == -1) {
			this.index = 0;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(texture);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
		//Entityモデル描画
		GuiInventory.func_147046_a(k + 51, l + 75, 30, (float) (k + 51 - x), (float) (l + 25 - y), this.npc);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 100) {
			this.setModel(++this.index);
		} else if (button.id == 101) {
			this.setModel(--this.index);
		}
	}

	private void setModel(int par1) {
		if (par1 >= this.modelList.size()) {
			par1 = 0;
		} else if (par1 < 0) {
			par1 = this.modelList.size() - 1;
		}
		this.index = par1;
		String s = this.modelList.get(par1).getConfig().getName();
		RTMCore.NETWORK_WRAPPER.sendToServer(new PacketSelectModel(this.npc, s));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);

		this.fontRendererObj.drawString(this.npc.getModelName(), this.guiLeft + 90, this.guiTop + 8, 0x000000);
		this.fontRendererObj.drawString("HP:" + this.npc.getHealth(), this.guiLeft + 90, this.guiTop + 16, 0x000000);
	}

	private class GuiButtonNPC extends GuiButton {
		private final int type;

		public GuiButtonNPC(int id, int x, int y, int t) {
			super(id, x, y, 10, 15, "");
			this.type = t;
		}

		@Override
		public void drawButton(Minecraft mc, int x, int y) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(texture);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
				int k = this.getHoverState(this.field_146123_n);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				int u = ((k == 2) ? 16 : 0) + 176;
				int v = (this.type == 1) ? 16 : 0;
				this.drawTexturedModalRect(this.xPosition, this.yPosition, u, v, 10, 15);
				this.mouseDragged(mc, x, y);
			}
		}
	}
}