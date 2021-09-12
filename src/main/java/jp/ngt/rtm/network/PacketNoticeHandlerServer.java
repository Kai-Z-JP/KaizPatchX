package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityTrainWorkBench;
import jp.ngt.rtm.entity.npc.EntityMotorman;
import jp.ngt.rtm.entity.npc.macro.TrainCommand;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.gui.ContainerRTMWorkBench;
import jp.ngt.rtm.gui.ContainerTrainControlPanel;
import jp.ngt.rtm.modelpack.ModelPackUploadThread;
import jp.ngt.rtm.modelpack.state.DataMap;
import jp.ngt.rtm.rail.TileEntityMarker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketNoticeHandlerServer implements IMessageHandler<PacketNotice, IMessage> {
    @Override
    public IMessage onMessage(PacketNotice message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        String msg = message.notice;

        if ((message.type & 1) == PacketNotice.Side_SERVER) {
            if (message.notice.equals("isConnected")) {
                RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, "setConnected"));
            } else if (message.notice.startsWith("getModelPack")) {
                RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, "setConnected"));
                ModelPackUploadThread.startThread();
            } else if (message.notice.startsWith("StartCrafting")) {
                TileEntity tile = message.getTileEntity(world);
                if (tile instanceof TileEntityTrainWorkBench) {
                    ((TileEntityTrainWorkBench) tile).startCrafting(player, false);
                }
            } else if (message.notice.startsWith("setTrainTab")) {
                String[] sa = message.notice.split(",");
                int tabIndex = Integer.parseInt(sa[1]);
                Entity entity = message.getEntity(world);
                if (entity instanceof EntityPlayer) {
                    Container container = ((EntityPlayer) entity).openContainer;
                    if (container instanceof ContainerTrainControlPanel) {
                        ((ContainerTrainControlPanel) container).setCurrentTab(tabIndex);
                    }
                }
            } else if (message.notice.startsWith("workbench")) {
                String[] sa = message.notice.split(",");
                String name = sa[1];
                float h = Float.parseFloat(sa[2]);

                if (player.openContainer instanceof ContainerRTMWorkBench) {
                    ((ContainerRTMWorkBench) player.openContainer).setRailProp(name, h);
                }
            } else if (message.notice.startsWith("TMacro")) {
                Entity entity = message.getEntity(world);
                if (entity instanceof EntityMotorman) {
                    String s2 = message.notice.replace("TMacro" + TrainCommand.SEPARATOR, "");
                    String[] sa = s2.split(TrainCommand.SEPARATOR);
                    ((EntityMotorman) entity).setMacro(sa);
                }
            } else if (msg.startsWith("DM")) {
                DataMap.receivePacket(msg, message, world, false);
            } else if (msg.startsWith("notch")) {
                Entity entity = message.getEntity(world);
                if (entity instanceof EntityTrainBase) {
                    int notchInc = Integer.parseInt(msg.split(":")[1]);
                    ((EntityTrainBase) entity).addNotch(player, notchInc);
                }
            } else if (msg.equals("marker_update")) {
                TileEntity tile = message.getTileEntity(world);
                if (tile instanceof TileEntityMarker) {
                    ((TileEntityMarker) tile).updateMarkerRM(player);
                }
            }
        }
        return null;
    }
}