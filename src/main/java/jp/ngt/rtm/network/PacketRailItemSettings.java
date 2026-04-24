package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.gui.ContainerRailItemSettings;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;

public class PacketRailItemSettings implements IMessage, IMessageHandler<PacketRailItemSettings, IMessage> {
    private float height;
    private boolean noBallast;

    public PacketRailItemSettings() {
    }

    public PacketRailItemSettings(float height, boolean noBallast) {
        this.height = height;
        this.noBallast = noBallast;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeFloat(this.height);
        buffer.writeBoolean(this.noBallast);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.height = buffer.readFloat();
        this.noBallast = buffer.readBoolean();
    }

    @Override
    public IMessage onMessage(PacketRailItemSettings message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (!(player.openContainer instanceof ContainerRailItemSettings)) {
            return null;
        }

        ContainerRailItemSettings container = (ContainerRailItemSettings) player.openContainer;
        ItemStack rail = container.getRailItem();
        if (rail == null || rail.getItem() != RTMItem.itemLargeRail) {
            return null;
        }

        RailProperty property = container.createAppliedProperty(message.height, message.noBallast);
        ItemRail.writePropToItem(property, rail);
        player.openContainer.detectAndSendChanges();

        if (player instanceof EntityPlayerMP) {
            this.updateCurrentItem((EntityPlayerMP) player);
        }
        return null;
    }

    private void updateCurrentItem(EntityPlayerMP player) {
        Slot slot = player.inventoryContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.inventoryContainer.windowId, slot.slotNumber, player.inventory.getCurrentItem()));
    }
}
