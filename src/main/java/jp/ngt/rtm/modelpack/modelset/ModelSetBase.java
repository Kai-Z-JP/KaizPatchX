package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.ModelConfig;
import jp.ngt.rtm.modelpack.state.DataFormatter;
import net.minecraft.util.ResourceLocation;

import javax.script.ScriptEngine;

public abstract class ModelSetBase<T extends ModelConfig> {
    protected final T cfg;
    public final DataFormatter dataFormatter;
    private final boolean isDummyModel;

    public ScriptEngine serverSE;

    @SideOnly(Side.CLIENT)
    public ScriptEngine guiSE;
    @SideOnly(Side.CLIENT)
    public ResourceLocation guiTexture;

    /**
     * ダミー用
     */
    public ModelSetBase() {
        this.cfg = this.getDummyConfig();
        this.dataFormatter = new DataFormatter(this.cfg);
        this.isDummyModel = true;
    }

    public ModelSetBase(T par1) {
        this.cfg = par1;
        this.dataFormatter = new DataFormatter(this.cfg);
        this.isDummyModel = false;

        if (this.cfg.serverScriptPath != null) {
            this.serverSE = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(this.cfg.serverScriptPath));
        }

        if (FMLCommonHandler.instance().getSide().isClient() && this.cfg.guiScriptPath != null) {
            this.guiSE = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(this.cfg.guiScriptPath));
            this.guiTexture = ModelPackManager.INSTANCE.getResource(this.cfg.guiTexture);
        }
    }

    public T getConfig() {
        return this.cfg;
    }

    public abstract T getDummyConfig();

    public boolean isDummy() {
        return this.isDummyModel;
    }

    protected ResourceLocation getSoundResource(String par1) {
        if (par1 != null && par1.length() > 0) {
            if (par1.contains(":")) {
                String[] sa = par1.split(":");
                return ModelPackManager.INSTANCE.getResource(sa[0], sa[1]);
            } else {
                return ModelPackManager.INSTANCE.getResource("rtm", par1);
            }
        }
        return null;
    }
}