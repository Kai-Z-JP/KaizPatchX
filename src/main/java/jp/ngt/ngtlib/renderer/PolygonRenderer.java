package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * VBOを使わないTessellator<br>
 * ディスプレイリストとの併用推奨
 */
@SideOnly(Side.CLIENT)
public final class PolygonRenderer implements IRenderer {
    public static final PolygonRenderer INSTANCE = new PolygonRenderer();
    public static final float DIV_15 = 1.0F / 15.0F;

    private PolygonRenderer() {
    }

    @Override
    public void startDrawing(int par1) {
        GL11.glBegin(par1);
    }

    @Override
    public int draw() {
        GL11.glEnd();
        return 0;
    }

    @Override
    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        GL11.glTexCoord2f(u, v);
        GL11.glVertex3f(x, y, z);
    }

    @Override
    public void setNormal(float x, float y, float z) {
        GL11.glNormal3f(x, y, z);
    }

    @Override
    public void setBrightness(int par1) {
        GLHelper.setBrightness(par1);
    }
}