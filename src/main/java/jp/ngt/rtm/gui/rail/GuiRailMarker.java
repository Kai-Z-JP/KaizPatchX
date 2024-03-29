package jp.ngt.rtm.gui.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiTextFieldCustom;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketMarkerRPClient;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiRailMarker extends GuiScreenCustom {
    public final TileEntityMarker marker;
    private RailPosition currentRP;

    private GuiButton buttonViewMode;
    private GuiTextFieldCustom fieldAncYaw;
    private GuiTextFieldCustom fieldAncPitch;
    private GuiTextFieldCustom fieldAncH;
    private GuiTextFieldCustom fieldAncV;
    private GuiTextFieldCustom fieldCantCenter;
    private GuiTextFieldCustom fieldCantEdge;
    private GuiTextFieldCustom fieldCantRandom;

    public GuiRailMarker(TileEntityMarker par1) {
        this.marker = par1;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.currentRP = this.marker.getMarkerRP();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 105, this.height - 28, 100, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 100, 20, I18n.format("gui.cancel")));

        //this.buttonViewMode = new GuiButton(100, 30, 20, 40, 20, "Horizontal");
        //this.buttonList.add(this.buttonViewMode);

        this.fieldAncYaw = this.setNumberField(this.width - 70, 20, 60, 20, String.valueOf(this.currentRP.anchorYaw), true);
        this.fieldAncPitch = this.setNumberField(this.width - 70, 50, 60, 20, String.valueOf(this.currentRP.anchorPitch), true);
        this.fieldAncH = this.setNumberField(this.width - 70, 80, 60, 20, String.valueOf(this.currentRP.anchorLengthHorizontal), true);
        this.fieldAncV = this.setNumberField(this.width - 70, 110, 60, 20, String.valueOf(this.currentRP.anchorLengthVertical), true);
        this.fieldCantCenter = this.setNumberField(this.width - 70, 140, 60, 20, String.valueOf(this.currentRP.cantCenter), true);
        this.fieldCantEdge = this.setNumberField(this.width - 70, 170, 60, 20, String.valueOf(this.currentRP.cantEdge), true);
        this.fieldCantRandom = this.setNumberField(this.width - 70, 200, 60, 20, String.valueOf(this.currentRP.cantRandom), true);

        if (this.marker.getBlockType() == RTMBlock.markerSwitch) {
            this.fieldAncPitch.setEnabled(false);
            this.fieldAncV.setEnabled(false);
            this.fieldCantCenter.setEnabled(false);
            this.fieldCantEdge.setEnabled(false);
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);

        this.renderLineAndAnchor();
        this.drawCenteredString(this.fontRendererObj, "Anchor Yaw", this.width - 70, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Anchor Pitch", this.width - 70, 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Anchor Length H", this.width - 70, 70, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Anchor Length V", this.width - 70, 100, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Cant Center", this.width - 70, 130, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Cant Edge", this.width - 70, 160, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Cant Random", this.width - 70, 190, 0xFFFFFF);
    }

    private void renderLineAndAnchor() {
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
        this.currentRP.anchorYaw = this.getFieldValue(this.fieldAncYaw, this.currentRP.anchorYaw);
        this.currentRP.anchorPitch = this.getFieldValue(this.fieldAncPitch, this.currentRP.anchorPitch);
        this.currentRP.anchorLengthHorizontal = this.getFieldValue(this.fieldAncH, this.currentRP.anchorLengthHorizontal);
        this.currentRP.anchorLengthVertical = this.getFieldValue(this.fieldAncV, this.currentRP.anchorLengthVertical);
        this.currentRP.cantCenter = this.getFieldValue(this.fieldCantCenter, this.currentRP.cantCenter);
        this.currentRP.cantEdge = this.getFieldValue(this.fieldCantEdge, this.currentRP.cantEdge);
        this.currentRP.cantRandom = this.getFieldValue(this.fieldCantRandom, this.currentRP.cantRandom);
    }

    private void sendPacket() {
        this.updateValues();
        RTMCore.NETWORK_WRAPPER.sendToServer(new PacketMarkerRPClient(this.marker));
    }
}