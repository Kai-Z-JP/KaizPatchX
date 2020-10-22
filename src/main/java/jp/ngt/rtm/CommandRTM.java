package jp.ngt.rtm;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

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
		EntityPlayer player = getCommandSenderAsPlayer(commandSender);

		if (s.length == 2) {
			byte state = (byte) ((int) Integer.valueOf(s[1]));

			double d0 = 16.0D;
			List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - d0, player.posY - d0, player.posZ - d0, player.posX + d0, player.posY + d0, player.posZ + d0));
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				Entity entity = (Entity) iterator.next();
				if (entity instanceof EntityTrainBase) {
					EntityTrainBase train = (EntityTrainBase) entity;
					if (s[0].equals("door")) {
						train.setTrainStateData(TrainStateType.State_Door.id, state);
					} else if (s[0].equals("pan")) {
						train.setTrainStateData(TrainStateType.State_Pantograph.id, state);
					}
				}
			}
		} else {
			if (s[0].equals("delAllTrain")) {
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
}