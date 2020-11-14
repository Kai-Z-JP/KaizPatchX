package jp.ngt.ngtlib.io;

import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
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
        return texts.stream().map(text -> text.split(",")).toArray(String[][]::new);
    }

    //結合処理は早い(1ms以下)
    public static String append(List<String> list, boolean indention) {
        StringBuilder sb = new StringBuilder();
        if (indention) {
            list.forEach(s -> sb.append(s).append("\n"));
        } else {
            list.forEach(sb::append);
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
        stream.forEach(list::add);
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
                            strings = br.lines().collect(Collectors.toList());
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
                strings = br.lines().collect(Collectors.toList());
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return strings;
    }

    public static boolean writeToText(File file, String... texts) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
            Arrays.stream(texts).forEach(pw::println);
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
            return list.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }
}