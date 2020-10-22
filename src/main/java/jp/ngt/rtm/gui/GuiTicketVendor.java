package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.electric.TileEntityTicketVendor;
import jp.ngt.rtm.gui.vendor.VendorScreen;
import jp.ngt.rtm.gui.vendor.VendorScreenSelectTicket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiTicketVendor extends GuiContainer {
	private static final ResourceLocation tex_main = new ResourceLocation("rtm", "textures/gui/ticketVendor.png");

	private final TileEntityTicketVendor vendor;
	private VendorScreen vendorScreen;

	public GuiTicketVendor(InventoryPlayer inventory, TileEntityTicketVendor par2) {
		super(new ContainerTicketVendor(inventory, par2));
		this.vendor = par2;
		this.vendorScreen = new VendorScreenSelectTicket(this);

		this.xSize = 256;
		this.ySize = 228;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.vendorScreen.init(this.guiLeft, this.guiTop);


	}

	public void addButton(GuiButton button) {
		this.buttonList.add(button);
	}

	public void setVendorScreen(VendorScreen par1) {
		this.vendorScreen = par1;
		this.buttonList.clear();
		this.vendorScreen.init(this.guiLeft, this.guiTop);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		this.vendorScreen.onClickButton(button);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(tex_main);

		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
}