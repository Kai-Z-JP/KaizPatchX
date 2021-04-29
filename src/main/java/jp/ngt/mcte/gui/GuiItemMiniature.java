package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.block.MiniatureBlockState;
import jp.ngt.mcte.item.ItemMiniature;
import jp.ngt.mcte.item.ItemMiniature.MiniatureMode;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatAllowedCharacters;

import java.io.File;

@SideOnly(Side.CLIENT)
public class GuiItemMiniature extends GuiScreenCustom {
    private final EntityPlayer player;
    private final ItemStack miniatureItem;

    private GuiButton buttonDone;
    private GuiButton buttonMode;
    private GuiButton enableAABB;
    private GuiTextField fieldScale;
    private GuiTextField fieldOffsetX, fieldOffsetY, fieldOffsetZ;
    private GuiTextField fieldName;
    private GuiTextField fieldLightValue, fieldAABB;
    private boolean enabledFieldAABB;
    private float scale;
    private float offsetX, offsetY, offsetZ;
    private final MiniatureBlockState state;
    private NGTObject ngto;
    private MiniatureMode mode;

    public GuiItemMiniature(EntityPlayer par1) {
        this.player = par1;
        this.miniatureItem = par1.inventory.getCurrentItem();
        if (this.miniatureItem.hasTagCompound()) {
            NBTTagCompound nbt = this.miniatureItem.getTagCompound();
            float[] fa = ItemMiniature.getOffset(nbt);
            this.offsetX = fa[0];
            this.offsetY = fa[1];
            this.offsetZ = fa[2];
            this.scale = ItemMiniature.getScale(nbt);
            this.ngto = ItemMiniature.getNGTObject(nbt);
            this.mode = ItemMiniature.getMode(nbt);
            this.state = ItemMiniature.getMiniatureBlockState(nbt);
        } else {
            this.scale = 1.0F;
            this.mode = MiniatureMode.miniature;
            this.state = new MiniatureBlockState();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int hw = this.width / 2;

        this.buttonList.clear();
        this.buttonDone = new GuiButton(0, hw - 155, this.height - 28, 150, 20, I18n.format("gui.done"));
        this.buttonDone.enabled = (this.ngto != null);
        this.buttonList.add(this.buttonDone);
        this.buttonList.add(new GuiButton(1, hw + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        int h = 40;
        this.buttonList.add(new GuiButton(100, hw - 120, h, 80, 20, I18n.format("gui.mcte.miniature.select")));
        this.buttonList.add(new GuiButton(101, hw - 120, h + 20, 80, 20, I18n.format("gui.mcte.miniature.export")));
        this.buttonMode = new GuiButton(102, hw - 120, h + 40, 80, 20, I18n.format("gui.mcte.miniature." + this.mode.toString()));
        this.buttonMode.enabled = (this.ngto != null && this.ngto.xSize == this.ngto.ySize && this.ngto.xSize == this.ngto.zSize);
        this.buttonList.add(this.buttonMode);
        this.enableAABB = new GuiButton(103, hw - 182, h + 120, 20, 20, "");
        this.buttonList.add(this.enableAABB);
        int i2 = 40;
        this.fieldScale = this.setTextField(hw + i2, h, 80, 20, String.valueOf(this.scale));
        this.fieldOffsetX = this.setTextField(hw + i2, h + 20, 80, 20, String.valueOf(this.offsetX));
        this.fieldOffsetY = this.setTextField(hw + i2, h + 40, 80, 20, String.valueOf(this.offsetY));
        this.fieldOffsetZ = this.setTextField(hw + i2, h + 60, 80, 20, String.valueOf(this.offsetZ));
        this.fieldLightValue = this.setTextField(hw + i2, h + 80, 80, 20, String.valueOf(this.state.lightValue));

        String name = this.miniatureItem.getDisplayName();
        this.fieldName = this.setTextField(hw - 120, h + 70, 100, 20, name);

        this.enabledFieldAABB = this.state.hasCustomAABB();
        this.fieldAABB = this.setTextField(hw - 160, h + 120, 340, 20, this.state.getAabbAsJson());
        this.fieldAABB.setEnabled(this.enabledFieldAABB);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.sendPacket();
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 100) {
            this.selectFile();
        } else if (button.id == 101) {
            this.exportFile();
        } else if (button.id == 102) {
            MiniatureMode[] array = MiniatureMode.values();
            int i = this.mode.id;
            ++i;
            if (i >= array.length) {
                i = 0;
            }
            this.mode = array[i];
            this.buttonMode.displayString = I18n.format("gui.mcte.miniature." + this.mode.toString());
        } else if (button.id == 103) {
            this.enabledFieldAABB ^= true;
            this.fieldAABB.setEnabled(this.enabledFieldAABB);
            this.state.setCustomAABB(this.enabledFieldAABB);
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);

        try {
            this.scale = this.getFloat(this.fieldScale.getText());
            this.offsetX = this.getFloat(this.fieldOffsetX.getText());
            this.offsetY = this.getFloat(this.fieldOffsetY.getText());
            this.offsetZ = this.getFloat(this.fieldOffsetZ.getText());
            this.state.lightValue = this.getByte(this.fieldLightValue.getText());
        } catch (NumberFormatException ignored) {
        }
    }

    private float getFloat(String s) {
        float f = Float.parseFloat(s);
        if (Float.isNaN(f)) {
            throw new NumberFormatException();
        }
        return f;
    }

    private byte getByte(String s) {
        byte b = Byte.parseByte(s);
        if (b < 0 || b > 15) {
            throw new NumberFormatException();
        }
        return b;
    }

    private void selectFile() {
        File file = NGTFileLoader.selectFile(new String[][]{{"NGTObject_File", "ngto"}});
        if (file != null) {
            this.ngto = NGTObject.importFromFile(file);
            this.buttonDone.enabled = true;
        }
    }

    private void exportFile() {
        if (this.ngto == null) {
            return;
        }

        File file = NGTFileLoader.saveFile(new String[]{"NGTObject_File", "ngto"});
        if (file != null) {
            this.ngto.exportToFile(file);
        }
    }

    private void sendPacket() {
        if (this.ngto != null) {
            this.state.setAABB(this.fieldAABB.getText());
            ItemStack stack = ItemMiniature.createMiniatureItem(this.ngto, this.scale, this.offsetX, this.offsetY, this.offsetZ, this.mode, this.state);
            String s = ChatAllowedCharacters.filerAllowedCharacters(this.fieldName.getText());
            if (s.length() <= 30) {
                stack.setStackDisplayName(s);
            }
            NGTUtil.sendPacketToServer(this.player, stack);
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        super.drawScreen(par1, par2, par3);

        int hw = this.width / 2;
        int i2 = hw + 5;
        int i3 = 45;
        this.drawString(this.fontRendererObj, "Scale", i2, i3, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "OffsetX", i2, i3 + 20, 0xFF0000);
        this.drawString(this.fontRendererObj, "OffsetY", i2, i3 + 40, 0x00FF00);
        this.drawString(this.fontRendererObj, "OffsetZ", i2, i3 + 60, 0x0000FF);
        this.drawString(this.fontRendererObj, "Brightness", hw, i3 + 80, 0xFFFFFF);
    }
}