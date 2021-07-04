package jp.ngt.ngtlib.io;

import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NGTFileLoader {
    public static final String NO_ZIP = "no_zip";
    private static final String[] EXCEPT_WORD = {"lang", "block", "item", "gui"};

    private static List<File> MODS_DIR;
    private static File PREV_OPENED_FOLDER;

    public static void log(String par1, Object... par2) {
        if (NGTCore.debugLog) {
            NGTLog.debug(par1, par2);
        }
    }

    /**
     * modsフォルダ以下にあるファイルを探す
     */
    public static List<File> findFile(FileMatcher matcher) {
        ScanResult result = findFile(new FileMatcher[]{matcher});
        return result.asList();
    }

    public static ScanResult findFile(FileMatcher... matchers) {
        ScanResult findFiles = new ScanResult();
        List<File> modsDir = getModsDir();
        modsDir.forEach(dir -> {
            log("[NGTFL] Set search path : " + dir.getAbsolutePath());
            findFileInDirectory(findFiles, dir, matchers);
        });
        return findFiles;
    }

    public static List<File> findFileInDirectory(File dir, FileMatcher matcher) {
        ScanResult result = new ScanResult();
        findFileInDirectory(result, dir, matcher);
        return result.asList();
    }

    /**
     * Map<ZipPath, Map<Matcher, List<File>>>
     */
    public static void findFileInDirectory(ScanResult result, File dir, FileMatcher... matchers) {
        String[] files = dir.list();

        if (files == null || files.length == 0) {
            //log("[NGTFL] There is no file in " + dir.getAbsolutePath());
            return;
        }

        Arrays.stream(files).map(entryName -> new File(dir, entryName)).forEach(entry -> {
            if (entry.isFile()) {
                String name = entry.getName();
                if (FileType.ZIP.match(name) || FileType.JAR.match(name)) {
                    findFileInZip(result, entry, "", matchers);
                } else {
                    Arrays.stream(matchers).filter(matcher -> matcher.match(entry)).forEach(matcher -> result.add(NO_ZIP, matcher, entry));
                }
            } else if (entry.isDirectory() && !isExeptFolder(entry)) {
                findFileInDirectory(result, entry, matchers);
            }
        });
    }

    private static void findFileInZip(ScanResult result, File archive, String encoding, FileMatcher... matchers) {
        log("[NGTFL] Scan zip : " + archive.getName());

        try {
            ZipFile zip = getArchive(archive, encoding);
            zip.stream()
                    .filter(x -> !x.isDirectory())
                    .map(zipEntry -> new File(zip.getName(), zipEntry.getName()))
                    .forEach(file -> Arrays.stream(matchers).filter(matcher -> matcher.match(file))
                            .forEach(matcher -> result.add(archive.getName(), matcher, file)));
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
            NGTLog.debug("[NGTFL] IOException:" + archive.getName());
        } catch (IllegalArgumentException e) {
            if (encoding.isEmpty()) {
                findFileInZip(result, archive, "MS932", matchers);//SJISで再読込
                return;
            }
            e.printStackTrace();
            NGTLog.debug("[NGTFL] IllegalArgumentException:" + archive.getName());
        }
    }

    /**
     * 次の除外対象フォルダに該当するか (lang, block, item, gui)
     */
    private static boolean isExeptFolder(File folder) {
        if (folder.getAbsolutePath().contains("sounds")) {
            return false;//sounds以下のblock, itemフォルダは除外しないため
        }

        return Arrays.stream(EXCEPT_WORD).anyMatch(word -> folder.getName().equals(word));
    }

    public static byte[] readBytes(File par1) throws IOException {
        InputStream is = new FileInputStream(par1);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int len; (len = is.read(buffer)) > 0; bout.write(buffer, 0, len)) {
        }
        is.close();
        return bout.toByteArray();
    }

    /**
     * modsフォルダの場所を取得
     *
     * @return modsフォルダの場所 (開発環境の場合、run以下とbin以下の2つ)
     */
    public static List<File> getModsDir()//cpw.mods.fml.common.Loader
    {
        if (MODS_DIR != null) {
            return MODS_DIR;
        }

        MODS_DIR = new ArrayList<>();

        boolean dev = false;
        try {
            //Dev環境以外ではぬるぽ
            File modsDir2 = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath());
            if (!modsDir2.getAbsolutePath().contains("mods")) {
                MODS_DIR.add(modsDir2);//開発環境でのMod本体のパス
                NGTLog.debug("[NGTFL] Add mods dir : " + modsDir2.getAbsolutePath());
                dev = true;
            }
        } catch (NullPointerException ignored) {
        }

        File modsDir = NGTCore.proxy.getMinecraftDirectory("mods");
        String modsDirPath = dev ? normalizePath(modsDir.getAbsolutePath()) : modsDir.getAbsolutePath();

        MODS_DIR.add(new File(modsDirPath));
        NGTLog.debug("[NGTFL] Add mods dir : " + modsDirPath);

        File jarInJarModDir = NGTCore.proxy.getMinecraftDirectory("jar-mods-cache/v1");
        MODS_DIR.add(new File(dev ? normalizePath(jarInJarModDir.getAbsolutePath()) : jarInJarModDir.getAbsolutePath()));
        NGTLog.debug("[NGTFL] Add jar-in-jar cache dir : " + modsDirPath);

        return MODS_DIR;
    }

    // cache pattern (String#replace compiles regex every time)
    private static final Pattern dotSlashModRegex
            = Pattern.compile(Pattern.quote(File.separator + "." + File.separator + "mods"));
    private static final String slashMod = getEscapedSeparator() + "mods";

    private static String getEscapedSeparator() {
        return File.separator.equals("\\") ? File.separator + File.separator : File.separator;
    }

    private static String normalizePath(String file) {
        return dotSlashModRegex.matcher(file).replaceAll(slashMod);
    }

    private static JFileChooser getCustomChooser(String title) {
        JFileChooser chooser = new JFileChooser(PREV_OPENED_FOLDER) {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setAlwaysOnTop(true);//常に前面に表示
                return dialog;
            }
        };

        chooser.setDialogTitle(title);
        chooser.requestFocusInWindow();
        return chooser;
    }

    //MCTE互換
    @Deprecated
    public static synchronized File selectFile(String[][] extensions) {
        FileType[] types = new FileType[extensions.length];
        for (int i = 0; i < extensions.length; ++i) {
            types[i] = FileType.getType(extensions[i][1]);
            if (types[i] == null) {
                return null;
            }
        }
        return selectFile(types);
    }

    //MCTE互換
    @Deprecated
    public static synchronized File saveFile(String[] extension) {
        FileType type = FileType.getType(extension[1]);
        if (type != null) {
            return saveFile(type);
        }
        return null;
    }

    /**
     * ファイル選択画面を開く
     *
     * @param types {ファイルの種類, 拡張子}
     */
    public static synchronized File selectFile(FileType... types) {
        final JFileChooser chooser = getCustomChooser("Select File");
        chooser.setAcceptAllFileFilterUsed(false);//フィルタ:全てのファイル
        Arrays.stream(types).map(type -> new FileNameExtensionFilter(type.getDescription(), type.getExtension())).forEach(chooser::addChoosableFileFilter);

        int state = chooser.showOpenDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            PREV_OPENED_FOLDER = file.getParentFile();
            return file;
        }

        return null;
    }

    /**
     * ファイル保存画面を開く
     *
     * @param types {{ファイルの種類, 拡張子}}
     */
    public static synchronized File saveFile(FileType... types) {
        final JFileChooser chooser = getCustomChooser("Save File");
        chooser.setAcceptAllFileFilterUsed(false);
        Arrays.stream(types).map(type -> new FileNameExtensionFilter(type.getDescription(), type.getExtension())).forEach(chooser::addChoosableFileFilter);

        int state = chooser.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            PREV_OPENED_FOLDER = file.getParentFile();
            if (!file.getName().contains("."))//拡張子がない場合
            {
                FileNameExtensionFilter filter = (FileNameExtensionFilter) chooser.getFileFilter();
                file = new File(file.getAbsolutePath() + "." + filter.getExtensions()[0]);
            }
            return file;
        }

        return null;
    }

    public static InputStream getInputStream(ResourceLocation par1) throws IOException {
        if (!NGTCore.proxy.isServer()) {
            return NGTUtilClient.getMinecraft().getResourceManager().getResource(par1).getInputStream();
        } else//Server用, ResourceManagerが使えないため
        {
            int index = par1.getResourcePath().lastIndexOf("/");
            String fileName = par1.getResourcePath().substring(index + 1);
            List<File> list = NGTFileLoader.findFile((file) -> file.getName().equals(fileName));
            if (list.isEmpty()) {
                throw new FileNotFoundException("On get stream : " + fileName);
            }
            File file = list.get(0);
            //return new FileInputStream(file);
            return getInputStreamFromFile(file);
        }
    }

    public static InputStream getInputStreamFromFile(File file) throws IOException {
        String suffix = getArchiveSuffix(file.getAbsolutePath());
        if (!suffix.isEmpty()) {
            return getStreamFromArchive(file, suffix);
        } else {
            return new FileInputStream(file);
        }
    }

    public static InputStream getStreamFromArchive(File file, String suffix) throws IOException {
        String zipPath = getArchivePath(file.getAbsolutePath(), suffix);
        ZipFile zip = getArchive(new File(zipPath), "");
        Enumeration<? extends ZipEntry> enu = zip.entries();
        while (enu.hasMoreElements()) {
            ZipEntry ze = enu.nextElement();
            if (!ze.isDirectory()) {
                File fileInZip = new File(zipPath, ze.getName());
                if (fileInZip.getName().equals(file.getName())) {
                    InputStream is = zip.getInputStream(ze);
                    return new BufferedInputStream(is);
                }
            }
        }
        zip.close();

        throw new FileNotFoundException("On get stream : " + file.getName());
    }

    public static String getArchivePath(String absPath, String suffix) {
        int index = absPath.indexOf(suffix);
        return absPath.substring(0, index + 4);
    }

    public static ZipFile getArchive(File file, String encoding) throws IOException {
        if (FileType.JAR.match(file.getName())) {
            return new JarFile(file.getAbsolutePath());
        } else if (FileType.ZIP.match(file.getName())) {
            return new ZipFile(file.getAbsolutePath(), Charset.forName(encoding.isEmpty() ? "UTF-8" : encoding));
        }
        return null;
    }

    public static String getArchiveSuffix(String absPath) {
        if (absPath.contains(".zip")) {
            return ".zip";
        } else if (absPath.contains(".jar")) {
            return ".jar";
        }
        return "";
    }

    public static File createTempFile(InputStream is, String name) throws IOException {
        //final File tempFile = File.createTempFile(prefix, suffix);
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final File tempFile = new File(tempDir, name);
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(is, out);
        return tempFile;
    }
}
