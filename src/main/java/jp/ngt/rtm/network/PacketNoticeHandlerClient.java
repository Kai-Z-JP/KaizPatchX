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
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketNoticeHandlerClient implements IMessageHandler<PacketNotice, IMessage> {
	@Override
	public IMessage onMessage(PacketNotice message, MessageContext ctx) {
		String msg = message.notice;

		if ((message.type & 1) == PacketNotice.Side_CLIENT) {
			World world = NGTUtil.getClientWorld();

			if (message.notice.equals("setConnected")) {
				RTMCore.proxy.setConnectionState((byte) 1);
			} else if (message.notice.startsWith("changeDisplayList")) {
				;
			} else if (message.notice.startsWith("fire")) {
				Entity entity = message.getEntity(world);
				if (entity instanceof EntityArtillery) {
					((EntityArtillery) entity).recoilCount = EntityArtillery.MaxRecoilCount;
				}
			} else if (message.notice.startsWith("marker")) {
				String[] sa0 = message.notice.split(",");
				int v = Integer.parseInt(sa0[1]);
				TileEntity tile = message.getTileEntity(world);
				if (tile instanceof TileEntityMarker) {
					((TileEntityMarker) tile).setDisplayMode((byte) v);
				}
			} else if (message.notice.startsWith("MM")) {
				String[] sa0 = message.notice.split(",");
				int v = Integer.parseInt(sa0[1]);
				TileEntity tile = message.getTileEntity(world);
				if (tile instanceof TileEntityMovingMachine) {
					((TileEntityMovingMachine) tile).setMovement((byte) v);
				}
			} else if (message.notice.startsWith("TRec")) {
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
			}
		}
		return null;
	}
}