package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class PacketGenerator implements IMessage, IMessageHandler<PacketGenerator, IMessage> {
    private int x;
    private int y;
    private int z;
    private String blockName;
    private byte metadata;

    public PacketGenerator() {
    }

    /**
     * ブロックの情報を1つずつ送る
     */
    public PacketGenerator(int par1, int par2, int par3, String par4, byte par5) {
        this.x = par1;
        this.y = par2;
        this.z = par3;
        this.blockName = par4;
        this.metadata = par5;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buffer, this.blockName);
        buffer.writeByte(this.metadata);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
        this.blockName = ByteBufUtils.readUTF8String(buffer);
        this.metadata = buffer.readByte();
    }

    @Override
    public IMessage onMessage(PacketGenerator message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        Block block = Block.getBlockFromName(message.blockName);
        if (block != null && world.checkChunksExist(message.x, message.y, message.z, message.x + 1, message.y + 1, message.z + 1)) {
            world.setBlock(message.x, message.y, message.z, block, message.metadata, 2);
            return null;
        }
        return null;
    }
}