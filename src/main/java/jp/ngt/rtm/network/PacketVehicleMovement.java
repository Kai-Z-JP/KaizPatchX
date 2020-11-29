package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class PacketVehicleMovement implements IMessage, IMessageHandler<PacketVehicleMovement, IMessage> {
    private static final double SAMPLING = 32.0D;
    private static final double DIV_32 = 1.0D / SAMPLING;

    public int entityId;
    /**
     * 0:Other, 1:Train, 2:Vehicle
     */
    public byte type;
    public int trainX, trainY, trainZ;
    public float trainYaw, trainPitch, trainRoll, trainSpeed;

    public PacketVehicleMovement() {
    }

    public PacketVehicleMovement(Entity par1, boolean onDead) {
        this.entityId = par1.getEntityId();
        this.type = 0;
        this.trainX = MathHelper.floor_double(par1.posX * SAMPLING);
        this.trainY = MathHelper.floor_double(par1.posY * SAMPLING);
        this.trainZ = MathHelper.floor_double(par1.posZ * SAMPLING);
        this.trainYaw = par1.rotationYaw;
        this.trainPitch = par1.rotationPitch;
        if (par1 instanceof EntityVehicleBase) {
            this.type = 2;
            this.trainRoll = ((EntityVehicleBase) par1).getRoll();
            this.trainSpeed = ((EntityVehicleBase) par1).getSpeed();
        } else {
            this.trainRoll = ((EntityBogie) par1).rotationRoll;
        }

        if (onDead) {
            this.trainY = -1;
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
        buffer.writeFloat(this.trainSpeed);
        buffer.writeFloat(this.trainRoll);
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
        this.trainSpeed = buffer.readFloat();
        this.trainRoll = buffer.readFloat();
    }

    @Override
    public IMessage onMessage(PacketVehicleMovement message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        if (world == null) {
            return null;
        }

        if (message.trainY < 0) {
            Entity entity = world.getEntityByID(message.entityId);
            if (entity == null) {
                entity = ((List<Entity>) world.weatherEffects).stream().filter(entity2 -> entity2.getEntityId() == message.entityId).findFirst().orElse(entity);
            }

            if (entity != null) {
                //Clinet側でsetDead()が呼ばれないことにより、WeatherEffectとして残り続けることの対策
                entity.setDead();
                return null;
            }
        }

        Entity entity = world.getEntityByID(message.entityId);
        if (entity != null) {
            entity.serverPosX = message.trainX;
            entity.serverPosY = message.trainY;
            entity.serverPosZ = message.trainZ;
            double x = (double) entity.serverPosX * DIV_32;
            double y = (double) entity.serverPosY * DIV_32;
            double z = (double) entity.serverPosZ * DIV_32;

            entity.setPositionAndRotation2(x, y, z, message.trainYaw, message.trainPitch, 3);
            if (entity instanceof EntityVehicleBase) {
                ((EntityVehicleBase) entity).setRollAndSpeed(message.trainSpeed, message.trainRoll);
            } else {
                ((EntityBogie) entity).setRoll(message.trainRoll);
            }
        }
        return null;
    }
}