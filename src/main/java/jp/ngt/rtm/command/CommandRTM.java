package jp.ngt.rtm.command;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            if (s[0].equalsIgnoreCase("flySpeed")) {
                float speed = MathHelper.clamp_float(Float.parseFloat(s[1]), 0, 10);
                RTMCore.NETWORK_WRAPPER.sendTo(new PacketNotice(PacketNotice.Side_CLIENT, "flySpeed," + speed), player);
                return;
            }

            int state = Integer.parseInt(s[1]);

            double d0 = 16.0D;
            List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - d0, player.posY - d0, player.posZ - d0, player.posX + d0, player.posY + d0, player.posZ + d0));
            list.stream().filter(EntityTrainBase.class::isInstance).map(EntityTrainBase.class::cast).forEach(train -> {
                if (s[0].equalsIgnoreCase("door")) {
                    train.setTrainStateData(TrainStateType.State_Door.id, (byte) state);
                } else if (s[0].equalsIgnoreCase("pan")) {
                    train.setTrainStateData(TrainStateType.State_Pantograph.id, (byte) state);
                } else if (s[0].equalsIgnoreCase("speed")) {
                    train.setSpeed(state / 72.0f);
                }
            });
        } else {
            if (s[0].equalsIgnoreCase("delAllTrain")) {
                int countTrain = 0;
                int countEntity = 0;
                List<Entity> list = player.worldObj.loadedEntityList;
                for (Entity entity0 : list) {
                    Entity entity1;
                    if (entity0 instanceof EntityTrainBase) {
                        entity1 = entity0;
                        ++countTrain;
                    } else if (entity0 instanceof EntityBogie || entity0 instanceof EntityFloor) {
                        entity1 = entity0;
                    } else {
                        continue;
                    }
                    ++countEntity;

                    if (!entity1.isDead) {
                        entity1.setDead();
                    }
                }
                int countFormation = FormationManager.getInstance().clearFormations();

                player.addChatMessage(new ChatComponentText("Deleted " + countTrain + "trains."));
                player.addChatMessage(new ChatComponentText("Deleted " + countEntity + "entities."));
                player.addChatMessage(new ChatComponentText("Deleted " + countFormation + "formations."));
            }
        }
    }

    private static final List<String> commandList = Arrays.asList("use1122marker", "door", "pan", "speed", "delAllTrain", "flySpeed");

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            //入力されている文字列と先頭一致
            if (args[0].length() == 0) {
                return commandList;
            }
            return commandList.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }

}