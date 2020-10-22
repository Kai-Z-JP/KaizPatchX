package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.editor.EntityEditor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

public class PacketResetSlot implements IMessage, IMessageHandler<PacketResetSlot, IMessage> {
	public String playerName;
	public int slotNumber;
	public int xPosition;
	public int yPosition;

	public PacketResetSlot() {
	}

	public PacketResetSlot(EntityEditor par1, Slot par2) {
		this.playerName = par1.getPlayer().getCommandSenderName();
		this.slotNumber = par2.slotNumber;
		this.xPosition = par2.xDisplayPosition;
		this.yPosition = par2.yDisplayPosition;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeUTF8String(buffer, this.playerName);
		buffer.writeInt(this.slotNumber);
		buffer.writeInt(this.xPosition);
		buffer.writeInt(this.yPosition);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.playerName = ByteBufUtils.readUTF8String(buffer);
		this.slotNumber = buffer.readInt();
		this.xPosition = buffer.readInt();
		this.yPosition = buffer.readInt();
	}

	@Override
	public IMessage onMessage(PacketResetSlot message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;
		EntityPlayer player = world.getPlayerEntityByName(message.playerName);
		if (player != null) {
			Slot slot = player.openContainer.getSlot(message.slotNumber);
			if (slot != null) {
				slot.xDisplayPosition = message.xPosition;
				slot.yDisplayPosition = message.yPosition;
				//NGTLog.debug("[RTM](Server) Reset Slot Position : " + message.slotNumber);
			}
		}
		return null;
	}
}