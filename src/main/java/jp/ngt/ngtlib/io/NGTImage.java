package jp.ngt.ngtlib.io;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class NGTImage {
    public static int[] getARGBFromInt(int par1) {
        int a = par1 >> 24;
        int r = (par1 >> 16) & 0xFF;
        int g = (par1 >> 8) & 0xFF;
        int b = par1 & 0xFF;
        return new int[]{a, r, g, b};
    }

    public static int getIntFromARGB(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    /**
     * 明度
     */
    public static int getColorValue(int par1) {
        int[] argb = getARGBFromInt(par1);
        return Arrays.stream(argb, 1, argb.length).filter(i -> i >= 0).max().orElse(0);
    }

    /**
     * 補色
     */
    public static int getComplementaryColor(int par1) {
        int[] argb = getARGBFromInt(par1);
        int max = 0;
        int min = 255;
        for (int i = 1; i < argb.length; ++i) {
            if (max < argb[i]) {
                max = argb[i];
            }

            if (min > argb[i]) {
                min = argb[i];
            }
        }

        int i0 = max + min;
        return getIntFromARGB(argb[0], i0 - argb[1], i0 - argb[2], i0 - argb[3]);
    }

    @SideOnly(Side.CLIENT)
    public static class Thumbnail {
        private final BufferedImage image;
        private final int glID;

        public Thumbnail(BufferedImage source, int width, int height) {
            BufferedImage thumb = new BufferedImage(width, height, source.getType());
            Graphics2D g2d = thumb.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g2d.drawImage(source, 0, 0, width, height, null);

            this.image = thumb;
            this.glID = TextureUtil.glGenTextures();
            TextureUtil.uploadTextureImage(this.glID, thumb);
        }

        public void bindTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.glID);
        }

        public void deleteTexture() {
            GL11.glDeleteTextures(this.glID);
        }
    }
}