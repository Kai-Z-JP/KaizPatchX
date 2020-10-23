package jp.ngt.ngtlib.util;

import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.event.ClickEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionChecker {
	private static final VersionChecker checker = new VersionChecker();

	private final List<PackInfo> checkList = new ArrayList<PackInfo>();
	/**
	 * {name, version, homepage}
	 */
	private final List<String[]> updateList = new ArrayList<String[]>();

	private boolean finished;

	/**
	 * 更新通知リストに追加
	 */
	public static void addToCheckList(PackInfo par1) {
		checker.checkList.add(par1);
	}

	public static void checkVersion() {
		Thread thread = checker.new VersionCheckThread();
		thread.start();
	}

	public static void sendUpdateMessage(ClientConnectedToServerEvent event) {
		if (checker.finished) {
			for (String[] sa : checker.updateList) {
				IChatComponent component = new ChatComponentTranslation("message.version", EnumChatFormatting.AQUA + sa[0]);
				component.appendText(" : " + EnumChatFormatting.GREEN + sa[1]);
				if (sa[2] != null && sa[2].length() > 0) {
					IChatComponent component2 = new ChatComponentTranslation("  §6§nDownload here");
					component2.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sa[2])));
					component.appendSibling(component2);
					//component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sa[2]));
				}
				event.handler.handleChat(new S02PacketChat(component));
			}
		}
	}

	public class VersionCheckThread extends Thread {
		public VersionCheckThread() {
			super("NGT Version Check");
		}

		@Override
		public void run() {
			List<String> strings = new ArrayList<String>();
			Map<String, String> latestVerMap = new HashMap<String, String>();//<name, ver>
			for (PackInfo info : checker.checkList) {
				if (latestVerMap.containsKey(info.name)) {
					String ver = latestVerMap.get(info.name);
					if (!info.version.equals(ver)) {
						checker.updateList.add(new String[]{info.name, ver, info.homepage});
						continue;
					}
				}

				String location = info.updateURL;
				if (location == null || location.length() == 0) {
					continue;
				}

				//HttpURLConnection connection = null;

				try {
			    	/*while((location != null) && (!location.isEmpty()))
				    {
				        URL url = new URL(location);
				        connection = (HttpURLConnection)url.openConnection();
				        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
				        connection.connect();
				        location = connection.getHeaderField("Location");
				    }

				    if(connection == null)
				    {
				    	NGTLog.debug("Failed to connect : " + info.name);
				        continue;
				    }

				    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));*/
					URL url = new URL(location);
					BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
					String string;
					while ((string = br.readLine()) != null) {
						strings.add(string);
					}
					br.close();

					//connection.disconnect();
				} catch (MalformedURLException e) {
					NGTLog.debug("URL:" + location);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String[] sa1 = strings.toArray(new String[strings.size()]);
				strings.clear();

				for (String s : sa1) {
					String[] sa2 = s.split(":");
					if (sa2.length == 2) {
						latestVerMap.put(sa2[0], sa2[1]);
						if (info.name.equals(sa2[0]) && !info.version.equals(sa2[1])) {
							checker.updateList.add(new String[]{info.name, sa2[1], info.homepage});
						}
					}
				}
			}
			checker.finished = true;
		}
	}
}