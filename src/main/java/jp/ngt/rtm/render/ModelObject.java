package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.MCModel;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.modelpack.ModelPackException;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.ModelConfig;
import jp.ngt.rtm.modelpack.cfg.ModelConfig.ModelSource;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * モデルデータとテクスチャを管理
 */
@SideOnly(Side.CLIENT)
public class ModelObject {
    /*ポリゴンモデル*/
    public final IModelNGT model;
    /*材質ごとのテクスチャ*/
    public final TextureSet[] textures;
    /*専用レンダラ*/
    public final PartsRenderer renderer;

    public final boolean light;
    public final boolean alphaBlend;
    private final boolean useTexture;

    public ModelObject(ModelSource par1, ModelSetBase par2, PartsRenderer par3, Object... args) {
        String filePath = par1.modelFile;
        this.model = ModelPackManager.INSTANCE.loadModel(filePath, GL11.GL_TRIANGLES, true, par2.getConfig());

        Material[] materials = this.getMaterials(this.getTextureMap(par1.textures));
        this.textures = new TextureSet[materials.length];
        boolean flag_l = false;
        boolean flag_a = false;
        int size = (materials.length != par1.textures.length) ? 1 : materials.length;
        for (int i = 0; i < size; ++i) {
            Material mat = materials[i];
            String[] sa = par1.textures[size == 1 ? 0 : mat.id];
            boolean flag0 = sa.length >= 3 && sa[2].contains("Light");
            boolean flag1 = sa.length >= 3 && sa[2].contains("AlphaBlend");
            int texSize = (flag0 ? 3 : 0);

            //独自定義のライト用テクスチャ名を使ってる場合
            String[] lightTextureNames = new String[sa.length >= 4 ? sa.length - 3 : 0];
            if (lightTextureNames.length > 0) {
                System.arraycopy(sa, 3, lightTextureNames, 0, lightTextureNames.length);
                texSize = lightTextureNames.length;
            }

            this.textures[mat.id] = new TextureSet(mat, texSize, flag1, lightTextureNames);
            flag_l |= flag0;
            flag_a |= flag1;
        }
        this.light = flag_l;
        this.alphaBlend = flag_a;
        this.useTexture = !(this.model.getType() == FileType.NGTO || this.model.getType() == FileType.NGTZ);

        if (this.textures[0] == null)//もしものため([0]で参照した場合)
        {
            this.textures[0] = new TextureSet(new Material((byte) 0, new ResourceLocation("hoge")), 0, false);
        }

        this.renderer = (par3 == null) ? this.getPartsRenderer(par1.rendererPath, this.model, args) : par3;
        this.renderer.init(par2, this);
    }

    /**
     * MissingModel用
     */
    public ModelObject(IModelNGT par1, TextureSet[] par2, ModelSetBase par3) {
        this.model = par1;
        this.textures = par2;
        this.light = false;
        this.alphaBlend = false;
        this.useTexture = true;

        this.renderer = this.getPartsRenderer(null, par1);
        this.renderer.init(par3, this);
    }

    private PartsRenderer getPartsRenderer(String path, IModelNGT par2, Object... args) {
        boolean b0 = !(args.length >= 1 && ("isBogie".equals(args[0])));
        if (path != null) {
            try {
                return PartsRenderer.getRendererWithScript(ModelPackManager.INSTANCE.getResource(path), String.valueOf(b0));
            } catch (Exception e) {
                throw new ModelPackException("On create renderer", path, e);
            }
        } else if (par2 instanceof MCModel) {
            return new MCModelRenderer(String.valueOf(b0));
        } else {
            return new BasicPartsRenderer();
        }
    }

    /**
     * モデル描画(通常はこれを使用)
     *
     * @param pass 0:通常, 1:透過、発光
     */
    public void render(Object entity, ModelConfig cfg, int pass, float par3) {
        GL11.glPushMatrix();

        this.renderer.preRender(entity, cfg.smoothing, cfg.doCulling, par3);

        if (!cfg.doCulling) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        if (cfg.smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        if (pass == 0) {
            this.renderWithTexture(entity, 0, par3);
        } else if (pass == 1) {
            //半透明
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.renderWithTexture(entity, 1, par3);
            GL11.glDisable(GL11.GL_BLEND);

            //発光
            GLHelper.disableLighting();
            GLHelper.setLightmapMaxBrightness();
            this.renderWithTexture(entity, 2, par3);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLHelper.enableLighting();
        }

        if (cfg.smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);

        this.renderer.postRender(entity, cfg.smoothing, cfg.doCulling, par3);

        GL11.glPopMatrix();
    }

    /**
     * スムージング、アルファブレンド等行わず
     */
    public void renderWithTexture(Object entity, int pass, float par3) {
        Arrays.stream(this.textures).filter(Objects::nonNull).forEach(texture -> {
            if (this.useTexture) {
                if (pass == 0) {
                    NGTUtilClient.bindTexture(texture.material.texture);
                } else if (pass == 1) {
                    if (!texture.doAlphaBlend) {
                        return;
                    }
                    NGTUtilClient.bindTexture(texture.material.texture);
                } else {
                    if (texture.subTextures == null) {
                        return;
                    }
                    NGTUtilClient.bindTexture(texture.subTextures[pass - 2]);
                }
            }
            this.renderer.currentMatId = texture.material.id;
            this.renderer.render(entity, pass, par3);
        });
    }

    public Material[] getMaterials(Map<String, String> map) {
        Map<String, Material> matMap = this.model.getMaterials();
        Material[] materials;
        if (matMap.isEmpty()) {
            materials = new Material[]{new Material((byte) 0, ModelPackManager.INSTANCE.getResource(map.get("default")))};
        } else {
            materials = new Material[matMap.size()];
            Iterator<Entry<String, Material>> iterator = matMap.entrySet().iterator();
            for (int i = 0; iterator.hasNext(); ++i) {
                Entry<String, Material> entry = iterator.next();
                String matName = map.get(entry.getKey());
                if (matName == null) {
                    matName = map.get("default");
                }
                materials[i] = new Material(entry.getValue().id, ModelPackManager.INSTANCE.getResource(matName));
            }
        }
        return materials;
    }

    protected Map<String, String> getTextureMap(String[][] par1) {
        return Arrays.stream(par1).collect(Collectors.toMap(sa -> sa[0], sa -> sa[1], (a, b) -> b));
    }
}