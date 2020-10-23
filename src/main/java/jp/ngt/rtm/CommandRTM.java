package jp.ngt.rtm;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CommandRTM extends CommandBase {
	@Override
	public String getCommandName() {
		return "rtm";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.rtm.usage";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] s) {
		EntityPlayerMP player = getCommandSenderAsPlayer(commandSender);
		if (s.length >= 1) {
			if (s[0].equalsIgnoreCase("use1122marker")) {
				RTMCore.NETWORK_WRAPPER.sendTo(new PacketNotice(PacketNotice.Side_CLIENT, "use1122marker," + (s.length == 2 ? Boolean.parseBoolean(s[1]) : "flip")), player);
				return;
			}
		}

		if (s.length == 2) {

			int state = Integer.parseInt(s[1]);

			double d0 = 16.0D;
			List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - d0, player.posY - d0, player.posZ - d0, player.posX + d0, player.posY + d0, player.posZ + d0));
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				Entity entity = (Entity) iterator.next();
				if (entity instanceof EntityTrainBase) {
					EntityTrainBase train = (EntityTrainBase) entity;
					if (s[0].equalsIgnoreCase("door")) {
						train.setTrainStateData(TrainStateType.State_Door.id, (byte) state);
					} else if (s[0].equalsIgnoreCase("pan")) {
						train.setTrainStateData(TrainStateType.State_Pantograph.id, (byte) state);
					} else if (s[0].equalsIgnoreCase("speed")) {
						train.setSpeed(state / 72.0f);
					}
				}
			}
		} else {
			if (s[0].equalsIgnoreCase("delAllTrain")) {
				int count = 0;
				List list = player.worldObj.loadedEntityList;
				for (Object object : list) {
					Entity entity = null;
					if (object instanceof EntityTrainBase) {
						entity = (EntityTrainBase) object;
						++count;
					} else if (object instanceof EntityBogie) {
						entity = (EntityBogie) object;
					}

					if (entity != null && !entity.isDead) {
						entity.setDead();
					}
				}

				NGTLog.sendChatMessage(player, "Delete " + count + "trains.");
			}
		}
	}

	private static final List<String> commandList = Arrays.asList("use1122marker", "door", "pan", "speed", "delAllTrain");

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			//入力されている文字列と先頭一致
			if (args[0].length() == 0) {
				return commandList;
			}
			for (String s : commandList) {
				if (s.startsWith(args[0])) {
					return Collections.singletonList(s);
				}
			}
		}
		return null;
	}

}