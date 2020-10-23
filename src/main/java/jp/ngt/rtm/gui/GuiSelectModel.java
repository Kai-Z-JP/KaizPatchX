package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.IConfigWithType;
import jp.ngt.rtm.modelpack.modelset.IModelSetClient;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.modelpack.state.ResourceState;
import jp.ngt.rtm.network.PacketSelectModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSelectModel extends GuiScreenCustom {
	public static final ResourceLocation ButtonBlue = new ResourceLocation("rtm", "textures/gui/button_blue.png");

	public final IModelSelector selector;
	private List<ModelSetBase> modelListAll;
	private List<ModelSetBase> modelListSelect;
	private GuiButtonSelectModel[] selectButtons;
	private GuiTextField argField;
	private GuiTextField searchField;

	private int currentScroll;
	private boolean wasClicking;

	public GuiSelectModel(World par1, IModelSelector par2) {
		this.xSize = 352;
		this.ySize = 240;
		this.selector = par2;

		this.modelListAll = ModelPackManager.INSTANCE.getModelList(par2.getModelType());
		this.modelListSelect = new ArrayList<>();
	}

	public GuiSelectModel(World par1, IModelSelectorWithType par2) {
		this(par1, (IModelSelector) par2);

		String type = par2.getSubType();
		List<ModelSetBase> list = ModelPackManager.INSTANCE.getModelList(par2.getModelType());
		this.modelListAll = new ArrayList<>();
		for (ModelSetBase modelSet : list) {
			if (type.isEmpty() || ((IConfigWithType) modelSet.getConfig()).getSubType().equals(type)) {
				this.modelListAll.add(modelSet);
			}
		}
		this.modelListSelect = new ArrayList<>();
	}

	@Override
	public void initGui() {
		super.initGui();

		this.searchField = this.setTextField(this.width - 120, 5, 100, 20, "");
		ResourceState state = this.selector.getResourceState();
		state.getResourceSet();//arg初期化
		this.argField = this.setTextField(this.width - 120, 30, 100, 20, state.getArg());

		this.resetModelList();
	}

	/**
	 * 入力されたキーワードを含むモデルを抽出
	 */
	private void resetModelList() {
		this.modelListSelect.clear();
		this.currentScroll = 0;

		String keyword = this.searchField.getText();
		if (keyword == null || keyword.length() == 0) {
			this.modelListSelect.addAll(this.modelListAll);
		} else {
			for (ModelSetBase set : this.modelListAll) {
				if (set.getConfig().tags.contains(keyword)) {
					this.modelListSelect.add(set);
				}
			}
		}

		//名前順にソート
		this.modelListSelect.sort(Comparator.comparing(o -> o.getConfig().getName()));

		this.buttonList.clear();

		int i0 = (this.height / 2) - 16;
		this.selectButtons = new GuiButtonSelectModel[this.modelListSelect.size()];
		for (int i = 0; i < this.selectButtons.length; ++i) {
			ModelSetBase modelSet = this.modelListSelect.get(i);
			this.selectButtons[i] = new GuiButtonSelectModel(i, 10, i0 + 32 * i,
					(IModelSetClient) modelSet, modelSet.getConfig().getName(), this);
			this.buttonList.add(this.selectButtons[i]);

			if (modelSet.getConfig().getName().equals(this.selector.getModelName())) {
				this.currentScroll = i;
				this.selectButtons[i].isSelected = true;
			}
		}
		this.resetButtonPos();

		this.buttonList.add(new GuiButton(900, this.width + 36, this.height - 20, 100, 20, "cansel"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		//ここにConfigのモデルの説明文を表示
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.modelListSelect.size() > 0) {
			this.drawScrollBar(par2, par3);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (this.modelListAll.size() == 0) {
			this.fontRendererObj.drawString("Can't get list", (this.width - this.xSize) / 2, (this.height - this.ySize) / 2, 0xff0000);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.wasClicking = Mouse.isButtonDown(0);
		boolean clickIsAvailable = par1 < this.width && par1 >= this.width - 16;
		if (this.wasClicking && clickIsAvailable) {
			int mouseY = par2 < 8 ? 8 : (Math.min(par2, this.height));
			int i1 = MathHelper.floor_float((float) mouseY * (float) (this.modelListSelect.size() + 1) / (float) (this.height - 16));
			this.scroll(i1);
		}

		this.drawDefaultBackground();

		super.drawScreen(par1, par2, par3);
	}

	private void drawScrollBar(int mouseX, int mouseY) {
		Tessellator tessellator = Tessellator.instance;

		//バー描画
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.setColorOpaque_I(0xFFFFFF);
		tessellator.startDrawingQuads();
		tessellator.addVertex(this.width - 7, this.height - 8, this.zLevel);
		tessellator.addVertex(this.width - 7, 8.0D, this.zLevel);
		tessellator.addVertex(this.width - 9, 8.0D, this.zLevel);
		tessellator.addVertex(this.width - 9, this.height - 8, this.zLevel);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		int buttonY = 0;
		if (this.wasClicking) {
			buttonY = (mouseY < 8 ? 8 : (Math.min(mouseY, this.height - 8))) - 8;
		} else if (this.modelListSelect.size() > 1) {
			buttonY = this.currentScroll * (this.height - 16) / (this.modelListSelect.size() - 1);
		}

		//ボタン描画
		this.mc.getTextureManager().bindTexture(ButtonBlue);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(this.width, buttonY + 16, this.zLevel, 1.0D, 0.0625D);
		tessellator.addVertexWithUV(this.width, buttonY, this.zLevel, 1.0D, 0.0D);
		tessellator.addVertexWithUV(this.width - 16, buttonY, this.zLevel, 0.9375D, 0.0D);
		tessellator.addVertexWithUV(this.width - 16, buttonY + 16, this.zLevel, 0.9375D, 0.0625D);
		tessellator.draw();
	}

	@Override
	public void drawTexturedModalRect(int x, int y, int z, int u, int v, int p_73729_6_) {
		float f = 0.001953125F;
		float f1 = 0.001953125F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + p_73729_6_, this.zLevel, (float) (z) * f, (float) (u + p_73729_6_) * f1);
		tessellator.addVertexWithUV(x + v, y + p_73729_6_, this.zLevel, (float) (z + v) * f, (float) (u + p_73729_6_) * f1);
		tessellator.addVertexWithUV(x + v, y, this.zLevel, (float) (z + v) * f, (float) (u) * f1);
		tessellator.addVertexWithUV(x, y, this.zLevel, (float) (z) * f, (float) (u) * f1);
		tessellator.draw();
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		super.keyTyped(par1, par2);

		this.resetModelList();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 900) {
			if (this.selector.closeGui(null, null)) {
				this.mc.displayGuiScreen(null);
			}
		}

		if (button.id < this.modelListSelect.size()) {
			ResourceState state = this.selector.getResourceState();
			state.setArg(this.argField.getText(), true);
			String s = this.modelListSelect.get(button.id).getConfig().getName();
			if (this.selector.closeGui(s, state)) {
				RTMCore.NETWORK_WRAPPER.sendToServer(new PacketSelectModel(this.selector, s));
				this.mc.displayGuiScreen(null);
			}
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		int i0 = Mouse.getEventDWheel();
		if (i0 != 0) {
			if (i0 > 0) {
				i0 = 1;
			} else {
				i0 = -1;
			}

			this.scroll(this.currentScroll - i0);
		}
	}

	private void scroll(int par1) {
		this.currentScroll = par1;

		if (this.currentScroll < 0) {
			this.currentScroll = 0;
		} else if (this.currentScroll >= this.selectButtons.length) {
			this.currentScroll = this.selectButtons.length - 1;
		}

		this.resetButtonPos();
	}

	/**
	 * ボタンの位置更新
	 */
	private void resetButtonPos() {
		int i0 = (this.height / 2) - 16;

		for (int i = 0; i < this.selectButtons.length; ++i) {
			this.selectButtons[i].yPosition = i0 + 32 * (i - this.currentScroll);
		}
	}

	public static void renderModel(IModelSetClient par1, Minecraft par2) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		//ScaledResolution sr = new ScaledResolution(par2, par2.displayWidth, par2.displayHeight);
		//GL11.glViewport(0, 0, sr.getScaledWidth(), sr.getScaledHeight());//いらない
		Project.gluPerspective(80.0F, 1.0F, 5.0F, 1000.0F);//視野角,アスペクト比,奥行きのmin, max
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		par1.renderModelInGui(par2);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.disableStandardItemLighting();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glViewport(0, 0, par2.displayWidth, par2.displayHeight);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		RenderHelper.disableStandardItemLighting();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}