package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.EnumFace;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.block.BlockMirror.MirrorType;
import jp.ngt.rtm.util.DummyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderMirror extends TileEntitySpecialRenderer {
    public static final RenderMirror INSTANCE = new RenderMirror();

    /**
     * その描画ループ内でテクスチャ生成が完了してるかどうか
     */
    public boolean finishRender;
    private final DummyRenderer renderer;

    private RenderMirror() {
        this.renderer = new DummyRenderer(NGTUtilClient.getMinecraft());
    }

    private void render(TileEntityMirror par1, double par2, double par3, double par4, float par5) {
        if (par1.mirrors == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par3 + 0.5F, (float) par4 + 0.5F);
        GL11.glDepthMask(true);
        GLHelper.disableLighting();

        int alpha = par1.getAlpha();
        boolean doAlphaBlend = par1.mirrorType == MirrorType.Hexa_Cube && alpha < 255 && alpha > 0;
        if (doAlphaBlend) {
            GL11.glDepthMask(false);
            //GL11.glAlphaFunc(GL11.GL_LEQUAL, 1.0F);//a<=1.0
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            //テクスチャを透かす
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, (float) alpha / 255.0F);
        }

        for (int i = 0; i < par1.mirrors.length; ++i) {
            MirrorComponent mirror = par1.mirrors[i];
            if (!mirror.skipRender() && !mirror.mirrorObject.skipRender()) {
                int faceId = i;

                if (par1.mirrorType == MirrorType.Mono_Panel) {
                    EnumFace face = mirror.mirrorObject.face;
                    faceId = par1.getBlockMetadata();
                    GL11.glTranslatef(-0.9375F * face.normal[0], -0.9375F * face.normal[1], -0.9375F * face.normal[2]);
                }

                //this.bindTexture(TextureMap.locationBlocksTexture);
                mirror.mirrorObject.bindTexture();
                this.drawFace(MirrorFace.get(faceId), mirror.uMin, mirror.uMax, mirror.vMin, mirror.vMax);
                //par1.mirror.unbindTexture();
            }
            mirror.calledFromRenderer = true;
        }

        if (doAlphaBlend) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            //GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);//a>0.1
            GL11.glDepthMask(true);
        }

        GLHelper.enableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void drawFace(MirrorFace par1, float uMin, float uMax, float vMin, float vMax) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque_I(-1);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(par1.vertices[0].x, par1.vertices[0].y, par1.vertices[0].z, uMin, vMin);
        tessellator.addVertexWithUV(par1.vertices[1].x, par1.vertices[1].y, par1.vertices[1].z, uMin, vMax);
        tessellator.addVertexWithUV(par1.vertices[2].x, par1.vertices[2].y, par1.vertices[2].z, uMax, vMax);
        tessellator.addVertexWithUV(par1.vertices[3].x, par1.vertices[3].y, par1.vertices[3].z, uMax, vMin);
        tessellator.draw();
    }

    public void onRenderTickEnd() {
        this.finishRender = false;
    }

    public void update()//onClientTick()
    {
        if (this.finishRender) {
            return;
        }

        Minecraft mc = NGTUtilClient.getMinecraft();

        boolean b = mc.mcProfiler.profilingEnabled;
        mc.mcProfiler.profilingEnabled = false;
        MirrorObject.onTick(this.renderer);
        mc.mcProfiler.profilingEnabled = b;

        //this.mirrorObjects.clear();
        this.finishRender = true;
    }

    @Override
    public void renderTileEntityAt(TileEntity par1, double par2, double par3, double par4, float par5) {
        this.render((TileEntityMirror) par1, par2, par3, par4, par5);
    }
}