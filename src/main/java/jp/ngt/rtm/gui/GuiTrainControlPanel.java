package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.npc.macro.MacroRecorder;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import jp.ngt.rtm.modelpack.state.DataMap;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.*;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiTrainControlPanel extends InventoryEffectRenderer {
    private static final ResourceLocation tabTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    private int selectedTabIndex = TabTrainControlPanel.TAB_Inventory.getTabIndex();
    /**
     * Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
     */
    private float currentScroll;
    /**
     * True if the scrollbar is being dragged
     */
    private boolean isScrolling;
    private boolean wasClicking;
    private List slotsList;
    private Slot slot;
    /**
     * 開いてるタブを保持
     */
    private static int tabPage = 0;
    private int maxPages = 0;
    private final Map<Integer, TrainControlPanelPage> pages = new HashMap<>();

    protected final EntityTrainBase train;
    protected final EntityPlayer player;
    protected final ModelSetVehicleBase<TrainConfig> modelset;

    /**
     * 0:R, L:1
     */
    private final GuiButtonDoor[] buttonDoor = new GuiButtonDoor[2];

    public GuiTrainControlPanel(ContainerTrainControlPanel par1) {
        super(par1);
        this.train = par1.train;
        this.player = par1.player;
        this.modelset = par1.train.getModelSet();
        this.player.openContainer = this.inventorySlots;
        this.allowUserInput = true;
        this.pages.put(TabTrainControlPanel.TAB_Setting.getTabIndex(), new TrainControlPanelSettingPage(this));
        this.pages.put(TabTrainControlPanel.TAB_Function.getTabIndex(), new TrainControlPanelFunctionPage(this));
        this.pages.put(TabTrainControlPanel.TAB_Protection.getTabIndex(), new TrainControlPanelProtectionPage(this));
        this.pages.put(TabTrainControlPanel.TAB_Formation.getTabIndex(), new TrainControlPanelFormationPage(this));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void drawHoveringText(List textLines, int x, int y, FontRenderer font) {
        super.drawHoveringText(textLines, x, y, font);
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();
        this.setCurrentTab(TabTrainControlPanel.tabArray[this.selectedTabIndex]);//i
        int tabCount = TabTrainControlPanel.tabArray.length;
        if (tabCount > 12) {
            this.buttonList.add(new GuiButton(101, this.guiLeft, this.guiTop - 50, 20, 20, "<"));
            this.buttonList.add(new GuiButton(102, this.guiLeft + this.xSize - 20, this.guiTop - 50, 20, 20, ">"));
            this.maxPages = ((tabCount - 12) / 10) + 1;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    private void setCurrentTab(TabTrainControlPanel tab) {
        if (tab == null) {
            return;
        }

        this.selectedTabIndex = tab.getTabIndex();
        ContainerTrainControlPanel containerTrain = (ContainerTrainControlPanel) this.inventorySlots;
        this.field_147008_s.clear();

        if (this.slotsList == null) {
            this.slotsList = containerTrain.inventorySlots;//ぬるぽ回避
        }

        if (tab == TabTrainControlPanel.TAB_Inventory) {
            containerTrain.inventorySlots = this.slotsList;
            this.buttonList.clear();
        } else {
            this.initInventorySlot(containerTrain);
            this.buttonList.clear();
            TrainControlPanelPage page = this.getActivePage();
            if (page != null) {
                page.init();
            }
        }

        this.buttonDoor[0] = new GuiButtonDoor(300, this.guiLeft + this.xSize + 20, this.guiTop + 20, 64, 80);
        this.buttonDoor[1] = new GuiButtonDoor(301, this.guiLeft - 84, this.guiTop + 20, 64, 80);
        int state = this.train.getTrainStateData(TrainStateType.State_Door.id);
        boolean r = (state & 1) == 1;
        boolean l = (state & 2) == 2;
        int trainDir = 0;
        int cabDir = this.train.getCabDirection();
        int xor = trainDir ^ cabDir;
        boolean dir = (xor & 1) == 0;
        this.buttonDoor[0].opened = dir ? l : r;
        this.buttonDoor[1].opened = dir ? r : l;
        this.buttonList.add(this.buttonDoor[0]);
        this.buttonList.add(this.buttonDoor[1]);

        this.currentScroll = 0.0F;
        this.sendTabPacket(this.selectedTabIndex);
    }

    private void initInventorySlot(Container containerTrain) {
        containerTrain.inventorySlots = new ArrayList<>();
        IntStream.range(0, 9)
                .mapToObj(i -> new Slot(this.player.inventory, i, 8 + i * 18, 142))
                .forEach(slot -> {
                    slot.slotNumber = containerTrain.inventorySlots.size();
                    containerTrain.inventorySlots.add(slot);
                });
    }

    private void sendTabPacket(int tabIndex) {
        String s = "setTrainTab," + tabIndex;
        RTMCore.NETWORK_WRAPPER.sendToServer(new PacketNotice(PacketNotice.Side_SERVER, s, this.player));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        TabTrainControlPanel tab = TabTrainControlPanel.tabArray[this.selectedTabIndex];
        if (tab != null) {
            GL11.glDisable(GL11.GL_BLEND);
            //インベントリ名表示
            //this.fontRendererObj.drawString(I18n.format(tab.getTranslatedTabLabel(), new Object[0]), 8, 6, 4210752);

            /*if(tab == TabTrainControlPanel.tabTrain)
            {
            	this.fontRendererObj.drawString(this.getFormattedText(8, this.train.getTrainStateData(8)), 8, 18, 4210752);
            	this.fontRendererObj.drawString(this.getFormattedText(9, this.train.getTrainStateData(9)), 8, 42, 4210752);
            }*/
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        if (par3 == 0) {
            int l = par1 - this.guiLeft;
            int i1 = par2 - this.guiTop;
            TabTrainControlPanel[] tabs = TabTrainControlPanel.tabArray;
            for (TabTrainControlPanel tab : tabs) {
                if (tab != null && this.func_147049_a(tab, l, i1)) {
                    return;
                }
            }

            TrainControlPanelPage page = this.getActivePage();
            if (page != null && page.mouseClicked(par1, par2, par3)) {
                return;
            }
        } else if (par3 == 1) {
            for (Object o : this.buttonList) {
                GuiButton guibutton = (GuiButton) o;
                if (guibutton.mousePressed(this.mc, par1, par2)) {
                    //this.selectedButton = guibutton;
                    guibutton.func_146113_a(this.mc.getSoundHandler());
                }
            }
            return;
        }

        super.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void mouseMovedOrUp(int par1, int par2, int par3) {
        if (par3 == 0) {
            int l = par1 - this.guiLeft;
            int i1 = par2 - this.guiTop;
            TabTrainControlPanel[] tabs = TabTrainControlPanel.tabArray;
            for (TabTrainControlPanel tab : tabs) {
                if (tab != null && this.func_147049_a(tab, l, i1)) {
                    this.setCurrentTab(tab);
                    return;
                }
            }
        }

        super.mouseMovedOrUp(par1, par2, par3);
    }

    private boolean needsScrollBars() {
        //return selectedTabIndex != TabTrainControlPanel.tabInventory.getTabIndex() && TabTrainControlPanel.tabArray[selectedTabIndex].shouldHidePlayerInventory() && ((GuiTrainControlPanel.ContainerCreative)this.inventorySlots).func_148328_e();
        return false;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0 && this.needsScrollBars()) {
            int j = 0;//((ContainerTrainControlPanel)this.inventorySlots).itemList.size() / 9 - 5 + 1;

            if (i > 0) {
                i = 1;
            } else {
                i = -1;
            }

            this.currentScroll = (float) ((double) this.currentScroll - (double) i / (double) j);

            if (this.currentScroll < 0.0F) {
                this.currentScroll = 0.0F;
            } else if (this.currentScroll > 1.0F) {
                this.currentScroll = 1.0F;
            }
            //((ContainerTrainControlPanel)this.inventorySlots).scrollTo(this.currentScroll);
        }
        if (i != 0) {
            TrainControlPanelPage page = this.getActivePage();
            if (page != null && page.handleMouseWheel(i)) {
                return;
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        boolean flag = Mouse.isButtonDown(0);
        int i1 = this.guiLeft + 175;
        int j1 = this.guiTop + 18;
        int k1 = i1 + 14;
        int l1 = j1 + 112;

        if (!this.wasClicking && flag && par1 >= i1 && par2 >= j1 && par1 < k1 && par2 < l1) {
            this.isScrolling = this.needsScrollBars();
        }

        if (!flag) {
            this.isScrolling = false;
        }

        this.wasClicking = flag;

        if (this.isScrolling) {
            this.currentScroll = ((float) (par2 - j1) - 7.5F) / ((float) (l1 - j1) - 15.0F);

            if (this.currentScroll < 0.0F) {
                this.currentScroll = 0.0F;
            } else if (this.currentScroll > 1.0F) {
                this.currentScroll = 1.0F;
            }

            //((ContainerTrainControlPanel)this.inventorySlots).scrollTo(this.currentScroll);
        }

        super.drawScreen(par1, par2, par3);
        TrainControlPanelPage activePage = this.getActivePage();
        if (activePage != null) {
            activePage.drawScreen(par1, par2, par3);
        }

        TabTrainControlPanel[] tabs = TabTrainControlPanel.tabArray;
        int start = tabPage * 10;
        int i2 = Math.min(tabs.length, ((tabPage + 1) * 10) + 2);
        if (tabPage != 0) {
            start += 2;
        }
        boolean rendered = Arrays.stream(tabs, start, i2)
                .filter(Objects::nonNull)
                .anyMatch(TabTrainControlPanel -> this.renderCreativeInventoryHoveringText(TabTrainControlPanel, par1, par2));

        if (!rendered)// && renderCreativeInventoryHoveringText(TabTrainControlPanel.tabAllSearch, par1, par2))
        {
            this.renderCreativeInventoryHoveringText(TabTrainControlPanel.TAB_Inventory, par1, par2);
        }

        if (this.slot != null && this.selectedTabIndex == TabTrainControlPanel.TAB_Inventory.getTabIndex() && this.func_146978_c(this.slot.xDisplayPosition, this.slot.yDisplayPosition, 16, 16, par1, par2)) {
            this.drawCreativeTabHoveringText(I18n.format("inventory.binSlot"), par1, par2);
        }

        if (this.maxPages != 0) {
            String page = String.format("%d / %d", tabPage + 1, maxPages + 1);
            int width = fontRendererObj.getStringWidth(page);
            GL11.glDisable(GL11.GL_LIGHTING);
            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            fontRendererObj.drawString(page, guiLeft + (xSize / 2) - (width / 2), guiTop - 44, -1);
            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);
        TrainControlPanelPage page = this.getActivePage();
        if (page != null) {
            page.keyTyped(par1, par2);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        TabTrainControlPanel tab = TabTrainControlPanel.tabArray[this.selectedTabIndex];
        TabTrainControlPanel[] tabs = TabTrainControlPanel.tabArray;

        int start = tabPage * 10;
        int k = Math.min(tabs.length, ((tabPage + 1) * 10 + 2));
        if (tabPage != 0) start += 2;

        Arrays.stream(tabs, start, k).forEach(tab1 -> {
            this.mc.getTextureManager().bindTexture(tabTexture);
            if (tab1 == null) {
                return;
            }
            if (tab1.getTabIndex() != this.selectedTabIndex) {
                this.renderTabItem(tab1);
            }
        });

        if (tabPage != 0 && tab != TabTrainControlPanel.TAB_Inventory) {
            this.mc.getTextureManager().bindTexture(tabTexture);
            this.renderTabItem(TabTrainControlPanel.TAB_Inventory);
        }

        this.mc.getTextureManager().bindTexture(tab.getTexture());

        this.drawTexturedModalRect(this.guiLeft - 1, this.guiTop - 1, 0, 0, this.xSize, this.ySize);//ズレ修正x-1,y-1
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //int i1 = this.guiLeft + 175;
        //k = this.guiTop + 18;
        //int l = k + 112;
        this.mc.getTextureManager().bindTexture(tabTexture);

        /*if(TabTrainControlPanel.shouldHidePlayerInventory())
        {
            this.drawTexturedModalRect(i1, k + (int)((float)(l - k - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
        }*/

        if (tab.getTabPage() != tabPage) {
            if (tab != TabTrainControlPanel.TAB_Inventory) {
                return;
            }
        }

        this.renderTabItem(tab);

        if (tab == TabTrainControlPanel.TAB_Inventory) {
            GuiInventory.func_147046_a(this.guiLeft + 51, this.guiTop + 75, 30, (float) (this.guiLeft + 51 - par2), (float) (this.guiTop + 75 - 50 - par3), this.mc.thePlayer);
        } else if (tab == TabTrainControlPanel.TAB_Function) {
//            this.drawGradientRect(this.guiLeft + 7, this.guiTop + 7, this.guiLeft + this.xSize - 7, this.guiTop + this.ySize - 30, -1072689136, -804253680);
        }
    }

    protected boolean func_147049_a(TabTrainControlPanel tab, int x, int y) {
        if (tab.getTabPage() != tabPage) {
            if (tab != TabTrainControlPanel.TAB_Inventory) {
                return false;
            }
        }

        int k = tab.getTabColumn();
        int l = 28 * k;

        if (k == 5) {
            l = this.xSize - 28 + 2;
        } else if (k > 0) {
            l += k;
        }

        int i1 = tab.isTabInFirstRow() ? -32 : this.ySize;

        return x >= l && x <= l + 28 && y >= i1 && y <= i1 + 32;
    }

    /**
     * Renders the creative inventory hovering text if mouse is over it. Returns true if did render or false otherwise.
     * Params: current creative tab to be checked, current mouse x position, current mouse y position.
     */
    protected boolean renderCreativeInventoryHoveringText(TabTrainControlPanel tab, int par2, int par3) {
        int k = tab.getTabColumn();
        int l = 28 * k;

        if (k == 5) {
            l = this.xSize - 28 + 2;
        } else if (k > 0) {
            l += k;
        }

        int i1 = tab.isTabInFirstRow() ? -32 : this.ySize;

        if (this.func_146978_c(l + 3, i1 + 3, 23, 27, par2, par3)) {
            this.drawCreativeTabHoveringText(I18n.format(tab.getTranslatedTabLabel()), par2, par3);
            return true;
        } else {
            return false;
        }
    }

    protected void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float f = (float) (par5 >> 24 & 255) / 255.0F;
        float f1 = (float) (par5 >> 16 & 255) / 255.0F;
        float f2 = (float) (par5 >> 8 & 255) / 255.0F;
        float f3 = (float) (par5 & 255) / 255.0F;
        float f4 = (float) (par6 >> 24 & 255) / 255.0F;
        float f5 = (float) (par6 >> 16 & 255) / 255.0F;
        float f6 = (float) (par6 >> 8 & 255) / 255.0F;
        float f7 = (float) (par6 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex(par3, par2, 0.0D);
        tessellator.addVertex(par1, par2, 0.0D);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex(par1, par4, 0.0D);
        tessellator.addVertex(par3, par4, 0.0D);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    protected void renderTabItem(TabTrainControlPanel tab) {
        boolean flag = tab.getTabIndex() == this.selectedTabIndex;
        boolean flag1 = tab.isTabInFirstRow();
        int i = tab.getTabColumn();
        int j = i * 28;
        int k = 0;
        int l = this.guiLeft + 28 * i;
        int i1 = this.guiTop;
        byte b0 = 32;

        if (flag) {
            k += 32;
        }

        if (i == 5) {
            l = this.guiLeft + this.xSize - 28;
        } else if (i > 0) {
            l += i;
        }

        if (flag1) {
            i1 -= 28;
        } else {
            k += 64;
            i1 += this.ySize - 4;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor3f(1F, 1F, 1F); //Forge: Reset color in case Items change it.
        GL11.glEnable(GL11.GL_BLEND); //Forge: Make sure blend is enabled else tabs show a white border.
        this.drawTexturedModalRect(l, i1, j, k, 28, b0);
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        l += 6;
        i1 += 8 + (flag1 ? 1 : -1);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        ItemStack itemstack = tab.getIconItemStack();
        itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, l, i1);
        itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, l, i1);
        GL11.glDisable(GL11.GL_LIGHTING);
        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
        }

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
        }

        if (button.id == 101) {
            tabPage = Math.max(tabPage - 1, 0);
        }

        if (button.id == 102) {
            tabPage = Math.min(tabPage + 1, maxPages);
        }

        TrainControlPanelPage page = this.getActivePage();
        if (page != null) {
            if (page.actionPerformed(button)) {
                return;
            }
        }

        if (button.id == 300 || button.id == 301) {
            ((GuiButtonDoor) button).opened ^= true;
            int r = (this.buttonDoor[0].opened ? 1 : 0);
            int l = (this.buttonDoor[1].opened ? 1 : 0);
            int trainDir = this.train.getTrainDirection();
            int cabDir = this.train.getCabDirection();
            int xor = trainDir ^ cabDir;
            boolean dir = (xor & 1) == 0;
            int state = dir ? (r << 1 | l) : (l << 1 | r);
            this.train.setTrainStateData(TrainStateType.State_Door.id, (byte) state);
            this.sendTrainState(TrainStateType.State_Door.id, (byte) state);
            TrainState type = TrainState.Door_Close;
            switch (state) {
                case 0:
                    type = TrainState.Door_Close;
                    break;
                case 1:
                    type = TrainState.Door_OpenRight;
                    break;
                case 2:
                    type = TrainState.Door_OpenLeft;
                    break;
                case 3:
                    type = TrainState.Door_OpenAll;
                    break;
            }
            MacroRecorder.INSTANCE.recDoor(this.train.worldObj, type);
        }
    }

    public void sendTrainState(int id, byte data) {
        this.train.syncTrainStateData(id, data);
    }

    protected String getFormattedText(int par1, byte par2) {
        TrainStateType stateType = TrainState.getStateType(par1);
        if (stateType == TrainStateType.State_ChunkLoader) {
            String s = "state." + stateType.stateName;
            return I18n.format(s, new Object[0]) + par2;
        } else if (stateType == TrainStateType.State_Destination) {
            if (par2 >= this.modelset.getConfig().rollsignNames.length) {
                par2 = (byte) (this.modelset.getConfig().rollsignNames.length - 1);
            }
            String s = "state." + stateType.stateName;
            return I18n.format(s) + " " + this.modelset.getConfig().rollsignNames[par2];
        } else if (stateType == TrainStateType.State_Announcement) {
            String s = "state." + stateType.stateName;
            String[][] sa = this.modelset.getConfig().sound_Announcement;
            if (sa != null && par2 < sa.length) {
                return I18n.format(s) + " " + sa[par2][0];
            }
            return I18n.format(s) + " null";
        } else {
            String s = "state." + stateType.stateName + "." + TrainState.getState(par1, par2).stateName;
            return I18n.format(s);
        }
    }

    public EntityTrainBase getPanelTrain() {
        return this.train;
    }

    public EntityPlayer getPanelPlayer() {
        return this.player;
    }

    public ModelSetVehicleBase<TrainConfig> getPanelModelSet() {
        return this.modelset;
    }

    public Minecraft getPanelMinecraft() {
        return this.mc;
    }

    public FontRenderer getPanelFontRenderer() {
        return this.fontRendererObj;
    }

    public int getPanelLeft() {
        return this.guiLeft;
    }

    public int getPanelTop() {
        return this.guiTop;
    }

    public int getPanelWidth() {
        return this.xSize;
    }

    public int getPanelHeight() {
        return this.ySize;
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    @SuppressWarnings("unchecked")
    public List<GuiButton> getPanelButtons() {
        return (List<GuiButton>) this.buttonList;
    }

    public void addPanelButton(GuiButton button) {
        this.buttonList.add(button);
    }

    public DataMap getPanelDataMap() {
        return this.train.getResourceState().getDataMap();
    }

    public String[][] getCustomButtons() {
        return this.modelset.getConfig().customButtons;
    }

    public String[] getCustomButtonTips() {
        return this.modelset.getConfig().customButtonTips;
    }

    public void sendProtectionPluginState(String id, boolean enabled) {
        String message = "setTrainProtectionPlugin," + id + "," + enabled;
        RTMCore.NETWORK_WRAPPER.sendToServer(new PacketNotice(PacketNotice.Side_SERVER, message, this.train));
    }

    public void playPanelClickSound() {
        if (this.mc.thePlayer != null) {
            this.mc.thePlayer.playSound("random.click", 1.0F, 1.0F);
        }
    }

    public void drawPanelRect(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, bottom, color);
    }

    public void drawPanelCenteredString(String text, int x, int y, int color) {
        this.drawCenteredString(this.fontRendererObj, text, x, y, color);
    }

    public void enableGuiScissor(int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int scale = resolution.getScaleFactor();
        GL11.glScissor(x * scale, (this.height - y - height) * scale, width * scale, height * scale);
    }

    private TrainControlPanelPage getActivePage() {
        return this.pages.get(this.selectedTabIndex);
    }
}
