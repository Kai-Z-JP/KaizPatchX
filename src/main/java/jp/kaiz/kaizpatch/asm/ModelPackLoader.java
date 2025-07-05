package jp.kaiz.kaizpatch.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

@IFMLLoadingPlugin.Name("ModelPackLoader")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ModelPackLoader implements IFMLLoadingPlugin {

    private static File mcDir;

    @Override
    public void injectData(Map<String, Object> data) {
        mcDir = (File) data.get("mcLocation");
        File modelpackDir = new File(mcDir, "mods/modelpacks");
        if (modelpackDir.exists()) {
            File[] files = modelpackDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (files != null) {
                for (File zip : files) {
                    try {
                        Launch.classLoader.addURL(zip.toURI().toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
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
