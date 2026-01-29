package jp.ngt.rtm.gui;

import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiTextFieldCustom;
import jp.ngt.ngtlib.network.PacketNBT;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiChangeOffset extends GuiScreenCustom {
    private TileEntityPlaceable tileEntity;
    private GuiTextFieldCustom fieldOffsetX;
    private GuiTextFieldCustom fieldOffsetY;
    private GuiTextFieldCustom fieldOffsetZ;
    private GuiTextFieldCustom fieldRotationRoll;
    private GuiTextFieldCustom fieldRotationPitch;
    private GuiTextFieldCustom fieldRotationYaw;
    private GuiTextFieldCustom fieldScale;

    public GuiChangeOffset(TileEntityPlaceable tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 105, this.height - 28, 100, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 100, 20, I18n.format("gui.cancel")));

        this.fieldOffsetX = this.setNumberField(this.width - 70, 20, 60, 20, String.valueOf(this.tileEntity.getOffsetX()), true);
        this.fieldOffsetY = this.setNumberField(this.width - 70, 50, 60, 20, String.valueOf(this.tileEntity.getOffsetY()), true);
        this.fieldOffsetZ = this.setNumberField(this.width - 70, 80, 60, 20, String.valueOf(this.tileEntity.getOffsetZ()), true);

        this.fieldRotationRoll = this.setNumberField(this.width - 70, 110, 60, 20, String.valueOf(this.tileEntity.getRotationRoll()), true);
        this.fieldRotationPitch = this.setNumberField(this.width - 70, 140, 60, 20, String.valueOf(this.tileEntity.getRotationPitch()), true);
        this.fieldRotationYaw = this.setNumberField(this.width - 70, 170, 60, 20, String.valueOf(this.tileEntity.getRotationYaw()), true);

        this.fieldScale = this.setNumberField(this.width - 70, 200, 60, 20, String.valueOf(this.tileEntity.getScale()), true);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);

        this.drawCenteredString(this.fontRendererObj, "Offset X", this.width - 70, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Offset Y", this.width - 70, 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Offset Z", this.width - 70, 70, 0xFFFFFF);

        this.drawCenteredString(this.fontRendererObj, "Rotation Roll", this.width - 70, 100, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Rotation Pitch", this.width - 70, 130, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Rotation Yaw", this.width - 70, 160, 0xFFFFFF);

        this.drawCenteredString(this.fontRendererObj, "Scale", this.width - 70, 190, 0xFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
            this.sendPacket();
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        }

        super.actionPerformed(button);
    }

    private void updateValues() {
        float offsetX = this.getFieldValue(this.fieldOffsetX, this.tileEntity.getOffsetX());
        float offsetY = this.getFieldValue(this.fieldOffsetY, this.tileEntity.getOffsetY());
        float offsetZ = this.getFieldValue(this.fieldOffsetZ, this.tileEntity.getOffsetZ());
        float roll = this.getFieldValue(this.fieldRotationRoll, this.tileEntity.getRotationRoll());
        float pitch = this.getFieldValue(this.fieldRotationPitch, this.tileEntity.getRotationPitch());
        float yaw = this.getFieldValue(this.fieldRotationYaw, this.tileEntity.getRotationYaw());
        float scale = this.getFieldValue(this.fieldScale, this.tileEntity.getScale());

        // Validate scale (0.01 to 10.0)
        if (scale < 0.01F) scale = 0.01F;
        if (scale > 10.0F) scale = 10.0F;

        this.tileEntity.setOffset(offsetX, offsetY, offsetZ, false);
        this.tileEntity.setRotationRoll(roll, false);
        this.tileEntity.setRotationPitch(pitch, false);
        this.tileEntity.setRotationYaw(yaw, false);
        this.tileEntity.setScale(scale, false);
    }

    private void sendPacket() {
        this.updateValues();
        PacketNBT.sendToServer(this.tileEntity);
    }
}
