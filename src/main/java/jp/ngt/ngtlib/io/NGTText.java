package jp.ngt.ngtlib.io;

import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NGTText {
	public static String getText(ResourceLocation resource, boolean indention) throws IOException {
		List<String> list = readText(resource);
		return append(list, indention);
	}

	public static List<String> readText(ResourceLocation resource) throws IOException {
		return readTextL(NGTFileLoader.getInputStream(resource), "");
	}

	public static String readText(File file, boolean indention, String encoding) throws IOException {
		return append(readText(file, encoding), indention);
	}

	public static List<String> readText(File file, String encoding) throws IOException {
		return readTextL(NGTFileLoader.getInputStreamFromFile(file), encoding);
	}

	/**
	 * 要素のnullチェック未実施
	 */
	public static String[][] readCSV(File file, String encoding) throws IOException {
		List<String> texts = readText(file, encoding);
		String[][] array = new String[texts.size()][];
		for (int i = 0; i < texts.size(); ++i) {
			array[i] = texts.get(i).split(",");
		}
		return array;
	}

	//結合処理は早い(1ms以下)
	public static String append(List<String> list, boolean indention) {
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s);
			if (indention) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static List<String> readTextL(InputStream is, String encoding) {
		List<String> list = new ArrayList<>();
		InputStreamReader isr;
		if (encoding == null || encoding.isEmpty()) {
			isr = new InputStreamReader(is);
		} else {
			try {
				isr = new InputStreamReader(is, encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				isr = new InputStreamReader(is);
			}
		}
		BufferedReader reader = new BufferedReader(isr);
		Stream<String> stream = reader.lines();
		stream.forEachOrdered(line -> {
			list.add(line);
		});
		stream.close();
		return list;
	}

	@Deprecated
	public static List<String> readTextL(File file, String encoding) {
		List<String> strings = new ArrayList<>();

		if (file.getAbsolutePath().contains(".zip")) {
			String path = file.getAbsolutePath();
			int index = path.indexOf(".zip");
			String zipPath = path.substring(0, index + 4);
			try {
				ZipFile zip = new ZipFile(zipPath);
				Enumeration<? extends ZipEntry> enu = zip.entries();
				while (enu.hasMoreElements()) {
					ZipEntry ze = enu.nextElement();
					if (!ze.isDirectory()) {
						File fileInZip = new File(zipPath, ze.getName());
						if (fileInZip.getName().equals(file.getName())) {
							InputStream is = zip.getInputStream(ze);
							BufferedInputStream bis = new BufferedInputStream(is);
							BufferedReader br = new BufferedReader(new InputStreamReader(bis));
							String string;
							while ((string = br.readLine()) != null) {
								strings.add(string);
							}
							br.close();
							break;
						}
					}
				}
				zip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				InputStreamReader isr;
				if (encoding.isEmpty()) {
					isr = new InputStreamReader(new FileInputStream(file));
				} else {
					isr = new InputStreamReader(new FileInputStream(file), encoding);
				}
				BufferedReader br = new BufferedReader(isr);
				String string;
				while ((string = br.readLine()) != null) {
					strings.add(string);
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return strings;
	}

	public static boolean writeToText(File file, String... texts) {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
			for (String s : texts) {
				pw.println(s);
			}
			pw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Deprecated
	public static String readText(File file, boolean indention) {
		try {
			return readText(file, indention, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Deprecated
	public static String[] readText(File file) {
		try {
			List<String> list = readText(file, "");
			return list.toArray(new String[list.size()]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String[0];
	}
}