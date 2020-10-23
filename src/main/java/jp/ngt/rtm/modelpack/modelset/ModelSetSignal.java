package jp.ngt.rtm.modelpack.modelset;

import jp.ngt.rtm.modelpack.cfg.SignalConfig;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelSetSignal extends ModelSetBase<SignalConfig> {
	private static final Pattern signalPattern = Pattern.compile("S\\((.+?)\\)");
	private static final Pattern intervalPattern = Pattern.compile("I\\((.+?)\\)");
	private static final Pattern partsPattern = Pattern.compile("P\\((.+?)\\)");

	public final int maxSignalLevel;

	public ModelSetSignal() {
		super();
		this.maxSignalLevel = 0;
	}

	public ModelSetSignal(SignalConfig par1) {
		super(par1);

		int i = 0;
		if (par1.lights != null) {
			LightParts[] parts = parseLightParts(par1.lights);
			for (LightParts light : parts) {
				if (light.signalLevel > i) {
					i = light.signalLevel;
				}
			}
		} else {
			i = 6;
		}
		this.maxSignalLevel = i;
	}

	@Override
	public SignalConfig getDummyConfig() {
		return SignalConfig.getDummyConfig();
	}

	public static LightParts[] parseLightParts(String[] par1) {
		List<LightParts> list = new LinkedList<LightParts>();
		for (int i = 0; i < par1.length; ++i) {
			String s0 = getMatchedString(par1[i], signalPattern);
			int i0 = Integer.parseInt(s0);
			String s1 = getMatchedString(par1[i], intervalPattern);
			int i1 = Integer.parseInt(s1);
			String s2 = getMatchedString(par1[i], partsPattern);
			String[] sa = s2.split(" ");
			list.add(new LightParts(i0, i1, sa));
		}
		Collections.sort(list);//番号順にソート
		return list.toArray(new LightParts[list.size()]);
	}

	private static String getMatchedString(String par1, Pattern par2) {
		Matcher matcher = par2.matcher(par1);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	public static class LightParts implements Comparable<LightParts> {
		public final int signalLevel;
		public final int interval;
		public final String[] parts;

		public LightParts(int par1, int par2, String[] par3) {
			this.signalLevel = par1;
			this.interval = par2;
			this.parts = par3;
		}

		@Override
		public int compareTo(LightParts obj) {
			return this.signalLevel - obj.signalLevel;
		}
	}
}