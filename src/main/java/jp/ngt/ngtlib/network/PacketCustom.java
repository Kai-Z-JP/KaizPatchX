package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Entity, TileEntityへのPacket
 */
public abstract class PacketCustom implements IMessage {
	private NBTTagCompound targetData;

	public PacketCustom() {
	}

	public PacketCustom(Entity entity) {
		this.targetData = new NBTTagCompound();
		//World上に他Entityがいない場合、PlayerがID=0になる
		/*if(entity.getEntityId() <= 0)
		{
			throw new IllegalArgumentException("Entity ID is invalid");
		}*/
		this.targetData.setInteger("EntityId", entity.getEntityId());
	}

	public PacketCustom(TileEntity tileEntity) {
		this.targetData = new NBTTagCompound();
		if (tileEntity.yCoord <= 0) {
			throw new IllegalArgumentException("TileEntity's position is invalid");
		}
		this.targetData.setInteger("PosX", tileEntity.xCoord);
		this.targetData.setInteger("PosY", tileEntity.yCoord);
		this.targetData.setInteger("PosZ", tileEntity.zCoord);
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, this.targetData);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.targetData = ByteBufUtils.readTag(buffer);
	}

	//鯖側NBT読み出し時などではClientで
	public Entity getEntity(World world) {
		int id = this.targetData.getInteger("EntityId");
        return world.getEntityByID(id);
	}

	public TileEntity getTileEntity(World world) {
		int x = this.targetData.getInteger("PosX");
		int y = this.targetData.getInteger("PosY");
		int z = this.targetData.getInteger("PosZ");
        return world.getTileEntity(x, y, z);
	}

	public boolean forEntity() {
		return this.targetData.hasKey("EntityId");
	}
}