package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.vehicle.EntityVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class PacketVehicleMovement implements IMessage, IMessageHandler<PacketVehicleMovement, IMessage> {
	private static final double DIV_32 = 1.0D / 32.0D;

	public int entityId;
	/**
	 * 0:Other, 1:Train, 2:Vehicle
	 */
	public byte type;
	public int trainX, trainY, trainZ;
	public float trainYaw, trainPitch;
	public float trainSpeed;

	public PacketVehicleMovement() {
	}

	public PacketVehicleMovement(Entity par1) {
		this.entityId = par1.getEntityId();
		this.type = 0;
		this.trainX = par1.myEntitySize.multiplyBy32AndRound(par1.posX);
		this.trainY = MathHelper.floor_double(par1.posY * 32.0D);
		this.trainZ = par1.myEntitySize.multiplyBy32AndRound(par1.posZ);
		this.trainYaw = par1.rotationYaw;
		this.trainPitch = par1.rotationPitch;
		if (par1 instanceof EntityTrainBase) {
			this.type = 1;
			this.trainSpeed = ((EntityTrainBase) par1).getSpeed();
		} else if (par1 instanceof EntityVehicle) {
			this.type = 2;
			this.trainSpeed = ((EntityVehicle) par1).rotationRoll;
		} else//bogie
		{
			;
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeByte(this.type);
		buffer.writeInt(this.trainX);
		buffer.writeInt(this.trainY);
		buffer.writeInt(this.trainZ);
		buffer.writeFloat(this.trainYaw);
		buffer.writeFloat(this.trainPitch);
		if (this.type > 0) {
			buffer.writeFloat(this.trainSpeed);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.entityId = buffer.readInt();
		this.type = buffer.readByte();
		this.trainX = buffer.readInt();
		this.trainY = buffer.readInt();
		this.trainZ = buffer.readInt();
		this.trainYaw = buffer.readFloat();
		this.trainPitch = buffer.readFloat();
		if (this.type > 0) {
			this.trainSpeed = buffer.readFloat();
		}
	}

	@Override
	public IMessage onMessage(PacketVehicleMovement message, MessageContext ctx) {
		World world = NGTUtil.getClientWorld();
		if (world == null) {
			return null;
		}

		Entity entity = world.getEntityByID(message.entityId);
		if (entity != null) {
			entity.serverPosX = message.trainX;
			entity.serverPosY = message.trainY;
			entity.serverPosZ = message.trainZ;
			double x = (double) entity.serverPosX * DIV_32;
			double y = (double) entity.serverPosY * DIV_32;
			double z = (double) entity.serverPosZ * DIV_32;

			entity.setPositionAndRotation2(x, y, z, message.trainYaw, message.trainPitch, 4);
			if (message.type == 1) {
				entity.setVelocity(message.trainSpeed, 0.0D, 0.0D);
			} else if (message.type == 2) {
				((EntityVehicle) entity).setRoll(message.trainSpeed);
			}
		}
		return null;
	}
}