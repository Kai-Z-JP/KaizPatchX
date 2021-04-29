package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.world.World;

import java.io.File;

/**
 * NGTObjectをClientのファイルへ出力
 */
public class PacketExportData implements IMessage, IMessageHandler<PacketExportData, IMessage> {
    private String fileName;
    private NGTObject blocksData;

    public PacketExportData() {
    }

    public PacketExportData(String par1, NGTObject par2) {
        this.fileName = par1;
        this.blocksData = par2;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.fileName);
        ByteBufUtils.writeTag(buffer, this.blocksData.writeToNBT());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.fileName = ByteBufUtils.readUTF8String(buffer);
        this.blocksData = NGTObject.readFromNBT(ByteBufUtils.readTag(buffer));
    }

    @Override
    public IMessage onMessage(PacketExportData message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        File file = new File(message.fileName);
        message.blocksData.exportToFile(file);
        return null;
    }
}