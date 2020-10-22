package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.editor.EntityEditor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Editorのブロックデータのやりとり専用
 */
public class PacketNBT implements IMessage, IMessageHandler<PacketNBT, IMessage> {
	public NBTTagCompound nbtData;

	public PacketNBT() {
	}

	public PacketNBT(EntityEditor entity, NBTTagCompound nbt) {
		this.nbtData = nbt;
		this.nbtData.setInteger("EntityId", entity.getEntityId());
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, this.nbtData);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.nbtData = ByteBufUtils.readTag(buffer);
	}

	public void onGetPacket(World world) {
		int id = this.nbtData.getInteger("EntityId");
		Entity entity = world.getEntityByID(id);
		if (entity instanceof EntityEditor) {
			((EntityEditor) entity).importBlocksFromNBT(this.nbtData);
		}
	}

	@Override
	public IMessage onMessage(PacketNBT message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;
		message.onGetPacket(world);
		return null;
	}
}