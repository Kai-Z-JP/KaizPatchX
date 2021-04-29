package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.texture.ITextureHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketTextureHolder implements IMessage, IMessageHandler<PacketTextureHolder, IMessage> {
    private String textureName;
    private int xPos, yPos, zPos;

    public PacketTextureHolder() {
    }

    public PacketTextureHolder(String par1, ITextureHolder par2) {
        this.textureName = par1;

        if (par2 instanceof TileEntity) {
            this.xPos = ((TileEntity) par2).xCoord;
            this.yPos = ((TileEntity) par2).yCoord;
            this.zPos = ((TileEntity) par2).zCoord;
        } else if (par2 instanceof Entity) {
            this.xPos = ((Entity) par2).getEntityId();
            this.yPos = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.textureName);
        buffer.writeInt(this.xPos);
        buffer.writeInt(this.yPos);
        buffer.writeInt(this.zPos);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.textureName = ByteBufUtils.readUTF8String(buffer);
        this.xPos = buffer.readInt();
        this.yPos = buffer.readInt();
        this.zPos = buffer.readInt();
    }

    @Override
    public IMessage onMessage(PacketTextureHolder message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        ITextureHolder holder;
        if (message.yPos == -1) {
            holder = (ITextureHolder) world.getEntityByID(message.xPos);
        } else {
            holder = (ITextureHolder) world.getTileEntity(message.xPos, message.yPos, message.zPos);
        }

        if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.CHANGE_MODEL))//GUI操作はClientOnlyなのでここで対応
        {
            holder.setTexture(message.textureName);
        }

        return null;
    }
}