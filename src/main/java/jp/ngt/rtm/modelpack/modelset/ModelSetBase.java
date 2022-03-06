package jp.ngt.rtm.modelpack.modelset;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.rtm.entity.util.ColFace;
import jp.ngt.rtm.entity.util.CollisionObj;
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

    private CollisionObj collisionObj;
    private boolean syncFinished;

    /**
     * ダミー用
     */
    public ModelSetBase() {
        this.cfg = this.getDummyConfig();
        this.dataFormatter = new DataFormatter();
        this.isDummyModel = true;
    }

    public ModelSetBase(T par1) {
        this.cfg = par1;
        this.dataFormatter = new DataFormatter();
        this.isDummyModel = false;

        if (this.cfg.serverScriptPath != null) {
            this.serverSE = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(this.cfg.serverScriptPath));
        }

        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (this.cfg.guiScriptPath != null) {
                this.guiSE = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(this.cfg.guiScriptPath));
                this.guiTexture = ModelPackManager.INSTANCE.getResource(this.cfg.guiTexture);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void finishConstruct() {
        if (this instanceof IModelSetClient) {
            this.collisionObj = new CollisionObj(((IModelSetClient) this).getModelObject(), this.getConfig());
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

    //Side.SERVER
    public void addColFace(String partsName, ColFace face, byte status) {
        if (!this.syncFinished) {
            if (this.collisionObj == null) {
                this.collisionObj = new CollisionObj();
            }
            this.collisionObj.addColFace(partsName, face, status);
            this.syncFinished = (status == 2);
        }
    }

    public CollisionObj getCollisionObj() {
        return this.collisionObj;
    }
}