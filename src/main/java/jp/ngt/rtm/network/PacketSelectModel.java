package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketSelectModel implements IMessage, IMessageHandler<PacketSelectModel, IMessage> {
    private int[] pos;
    private String modelName;
    private NBTTagCompound data;

    public PacketSelectModel() {
    }

    public PacketSelectModel(IModelSelector selsector, String name) {
        this.pos = selsector.getPos();
        this.modelName = name;
        this.data = selsector.getResourceState().writeToNBT();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.pos[0]);
        buffer.writeInt(this.pos[1]);
        buffer.writeInt(this.pos[2]);
        ByteBufUtils.writeUTF8String(buffer, this.modelName);
        ByteBufUtils.writeTag(buffer, this.data);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.pos = new int[3];
        this.pos[0] = buffer.readInt();
        this.pos[1] = buffer.readInt();
        this.pos[2] = buffer.readInt();
        this.modelName = ByteBufUtils.readUTF8String(buffer);
        this.data = ByteBufUtils.readTag(buffer);
    }

    @Override
    public IMessage onMessage(PacketSelectModel message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        IModelSelector selector = null;

        if (message.pos[1] >= 0) {
            TileEntity tile = world.getTileEntity(message.pos[0], message.pos[1], message.pos[2]);
            if (tile instanceof IModelSelector) {
                selector = (IModelSelector) tile;
            }
        } else {
            Entity entity = world.getEntityByID(message.pos[0]);
            if (entity instanceof IModelSelector) {
                selector = (IModelSelector) entity;
            }
        }

        if (selector != null) {
            if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.CHANGE_MODEL))//GUI操作はClientOnlyなのでここで対応
            {
                selector.setModelName(message.modelName);
                selector.getResourceState().readFromNBT(message.data);
            }
        }

        return null;
    }
}