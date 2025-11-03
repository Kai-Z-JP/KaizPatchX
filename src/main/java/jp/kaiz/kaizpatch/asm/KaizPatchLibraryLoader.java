package jp.kaiz.kaizpatch.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class KaizPatchLibraryLoader implements IFMLLoadingPlugin {
    private static final String LIB_DIR = "libs";

    @Override
    public void injectData(Map<String, Object> data) {
        try {
            File coreModFile = (File) data.get("coremodLocation");
            File tmpDir = new File((File) data.get("mcLocation"), "mods/kaizpatch/libs");
            tmpDir.mkdirs();

            try (JarInputStream jis = new JarInputStream(coreModFile.toURI().toURL().openStream())) {
                JarEntry entry;
                while ((entry = jis.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (name.startsWith(LIB_DIR + "/") && name.endsWith(".jar")) {
                        File out = new File(tmpDir, new File(name).getName());
                        try (OutputStream os = Files.newOutputStream(out.toPath())) {
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = jis.read(buf)) != -1) os.write(buf, 0, r);
                        }
                        Launch.classLoader.addURL(out.toURI().toURL());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CoreMod library load failed", e);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
