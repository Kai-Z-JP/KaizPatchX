package jp.ngt.rtm.gui;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRailItemSettings extends Container {
    private static final int BALLAST_SLOT_INDEX = 0;

    private final InventoryBasic ballastInventory = new InventoryBasic("RailBallast", false, 1);
    private final EntityPlayer player;
    private final int lockedHotbarSlot;
    private int lockedContainerSlot = -1;

    public ContainerRailItemSettings(InventoryPlayer inventory) {
        this.player = inventory.player;
        this.lockedHotbarSlot = inventory.currentItem;

        this.addSlotToContainer(new SlotBallast(this.ballastInventory, 0, 104, 35));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addPlayerSlot(inventory, col + row * 9 + 9, 8 + col * 18, 120 + row * 18);
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addPlayerSlot(inventory, col, 8 + col * 18, 178);
        }
    }

    private void addPlayerSlot(InventoryPlayer inventory, int slotIndex, int x, int y) {
        Slot slot = new Slot(inventory, slotIndex, x, y);
        this.addSlotToContainer(slot);
        if (slotIndex == this.lockedHotbarSlot) {
            this.lockedContainerSlot = this.inventorySlots.size() - 1;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        ItemStack stack = player.inventory.getStackInSlot(this.lockedHotbarSlot);
        return stack != null && stack.getItem() == RTMItem.itemLargeRail;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, int clickType, EntityPlayer player) {
        if (slotId == this.lockedContainerSlot || (clickType == 2 && dragType == this.lockedHotbarSlot)) {
            return null;
        }

        if (slotId == BALLAST_SLOT_INDEX) {
            ItemStack cursor = player.inventory.getItemStack();
            if (this.isValidBallast(cursor)) {
                this.setBallastDisplay(cursor);
            }
            return cursor;
        }

        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot != null && slot.slotNumber != this.lockedContainerSlot && super.canDragIntoSlot(slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack() || slotIndex == this.lockedContainerSlot) {
            return null;
        }

        ItemStack stack = slot.getStack();
        result = stack.copy();
        if (slotIndex == BALLAST_SLOT_INDEX) {
            return null;
        } else if (this.isValidBallast(stack)) {
            this.setBallastDisplay(stack);
        } else {
            return null;
        }

        return result;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
    }

    public void clearBallastStack() {
        this.ballastInventory.setInventorySlotContents(0, null);
        this.detectAndSendChanges();
    }

    public ItemStack getRailItem() {
        return this.player.inventory.getStackInSlot(this.lockedHotbarSlot);
    }

    public ItemStack getBallastStack() {
        return this.ballastInventory.getStackInSlot(0);
    }

    public void clearBallastStackClient() {
        this.clearBallastStack();
    }

    private void setBallastDisplay(ItemStack stack) {
        ItemStack display = stack.copy();
        display.stackSize = 1;
        this.ballastInventory.setInventorySlotContents(0, display);
        this.detectAndSendChanges();
    }

    public RailProperty getCurrentProperty() {
        ItemStack rail = this.getRailItem();
        RailProperty prop = rail == null ? null : ItemRail.getProperty(rail);
        return prop == null ? ItemRail.getDefaultProperty() : prop;
    }

    public RailProperty createAppliedProperty(float height, boolean noBallast) {
        RailProperty current = this.getCurrentProperty();
        Block block = current.block;
        int meta = current.blockMetadata;

        if (noBallast) {
            block = Blocks.air;
            meta = 0;
        } else {
            ItemStack ballast = this.getBallastStack();
            if (ballast != null && this.isValidBallast(ballast)) {
                block = Block.getBlockFromItem(ballast.getItem());
                meta = ballast.getItemDamage();
            }
        }

        return new RailProperty(current.railModel, block, meta, height);
    }

    private boolean isValidBallast(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        Block block = Block.getBlockFromItem(stack.getItem());
        return block != null && block != Blocks.air;
    }

    private class SlotBallast extends Slot {
        public SlotBallast(InventoryBasic inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return ContainerRailItemSettings.this.isValidBallast(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
