package jp.ngt.ngtlib.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jp.kaiz.kaizpatch.util.MCFileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class NGTJson {
    public static String readFromJson(File file) {
        return MCFileUtil.readText(file);
    }

    public static void writeToJson(String json, File file) {
        try {
            //PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)));
            pw.println(json);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * jsonからオブジェクトを生成
     *
     * @throws NGTFileLoadException jsonの書式が不正な場合にスロー
     */
    public static Object getObjectFromJson(String json, Class<?> clazz) throws NGTFileLoadException {
        try {
            return getGson().fromJson(json, clazz);
        } catch (Exception e) {
            String message = "Can't load json : " + json + " (" + e.getMessage() + ")";
            throw new NGTFileLoadException(message, e);
        }
    }

    public static String getJsonFromObject(Object object) {
        return getGson().toJson(object);
    }

    private static Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }
}