package jp.ngt.rtm.entity.npc.macro;

import jp.ngt.ngtlib.io.NGTLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainCommand {
	public static final String SEPARATOR = "//";
	private static final String FORMAT = "%s %s:%s";
	private static final Pattern PATTERN = Pattern.compile("([0-9]+)\\s+(.+):(.*)");

	public final long time;
	public final CommandType type;
	public final Object parameter;

	public TrainCommand(long par1, CommandType par2, Object par3) {
		this.time = par1;
		this.type = par2;
		this.parameter = par3;
	}

	public static TrainCommand parse(String par1) {
		String s = par1.split("#")[0];//コメント除去

		try {
			Matcher matcher = PATTERN.matcher(s);
			if (matcher.find()) {
				int count = matcher.groupCount();
				if (count == 2) {
					long t1 = Long.valueOf(matcher.group(1));
					CommandType type = CommandType.valueOf(matcher.group(2));
					return new TrainCommand(t1, type, "");
				} else if (count == 3) {
					long t1 = Long.valueOf(matcher.group(1));
					CommandType type = CommandType.valueOf(matcher.group(2));
					String param = matcher.group(3).replace(" ", "");//コメントあり時のスペース除去
					return new TrainCommand(t1, type, param);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		NGTLog.debug("Failed parse : " + par1);
		return null;
	}

	@Override
	public String toString() {
		return String.format(FORMAT, this.time, this.type.toString(), this.parameter.toString());
	}

	public enum CommandType {
		Notch,
		Horn,
		Chime,
		Door
	}
}