package jp.ngt.rtm.network

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import jp.ngt.rtm.item.ItemBlockMarker
import net.minecraft.network.play.server.S2FPacketSetSlot

class PacketMarkerItemSettings() : IMessage, IMessageHandler<PacketMarkerItemSettings, IMessage> {
    private var hotbarSlot = 0
    private var height = 0

    constructor(hotbarSlot: Int, height: Int) : this() {
        this.hotbarSlot = hotbarSlot
        this.height = height
    }

    override fun toBytes(buffer: ByteBuf) {
        buffer.writeByte(hotbarSlot)
        buffer.writeByte(height)
    }

    override fun fromBytes(buffer: ByteBuf) {
        hotbarSlot = buffer.readByte().toInt()
        height = buffer.readByte().toInt()
    }

    override fun onMessage(message: PacketMarkerItemSettings, ctx: MessageContext): IMessage? {
        val player = ctx.serverHandler.playerEntity
        if (message.hotbarSlot !in 0..8 || player.inventory.currentItem != message.hotbarSlot) {
            return null
        }

        val itemStack = player.inventory.getStackInSlot(message.hotbarSlot)
        if (itemStack == null || itemStack.item !is ItemBlockMarker) {
            return null
        }

        ItemBlockMarker.setMarkerHeight(itemStack, message.height)
        player.inventory.markDirty()
        player.inventoryContainer.detectAndSendChanges()

        val slot = player.inventoryContainer.getSlotFromInventory(player.inventory, message.hotbarSlot)
        player.playerNetServerHandler.sendPacket(
            S2FPacketSetSlot(player.inventoryContainer.windowId, slot.slotNumber, itemStack),
        )
        return null
    }
}
