package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelFormatException;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class NGTOModel implements IModelNGT {
    public static final String GROUP_NAME = "default";

    private final NGTObject ngto;
    private final float scale;
    private DisplayList[] glLists;
    private NGTWorld world;

    private final ArrayList<GroupObject> parts = new ArrayList<>();
    private final Map<String, Material> materials = new HashMap<>();

    public NGTOModel(ResourceLocation par1, float par2) {
        this.ngto = this.loadModel(par1);
        if (this.ngto == null) {
            throw new ModelFormatException("Can't load NGTO");
        }
        this.materials.put(GROUP_NAME, new Material((byte) 0, TextureMap.locationBlocksTexture));
        this.scale = par2;
    }

    private NGTObject loadModel(ResourceLocation par1) {
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(par1);
            return NGTObject.load(res.getInputStream());
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model", e);
        }
    }

    @Override
    public void renderAll(boolean smoothing) {
        if (this.world == null) {
            if (NGTUtil.getClientWorld() == null) {
                return;
            }
            this.world = new NGTWorld(NGTUtil.getClientWorld(), this.ngto);
        }
        GL11.glPushMatrix();
        GL11.glScalef(this.scale, this.scale, this.scale);
        float x = (float) this.ngto.xSize * 0.5F;
        float z = (float) this.ngto.zSize * 0.5F;
        GL11.glTranslatef(-x, 0.0F, -z);
        int pass = MinecraftForgeClient.getRenderPass();
        if (pass == -1) {
            pass = 0;
        }
        NGTRenderer.renderTileEntities(this.world, 0.0F, pass);
        NGTRenderer.renderEntities(this.world, 0.0F, pass);
        this.renderBlocks(pass);
        GL11.glPopMatrix();
    }

    @Override
    public void renderOnly(boolean smoothing, String... groupNames) {
        if (groupNames.length == 1 && groupNames[0].equals(GROUP_NAME)) {
            this.renderAll(smoothing);
        }
    }

    @Override
    public void renderPart(boolean smoothing, String partName) {
        if (partName.equals(GROUP_NAME)) {
            this.renderAll(smoothing);
        }
    }

    private void renderBlocks(int pass) {
        if (this.glLists == null) {
            this.glLists = new DisplayList[2];
        }

        NGTUtilClient.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        boolean smoothing = NGTUtilClient.getMinecraft().gameSettings.ambientOcclusion != 0;
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }
        if (!GLHelper.isValid(this.glLists[pass])) {
            this.glLists[pass] = GLHelper.generateGLList();
            GLHelper.startCompile(this.glLists[pass]);
            NGTRenderer.renderNGTObject(this.world, this.ngto, true, 0, pass);
            GLHelper.endCompile();
        } else {
            GLHelper.callList(this.glLists[pass]);
        }
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLHelper.enableLighting();
        NGTUtilClient.getMinecraft().entityRenderer.enableLightmap(0.0D);
    }

    @Override
    public int getDrawMode() {
        return 0;
    }

    @Override
    public ArrayList<GroupObject> getGroupObjects() {
        return this.parts;
    }

    @Override
    public Map<String, Material> getMaterials() {
        return this.materials;
    }

    @Override
    public FileType getType() {
        return FileType.NGTO;
    }
}