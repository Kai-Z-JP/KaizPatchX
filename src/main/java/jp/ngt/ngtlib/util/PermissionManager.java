package jp.ngt.ngtlib.util;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class PermissionManager {
    public static final PermissionManager INSTANCE = new PermissionManager();

    private final File saveDir;
    private final File saveFile;
    private final Map<String, List<String>> permissionMap = new HashMap<>();

    private static final boolean DEBUG_MODE = false;//シングルでOP無視

    private PermissionManager() {
        this.saveDir = new File(NGTFileLoader.getModsDir().get(0), "ngt");
        this.saveFile = new File(this.saveDir, "permission.txt");
    }

    public void save() {
        String[] sa = new String[this.permissionMap.size()];
        int i = 0;
        for (Entry<String, List<String>> entry : this.permissionMap.entrySet()) {
            sa[i] = entry.getValue().stream().map(s -> s + ",").collect(Collectors.joining("", entry.getKey() + ":", ""));
            ++i;
        }
        NGTText.writeToText(this.saveFile, sa);
    }

    public void load() throws IOException {
        this.initFile();

        List<String> slist = NGTText.readText(this.saveFile, "");
        slist.stream().map(s -> s.split(":")).filter(sa2 -> sa2.length == 2).forEach(sa2 -> {
            List<String> list = this.getPlayerList(sa2[0]);
            String[] sa3 = sa2[1].split(",");
            list.addAll(Arrays.asList(sa3));
        });
    }

    private void initFile() {
        if (!this.saveDir.exists()) {
            this.saveDir.mkdirs();
        }

        if (!this.saveFile.exists()) {
            try {
                this.saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getPlayerList(String par1) {
        if (!this.permissionMap.containsKey(par1)) {
            this.permissionMap.put(par1, new ArrayList<>());
        }
        return this.permissionMap.get(par1);
    }

    public void registerPermission(String per1) {
        this.getPlayerList(per1);
    }

    public void showPermissionList(ICommandSender player) {
        this.permissionMap.entrySet().stream().map(entry -> entry.getValue().stream().map(s -> s + ",").collect(Collectors.joining("", entry.getKey() + ":", ""))).forEach(sb -> NGTLog.sendChatMessage(player, sb));
    }

    public List<String> getPermissionList() {
        return new ArrayList<>(this.permissionMap.keySet());
    }

    public void addPermission(ICommandSender player, String targetPlayerName, String category) {
        if (this.isOp(player)) {
            this.getPlayerList(category).add(targetPlayerName);
            NGTLog.sendChatMessageToAll("Add permission (%s) to %s.", category, targetPlayerName);
            this.save();
        } else {
            NGTLog.sendChatMessage(player, "Only operator can use this command.");
        }
    }

    public void removePermission(ICommandSender player, String targetPlayerName, String category) {
        if (this.isOp(player)) {
            this.getPlayerList(category).remove(targetPlayerName);
            NGTLog.sendChatMessageToAll("Remove permission (%s) from %s.", category, targetPlayerName);
            this.save();
        } else {
            NGTLog.sendChatMessage(player, "Only operator can use this command.");
        }
    }

    public boolean hasPermission(ICommandSender player, String category) {
        boolean has = hasPermissionInternal(player, category);
        if (!has)
            NGTLog.sendChatMessageToAll("%s need permission (%s).", player.getCommandSenderName(), category);
        return has;
    }

    private boolean hasPermissionInternal(ICommandSender player, String category) {
        if (this.getPlayerList("negative.".concat(category)).contains(player.getCommandSenderName())) {
            return false;
        }
        if (this.isOp(player)) {
            return true;//シングル or OP
        } else {
            if (this.getPlayerList(category).contains(player.getCommandSenderName())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isOp(ICommandSender player) {
        if (!DEBUG_MODE && !NGTUtil.isSMP()) {
            return true;//シングルでは常に使用可
        } else if (player instanceof EntityPlayerMP) {
            String[] names = ((EntityPlayerMP) player).mcServer.getConfigurationManager().func_152606_n();
            return Arrays.stream(names).anyMatch(name -> player.getCommandSenderName().equals(name));
        } else return player instanceof MinecraftServer;
    }
}
