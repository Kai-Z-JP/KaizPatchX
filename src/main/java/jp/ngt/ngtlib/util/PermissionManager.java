package jp.ngt.ngtlib.util;

import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class PermissionManager {
	public static final PermissionManager INSTANCE = new PermissionManager();

	private final File saveDir;
	private final File saveFile;
	private final Map<String, List<String>> permissionMap = new HashMap<>();

	private static final boolean DEBUG_MODE = false;//シングルでOP無視

	private PermissionManager() {
		String path = NGTCore.proxy.getMinecraftDirectory("ngt").getAbsolutePath();
		if (path.contains(".") && !path.contains(".minecraft")) {
			path = path.replace("\\.", "");//開発環境では\.が含まれるため
		}
		this.saveDir = new File(path);
		this.saveFile = new File(this.saveDir, "permission.txt");
	}

	public void save() {
		String[] sa = new String[this.permissionMap.size()];
		int i = 0;
		for (Entry<String, List<String>> entry : this.permissionMap.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey()).append(":");
			for (String s : entry.getValue()) {
				sb.append(s).append(",");
			}
			sa[i] = sb.toString();
			++i;
		}
		NGTText.writeToText(this.saveFile, sa);
	}

	public void load() throws IOException {
		this.initFile();

		List<String> slist = NGTText.readText(this.saveFile, "");
		for (String s : slist) {
			String[] sa2 = s.split(":");
			if (sa2.length == 2) {
				List<String> list = this.getPlayerList(sa2[0]);
				String[] sa3 = sa2[1].split(",");
				for (String s2 : sa3) {
					list.add(s2);
				}
			}
		}
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
			this.permissionMap.put(par1, new ArrayList<String>());
		}
		return this.permissionMap.get(par1);
	}

	public void showPermissionList(ICommandSender player) {
		for (Entry<String, List<String>> entry : this.permissionMap.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey()).append(":");
			for (String s : entry.getValue()) {
				sb.append(s).append(",");
			}
			NGTLog.sendChatMessage(player, sb.toString());
		}
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
		if (this.isOp(player)) {
			return true;//シングル or OP
		} else {
			if (this.getPlayerList(category).contains(player.getCommandSenderName())) {
				return true;
			} else {
				NGTLog.sendChatMessageToAll("%s need permission (%s).", player.getCommandSenderName(), category);
				return false;
			}
		}
	}

	public boolean isOp(ICommandSender player) {
		if (!DEBUG_MODE && !NGTUtil.isSMP()) {
			return true;//シングルでは常に使用可
		} else if (player instanceof EntityPlayerMP) {
			String[] names = ((EntityPlayerMP) player).mcServer.getConfigurationManager().func_152606_n();
			for (String name : names) {
				if (player.getCommandSenderName().equals(name)) {
					return true;
				}
			}
		} else return player instanceof MinecraftServer;
		return false;
	}
}