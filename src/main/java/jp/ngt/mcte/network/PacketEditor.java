package jp.ngt.mcte.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EditorManager;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.ngtlib.block.NGTObject;
import net.minecraft.world.World;

public class PacketEditor implements IMessage, IMessageHandler<PacketEditor, IMessage> {
    private String playerName;
    private String type;
    private int[] start;
    private int[] end;
    private int[] clone;

    public PacketEditor() {
    }

    /**
     * @param par4 start
     * @param par5 end
     * @param par6 clone
     */
    public PacketEditor(EntityEditor par1, String par2, int[] par4, int[] par5, int[] par6) {
        this.playerName = par1.getPlayer().getCommandSenderName();
        this.type = par2;
        this.start = par4;
        this.end = par5;
        this.clone = par6;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.playerName);
        ByteBufUtils.writeUTF8String(buffer, this.type);
        buffer.writeInt(this.start[0]);
        buffer.writeInt(this.start[1]);
        buffer.writeInt(this.start[2]);
        buffer.writeInt(this.end[0]);
        buffer.writeInt(this.end[1]);
        buffer.writeInt(this.end[2]);
        buffer.writeInt(this.clone[0]);
        buffer.writeInt(this.clone[1]);
        buffer.writeInt(this.clone[2]);
        buffer.writeInt(this.clone[3]);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.playerName = ByteBufUtils.readUTF8String(buffer);
        this.type = ByteBufUtils.readUTF8String(buffer);
        this.start = new int[3];
        this.start[0] = buffer.readInt();
        this.start[1] = buffer.readInt();
        this.start[2] = buffer.readInt();
        this.end = new int[3];
        this.end[0] = buffer.readInt();
        this.end[1] = buffer.readInt();
        this.end[2] = buffer.readInt();
        this.clone = new int[4];
        this.clone[0] = buffer.readInt();
        this.clone[1] = buffer.readInt();
        this.clone[2] = buffer.readInt();
        this.clone[3] = buffer.readInt();
    }

    @Override
    public IMessage onMessage(PacketEditor message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        Editor editor = EditorManager.INSTANCE.getEditor(message.playerName);
        if (editor != null) {
            editor.getEntity().setPos(true, message.start[0], message.start[1], message.start[2]);
            editor.getEntity().setPos(false, message.end[0], message.end[1], message.end[2]);
            editor.getEntity().setCloneBox(message.clone[0], message.clone[1], message.clone[2], message.clone[3]);

            if (message.type.equals("replace")) {
                editor.editBlocks(Editor.EditType_Replace, 0.0F);
            } else if (message.type.equals("clone")) {
                editor.editBlocks(Editor.EditType_Clone, 0.0F);
            } else if (message.type.startsWith("miniature")) {
                String[] sa = message.type.split(":");
                float rate = Float.parseFloat(sa[1]);
                editor.editBlocks(Editor.EditType_Miniature, rate);
            } else if (message.type.startsWith("transform")) {
                String[] sa = message.type.split(":");
                int type = Integer.parseInt(sa[1]);
                editor.transformBlocks((byte) type);
            } else if (message.type.startsWith("export")) {
                String[] sa = message.type.split(" ");
                NGTObject ngto = editor.copy(editor.getSelectBox(), "").convertNGTO();
                MCTE.NETWORK_WRAPPER.sendTo(new PacketExportData(sa[1], ngto), ctx.getServerHandler().playerEntity);
            }
        }
        return null;
    }
}