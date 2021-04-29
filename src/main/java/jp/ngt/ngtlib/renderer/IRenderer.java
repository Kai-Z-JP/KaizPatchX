package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IRenderer {
    void startDrawing(int par1);

    int draw();

    void addVertexWithUV(float x, float y, float z, float u, float v);

    void setNormal(float x, float y, float z);

    void setBrightness(int par1);
}