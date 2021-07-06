package jp.ngt.rtm.command;

import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMCtrl extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "mctrl";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.mctrl.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length == 0 || "help".equals(args[0])) {
            this.help(sender);
            return;
        }

        if (args.length < 3) {
            NGTLog.sendChatMessage(sender, "Invalid command.");
            return;
        }

        ModelCtrl ctrl = ModelCtrl.getCommand(args[1]);
        List<Entity> list = this.getTargets(sender, args[0], ctrl);
        if (list.isEmpty()) {
            NGTLog.sendChatMessage(sender, "Target not found.");
            return;
        }

        list.forEach(obj -> ctrl.executor.exec(obj, sender, args[1], args[2]));
    }

    private List<Entity> getTargets(ICommandSender player, String filter, ModelCtrl ctrl) {
        List<Entity> list = new ArrayList<>();
        List<Entity> allEntities = player.getEntityWorld().loadedEntityList;//getterはsideonly
        double distanceSq = Double.MAX_VALUE;
        allEntities.stream().filter(ctrl.filter::match).forEach(entity -> {
            if (filter.equals("@a")) {
                list.add(entity);
            } else if (filter.equals("@n")) {
                ChunkCoordinates cc = player.getPlayerCoordinates();
                double d1 = entity.getDistanceSq(cc.posX, cc.posY, cc.posZ);
                if (d1 < distanceSq) {
                    list.clear();
                    list.add(entity);
                }
            } else if (filter.startsWith("@r")) {
                int range = Integer.parseInt(filter.replace("@r:", ""));
                ChunkCoordinates cc = player.getPlayerCoordinates();
                double d1 = entity.getDistanceSq(cc.posX, cc.posY, cc.posZ);
                if (d1 <= range * range) {
                    list.add(entity);
                }
            } else if (filter.equals(entity.getCommandSenderName())) {
                list.add(entity);
            }
        });
        return list;
    }


    private void help(ICommandSender player) {
        NGTLog.sendChatMessage(player, "Target filter -> @a...all, @n...nearest, @r:00...range, or name");
        Arrays.stream(ModelCtrl.values()).filter(mc -> !mc.discription.isEmpty()).forEach(mc -> NGTLog.sendChatMessage(player, mc.discription));
    }

    private static final List<List<String>> subCommandsList = Arrays.asList(
            Arrays.asList("@a", "@n", "@r"),
            Arrays.asList("notch", "dir", "dm:", "state:")
    );

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length <= 2) {
            //入力されている文字列と先頭一致
            if (args[args.length - 1].length() == 0) {
                return subCommandsList.get(args.length - 1);
            }
            return subCommandsList.get(args.length - 1).stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }
}