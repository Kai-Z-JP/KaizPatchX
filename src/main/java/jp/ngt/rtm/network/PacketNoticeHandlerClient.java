package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import jp.ngt.rtm.entity.npc.macro.MacroRecorder;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.modelpack.state.DataMap;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.sound.SpeakerSounds;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class PacketNoticeHandlerClient implements IMessageHandler<PacketNotice, IMessage> {
    @Override
    public IMessage onMessage(PacketNotice message, MessageContext ctx) {
        String msg = message.notice;

        if ((message.type & 1) == PacketNotice.Side_CLIENT) {
            World world = NGTUtil.getClientWorld();

            if (msg.equals("setConnected")) {
                RTMCore.proxy.setConnectionState((byte) 1);
            } else if (msg.startsWith("changeDisplayList")) {
            } else if (msg.startsWith("fire")) {
                Entity entity = message.getEntity(world);
                if (entity instanceof EntityArtillery) {
                    ((EntityArtillery) entity).recoilCount = EntityArtillery.MaxRecoilCount;
                }
            } else if (msg.startsWith("marker")) {
                String[] sa0 = msg.split(",");
                int v = Integer.parseInt(sa0[1]);
                TileEntity tile = message.getTileEntity(world);
                if (tile instanceof TileEntityMarker) {
                    ((TileEntityMarker) tile).setDisplayMode((byte) v);
                }
            } else if (msg.startsWith("MM")) {
                String[] sa0 = msg.split(",");
                int v = Integer.parseInt(sa0[1]);
                TileEntity tile = message.getTileEntity(world);
                if (tile instanceof TileEntityMovingMachine) {
                    ((TileEntityMovingMachine) tile).setMovement((byte) v);
                }
            } else if (msg.startsWith("TRec")) {
                if (MacroRecorder.INSTANCE.isRecording()) {
                    MacroRecorder.INSTANCE.stop(world);
                } else {
                    MacroRecorder.INSTANCE.start(world);
                }
            } else if (msg.startsWith("DM")) {
                if (world == null) {
                    return null;
                }
                DataMap.receivePacket(msg, message, world, true);
            } else if (msg.startsWith("use1122marker")) {
                String[] sa0 = msg.split(",");
                boolean use1122marker = sa0[1].equals("flip") ? !RTMCore.use1122Marker : Boolean.parseBoolean(sa0[1]);
                RTMCore.use1122Marker = use1122marker;
                RTMCore.marker.setValue(use1122marker);
                RTMCore.cfg.save();
                NGTUtil.getClientPlayer().addChatMessage(new ChatComponentText("Config: use1122marker = " + use1122marker));
            } else if (msg.startsWith("speaker")) {
                SpeakerSounds.getInstance(false).onGetPacket(msg, false);
            } else if (msg.startsWith("flySpeed")) {
                String[] sa0 = msg.split(",");
                float speed = Float.parseFloat(sa0[1]);
                NGTUtil.getClientPlayer().capabilities.setFlySpeed(speed / 20f);
                NGTUtil.getClientPlayer().addChatMessage(new ChatComponentText("FlySpeed set " + sa0[1]));
                NGTUtil.getClientPlayer().sendPlayerAbilities();
            }
        }
        return null;
    }
}