package jp.ngt.ngtlib.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NGTCertificate {
	@SideOnly(Side.CLIENT)
	private static boolean keyRegistered;

	@SideOnly(Side.CLIENT)
	public static boolean canUse() {
		return keyRegistered;
	}

	@SideOnly(Side.CLIENT)
	public static boolean checkPlayerData(String player)//ClientProxy.preInit()
	{
		File keyFile = getKeyFile();
		if (keyFile.exists()) {
			String[] sa = NGTText.readText(keyFile);
			if (sa.length >= 2) {
				byte[] ba = Base64.decodeBase64(sa[1]);
				String s = new String(ba);
				if (player.equals(s) || (sa.length == 3 && sa[2] != null && sa[2].equals("develop_mode"))) {
					keyRegistered = true;
					return true;
				} else {
					NGTLog.debug("not matched player data : " + s);
				}
			} else {
				NGTLog.debug("illegal file");
			}
		}
		return false;
	}

	public static boolean registerKey(EntityPlayer player, String key) {
		List<String> strings = new ArrayList<String>();
		try {
			URL url = new URL("https://dl.dropboxusercontent.com/s/tukcqsaylqfhx7j/key.ngt");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String string;
			while ((string = br.readLine()) != null) {
				strings.add(string);
			}
			br.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			NGTLog.sendChatMessage(player, "message.regKey.0");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			NGTLog.sendChatMessage(player, "message.regKey.1");
			return false;
		}

		if (strings.size() > 0) {
			String[] sa = strings.toArray(new String[strings.size()]);
			if (sa[0] == null || sa[0].equals("unavailable")) {
				NGTLog.sendChatMessage(player, "message.regKey.2");
				return false;
			} else if (sa[0].equals("available")) {
				NGTLog.sendChatMessage(player, "message.regKey.3");
				return true;
			} else if (sa[0].equals(key)) {
				NGTLog.sendChatMessage(player, "message.regKey.4");
				return true;
			}
		}

		NGTLog.sendChatMessage(player, "message.regKey.5");
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static void writePlayerData(String player) {
		File keyFile = getKeyFile();
		if (keyFile.exists()) {
			keyFile.delete();
		}

		String s1 = Base64.encodeBase64String(NGTCore.metadata.version.getBytes());
		String s2 = Base64.encodeBase64String(player.getBytes());
		NGTText.writeToText(keyFile, s1 + "\n" + s2);

		keyRegistered = true;
		NGTCore.proxy.removeGuiWarning();
	}

	private static File getKeyFile() {
		File ngtDir = new File(NGTFileLoader.getModsDir().get(0), "ngt");
		if (!ngtDir.exists()) {
			ngtDir.mkdir();
		}
		return new File(ngtDir, "data.ngt");
	}
}