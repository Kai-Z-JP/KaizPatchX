package jp.ngt.rtm.entity.npc.macro;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.entity.npc.macro.TrainCommand.CommandType;
import jp.ngt.rtm.entity.train.util.TrainState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MacroRecorder {
	private static final String MACRO_FOLDER = "rtm/train_macro";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	public static final MacroRecorder INSTANCE = new MacroRecorder();

	private final List<TrainCommand> commands = new ArrayList<TrainCommand>();
	private boolean recording;
	private long startTime;

	private MacroRecorder() {
	}

	public boolean start(World world) {
		if (this.recording) {
			return false;
		} else {
			this.recording = true;
			this.startTime = world.getWorldTime();
			this.commands.clear();
			NGTLog.sendChatMessage(NGTUtil.getClientPlayer(), "Start recording");
			return true;
		}
	}

	public boolean stop(World world) {
		if (!this.recording) {
			return false;
		} else {
			this.recording = false;
			this.startTime = 0L;
			this.saveToFile(NGTUtil.getClientPlayer());
			return true;
		}
	}

	private void setCommand(World world, CommandType type, Object param) {
		if (this.recording) {
			long time = world.getWorldTime() - this.startTime;
			this.commands.add(new TrainCommand(time, type, param));
		}
	}

	private void saveToFile(EntityPlayer player) {
		File saveFile = null;

		try {
			File macroFolder = this.getMacroFolder();
			String fileName = DATE_FORMAT.format(new Date());
			saveFile = new File(macroFolder, fileName + ".txt");
			saveFile.createNewFile();
			String[] texts = new String[this.commands.size()];
			for (int i = 0; i < texts.length; ++i) {
				TrainCommand command = this.commands.get(i);
				texts[i] = command.toString();
			}
			NGTText.writeToText(saveFile, texts);
			NGTLog.sendChatMessage(player, "Save macro : " + saveFile.getName());
		} catch (IOException e) {
			if (saveFile != null) {
				NGTLog.sendChatMessage(player, "Failed to save file : " + saveFile.getAbsolutePath());
			}
			e.printStackTrace();
		}
	}

	public boolean isRecording() {
		return this.recording;
	}

	public void recNotch(World world, int notch) {
		this.setCommand(world, CommandType.Notch, notch);
	}

	public void recHorn(World world) {
		this.setCommand(world, CommandType.Horn, "");
	}

	public void recChime(World world, String name) {
		this.setCommand(world, CommandType.Chime, name);
	}

	public void recDoor(World world, TrainState state) {
		this.setCommand(world, CommandType.Door, state);
	}

	/**
	 * 運転マクロ用フォルダ取得, なければ作成
	 */
	public File getMacroFolder() {
		File macroFolder = new File(NGTFileLoader.getModsDir().get(0), MACRO_FOLDER);
		if (!macroFolder.exists()) {
			macroFolder.mkdirs();
		}
		return macroFolder;
	}
}