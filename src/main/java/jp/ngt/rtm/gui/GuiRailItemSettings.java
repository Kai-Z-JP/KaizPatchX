package jp.ngt.rtm.gui;

import cpw.mods.fml.client.config.GuiCheckBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiContainerCustom;
import jp.ngt.ngtlib.gui.GuiNumberField;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketRailItemSettings;
import jp.ngt.rtm.network.PacketRailItemSettingsSlot;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiRailItemSettings extends GuiContainerCustom {
    private final ContainerRailItemSettings container;
    private static final int EXISTING_SLOT_X = 56;
    private static final int EXISTING_SLOT_Y = 35;
    private static final int BALLAST_SLOT_X = 104;
    private static final int BALLAST_SLOT_Y = 35;

    private GuiNumberField heightField;
    private GuiCheckBox noBallastButton;
    private boolean noBallast;

    public GuiRailItemSettings(InventoryPlayer inventory) {
        super(new ContainerRailItemSettings(inventory));
        this.drawTextBox = false;
        this.container = (ContainerRailItemSettings) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 202;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.textFields.clear();

        RailProperty prop = this.container.getCurrentProperty();
        this.heightField = this.setNumberField(this.guiLeft + 128, this.guiTop + 58, 38, 20, String.valueOf(prop.blockHeight), true);
        this.buttonList.add(new GuiButton(0, this.guiLeft + 8, this.guiTop + 84, 78, 20, "Apply"));
        this.buttonList.add(new GuiButton(1, this.guiLeft + 90, this.guiTop + 84, 78, 20, "Cancel"));
        this.noBallastButton = new GuiCheckBox(2, this.guiLeft + 8, this.guiTop + 62, "No Ballast", this.noBallast);
        this.buttonList.add(this.noBallastButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            RTMCore.NETWORK_WRAPPER.sendToServer(new PacketRailItemSettings(this.getHeightValue(), this.noBallast));
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 2) {
            this.noBallast = this.noBallastButton.isChecked();
            if (this.noBallast && this.container.getBallastStack() != null) {
                this.container.clearBallastStackClient();
                RTMCore.NETWORK_WRAPPER.sendToServer(new PacketRailItemSettingsSlot());
            }
        }
    }

    private float getHeightValue() {
        try {
            return Float.parseFloat(this.heightField.getText());
        } catch (NumberFormatException e) {
            return this.container.getCurrentProperty().blockHeight;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN) {
            RTMCore.NETWORK_WRAPPER.sendToServer(new PacketRailItemSettings(this.getHeightValue(), this.noBallast));
            this.mc.displayGuiScreen(null);
        } else {
            super.keyTyped(keyChar, keyCode);
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, int clickType) {
        super.handleMouseClick(slot, slotId, mouseButton, clickType);
        if (this.noBallast && this.container.getBallastStack() != null) {
            this.setNoBallast(false);
        }
    }

    private void setNoBallast(boolean value) {
        this.noBallast = value;
        if (this.noBallastButton != null) {
            this.noBallastButton.setIsChecked(value);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawPanel();
        this.drawSlotFrame(this.guiLeft + EXISTING_SLOT_X - 1, this.guiTop + EXISTING_SLOT_Y - 1);
        for (Object obj : this.inventorySlots.inventorySlots) {
            Slot slot = (Slot) obj;
            this.drawSlotFrame(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1);
        }
        if (this.heightField != null) {
            this.heightField.drawTextBox();
        }
    }

    private void drawPanel() {
        drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFFC6C6C6);
        drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + 1, 0xFFFFFFFF);
        drawRect(this.guiLeft, this.guiTop, this.guiLeft + 1, this.guiTop + this.ySize, 0xFFFFFFFF);
        drawRect(this.guiLeft + this.xSize - 1, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFF555555);
        drawRect(this.guiLeft, this.guiTop + this.ySize - 1, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFF555555);
        drawRect(this.guiLeft + 3, this.guiTop + 3, this.guiLeft + this.xSize - 3, this.guiTop + this.ySize - 3, 0xFFB8B8B8);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RailProperty prop = this.container.getCurrentProperty();
        this.fontRendererObj.drawString("Rail Settings", 8, 6, 4210752);
        this.fontRendererObj.drawString("Model: " + prop.railModel, 8, 18, 4210752);
        this.fontRendererObj.drawString("->", 82, 40, 4210752);
        this.fontRendererObj.drawString("Height", 94, 64, 4210752);
        this.fontRendererObj.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);

        ItemStack preview = this.getPreviewStack(prop);
        if (preview != null) {
            this.drawItem(preview, EXISTING_SLOT_X, EXISTING_SLOT_Y);
            if (!this.noBallast && this.container.getBallastStack() == null) {
                this.drawGhostItem(preview, BALLAST_SLOT_X, BALLAST_SLOT_Y);
            }
        }
    }

    private void drawSlotFrame(int x, int y) {
        drawRect(x, y, x + 18, y + 18, 0xFF373737);
        drawRect(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    private ItemStack getPreviewStack(RailProperty current) {
        if (current.block == Blocks.air) {
            return null;
        }

        Item item = Item.getItemFromBlock(current.block);
        return item == null ? null : new ItemStack(item, 1, current.blockMetadata);
    }

    private void drawItem(ItemStack stack, int x, int y) {
        RenderItem renderer = NGTRenderHelper.getItemRenderer();
        float zLevel = renderer.zLevel;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            renderer.renderItemIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
        } finally {
            renderer.zLevel = zLevel;
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void drawGhostItem(ItemStack stack, int x, int y) {
        this.drawItem(stack, x, y);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawRect(x, y, x + 16, y + 16, 0xCCB8B8B8);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
