package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class PacketRenderBlocks implements IMessage, IMessageHandler<PacketRenderBlocks, IMessage> {
    private int entityId;
    private byte flag;
    private NGTObject blocksData;

    public PacketRenderBlocks() {
    }

    public PacketRenderBlocks(EntityEditor par1, NGTObject ngto) {
        this.entityId = par1.getEntityId();

        if (ngto == null) {
            this.flag = 0;
        } else {
            this.blocksData = ngto;
            this.flag = 1;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeByte(this.flag);
        if (this.flag == 1) {
            ByteBufUtils.writeTag(buffer, this.blocksData.writeToNBT());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.flag = buffer.readByte();
        if (this.flag == 1) {
            this.blocksData = NGTObject.readFromNBT(ByteBufUtils.readTag(buffer));
        }
    }

    @Override
    public IMessage onMessage(PacketRenderBlocks message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        Entity entity = world.getEntityByID(message.entityId);
        if (entity instanceof EntityEditor) {
            EntityEditor editor = (EntityEditor) entity;
            if (message.flag == 1) {
                editor.blocksForRenderer = message.blocksData;
                editor.setUpdate(true);
            } else {
                editor.blocksForRenderer = null;
            }
        }
        return null;
    }
}