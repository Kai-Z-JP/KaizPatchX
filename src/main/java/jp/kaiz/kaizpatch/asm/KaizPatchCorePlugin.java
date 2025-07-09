package jp.kaiz.kaizpatch.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import java.util.Map;

@IFMLLoadingPlugin.Name("KaizPatchCorePlugin")
@MCVersion("1.7.10")
public class KaizPatchCorePlugin implements IFMLLoadingPlugin {
    @Override
    public void injectData(Map<String, Object> data) {
        new ModelPackLoader().injectData(data);
        new KaizPatchLibraryLoader().injectData(data);
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