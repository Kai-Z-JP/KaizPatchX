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
import java.util.stream.Collectors;

public final class PermissionManager {
    public static final PermissionManager INSTANCE = new PermissionManager();
    private static final String ALL = "-all";

    private final File saveDir;
    private final File saveFile;
    private final Map<String, Set<String>> permissionMap = new HashMap<>();

    private static final boolean DEBUG_MODE = false;//シングルでOP無視

    private PermissionManager() {
        this.saveDir = new File(NGTFileLoader.getModsDir().get(0), "ngt");
        this.saveFile = new File(this.saveDir, "permission.txt");
    }

    public void save() {
        NGTText.writeToText(this.saveFile, this.permissionMap
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().stream().collect(Collectors.joining(",", entry.getKey() + ":", "")))
                .toArray(String[]::new));
    }

    public void load() throws IOException {
        this.initFile();

        NGTText.readText(this.saveFile, "").stream()
                .map(s -> s.split(":"))
                .filter(split -> split.length == 2)
                .collect(Collectors.toMap(split -> split[0], split -> Arrays.asList(split[1].split(","))))
                .forEach((key, value) -> this.getPlayerList(key).addAll(value));
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

    public Collection<String> getPlayerList(String par1) {
        return this.permissionMap.computeIfAbsent(par1, k -> new HashSet<>());
    }

    public void registerPermission(String per1) {
        this.getPlayerList(per1);
        this.getPlayerList("negative." + per1);
    }

    public void showPermissionList(ICommandSender player) {
        this.permissionMap.entrySet().stream()
                .map(entry -> entry.getValue().stream().collect(Collectors.joining(", ", entry.getKey() + ": ", "")))
                .forEach(sb -> NGTLog.sendChatMessage(player, sb));
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
        boolean has = this.hasPermissionInternal(player, category);
        if (!has) {
            NGTLog.sendChatMessage(player, "%s need permission (%s).", player.getCommandSenderName(), category);
        }
        return has;
    }

    private boolean hasPermissionInternal(ICommandSender player, String category) {
        if (this.getPlayerList("negative.".concat(category)).contains(player.getCommandSenderName())) {
            return false;
        }
        if (!category.equals("fixrtm.all_permit") && hasPermissionInternal(player, "fixrtm.all_permit"))
            return true;
        if (this.isOp(player)) {
            return true;//シングル or OP
        } else {
            Collection<String> players = this.getPlayerList(category);
            return players.contains(player.getCommandSenderName()) || players.contains(ALL);
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
