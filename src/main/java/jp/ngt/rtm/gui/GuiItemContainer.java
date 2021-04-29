package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiItemContainer extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private final IInventory inventoryPlayer;
    private final IInventory inventory;
    private final int inventoryRows;
    private float currentScroll;

    public GuiItemContainer(InventoryPlayer par1InventoryPlayer, IInventory par2IInventory) {
        super(new ContainerItemContainer(par1InventoryPlayer, par2IInventory));
        this.inventoryPlayer = par1InventoryPlayer;
        this.inventory = par2IInventory;
        this.allowUserInput = false;
        short short1 = 222;
        int i = short1 - 108;
        this.inventoryRows = par2IInventory.getSizeInventory() / 9;
        this.ySize = i + this.inventoryRows * 18;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        this.fontRendererObj.drawString(this.inventory.hasCustomInventoryName() ? this.inventory.getInventoryName() : I18n.format(this.inventory.getInventoryName()), 8, 6, 4210752);
        this.fontRendererObj.drawString(this.inventoryPlayer.hasCustomInventoryName() ? this.inventoryPlayer.getInventoryName() : I18n.format(this.inventoryPlayer.getInventoryName()), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}