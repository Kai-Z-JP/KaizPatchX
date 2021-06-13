package jp.ngt.rtm.gui;

import jp.kaiz.kaizpatch.util.KeyboardUtil;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiTextFieldCustom;
import jp.ngt.ngtlib.network.PacketNBT;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class GuiChangeOffset extends GuiScreenCustom {
    private TileEntityPlaceable tileEntity;
    private GuiTextFieldCustom fieldOffsetX;
    private GuiTextFieldCustom fieldOffsetY;
    private GuiTextFieldCustom fieldOffsetZ;
    private GuiTextFieldCustom fieldRotationYaw;

    public GuiChangeOffset(TileEntityPlaceable tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 105, this.height - 28, 100, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 100, 20, I18n.format("gui.cancel")));

        this.fieldOffsetX = this.setTextField(this.width - 70, 20, 60, 20, String.valueOf(this.tileEntity.getOffsetX()));
        this.fieldOffsetY = this.setTextField(this.width - 70, 50, 60, 20, String.valueOf(this.tileEntity.getOffsetY()));
        this.fieldOffsetZ = this.setTextField(this.width - 70, 80, 60, 20, String.valueOf(this.tileEntity.getOffsetZ()));
        this.fieldRotationYaw = this.setTextField(this.width - 70, 110, 60, 20, String.valueOf(this.tileEntity.getRotation()));
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);

        this.drawCenteredString(this.fontRendererObj, "Offset X", this.width - 70, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Offset Y", this.width - 70, 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Offset Z", this.width - 70, 70, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Rotation Yaw", this.width - 70, 100, 0xFFFFFF);
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

    @Override
    protected void keyTyped(char par1, int par2) {
        if (par2 == Keyboard.KEY_ESCAPE || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.thePlayer.closeScreen();
        }

        if (this.currentTextField != null) {
            if (KeyboardUtil.isDecimalNumberKey(par2)) {
                this.currentTextField.textboxKeyTyped(par1, par2);
            }
        }

        if (par2 == Keyboard.KEY_RETURN) {
            //this.updateValues();//GUI閉じるときのみRPの値を更新
        }
    }

    private void updateValues() {
        float offsetX = this.getFieldValue(this.fieldOffsetX, this.tileEntity.getOffsetX());
        float offsetY = this.getFieldValue(this.fieldOffsetY, this.tileEntity.getOffsetY());
        float offsetZ = this.getFieldValue(this.fieldOffsetZ, this.tileEntity.getOffsetZ());
        float rotation = this.getFieldValue(this.fieldRotationYaw, this.tileEntity.getRotation());
        this.tileEntity.setOffset(offsetX, offsetY, offsetZ, false);
        this.tileEntity.setRotation(rotation, false);
    }

    private void sendPacket() {
        this.updateValues();
        PacketNBT.sendToServer(this.tileEntity);
    }
}
