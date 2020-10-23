package jp.ngt.rtm.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public final class RenderUtil {
	private static final FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);

	public static void enableCustomLighting(int id, float x, float y, float z, float r, float g, float b) {
		int light = getLight(id);
		if (light < 0) {
			return;
		}

		GL11.glDisable(GL11.GL_LIGHT0);//上からの光
		GL11.glDisable(GL11.GL_LIGHT1);
		//NGTUtilClient.getMinecraft().entityRenderer.disableLightmap(0.0D);

		if (r < 0.0F) {
			long time = NGTUtil.getClientWorld().getWorldInfo().getWorldTime();
			int t2 = (int) ((double) time * 15.0D);
			int hue = t2 % 360;
			switch (hue / 60) {
				case 0:
					r = 1.0F;
					g = ((float) (hue) / 60.0F);
					b = 0.0F;
					break;
				case 1:
					r = ((float) (120 - hue) / 60.0F);
					g = 1.0F;
					b = 0.0F;
					break;
				case 2:
					r = 0.0F;
					g = 1.0F;
					b = ((float) (120 - hue) / 60.0F);
					break;
				case 3:
					r = 0.0F;
					g = ((float) (240 - hue) / 60.0F);
					b = 1.0F;
					break;
				case 4:
					r = ((float) (hue - 240) / 60.0F);
					g = 0.0F;
					b = 1.0F;
					break;
				case 5:
					r = 1.0F;
					g = 0.0F;
					b = ((float) (360 - hue) / 60.0F);
					break;
			}
		}

		GL11.glEnable(light);
		//方向
		//GL11.glLight(light, GL11.GL_SPOT_DIRECTION, setColorBuffer(0.0F, -1.0F, 0.0F, 1.0F));
		//位置
		GL11.glLight(light, GL11.GL_POSITION, setColorBuffer(x, y, z, 1.0F));
		//拡散光
		float dif = 1.0F;//0.6
		GL11.glLight(light, GL11.GL_DIFFUSE, setColorBuffer(r, g, b, 1.0F));
		//環境光
		float amb = 0.0F;//0.2
		GL11.glLight(light, GL11.GL_AMBIENT, setColorBuffer(amb, amb, amb, 1.0F));
		//反射光
		float spe = 0.0F;//1.0
		GL11.glLight(light, GL11.GL_SPECULAR, setColorBuffer(spe, spe, spe, 1.0F));
		//GL11.glLight(light, GL11.GL_SPECULAR, setColorBuffer(r, g, b, 1.0F));

		//一定減衰率
		//GL11.glLightf(GL11.GL_LIGHT3, GL11.GL_CONSTANT_ATTENUATION, 1.0F);
		//線形減衰率
		//GL11.glLightf(GL11.GL_LIGHT3, GL11.GL_LINEAR_ATTENUATION, 0.05F);
		//2次減衰率
		//GL11.glLightf(GL11.GL_LIGHT3, GL11.GL_QUADRATIC_ATTENUATION, 0.05F);
	}

	public static void disableCustomLighting(int id) {
		int light = getLight(id);
		if (light < 0) {
			return;
		}
		GL11.glDisable(light);

		//NGTUtilClient.getMinecraft().entityRenderer.enableLightmap(0.0D);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		//RenderHelper.enableStandardItemLighting();
	}

	private static int getLight(int id) {
		switch (id) {
			case 0:
				return GL11.GL_LIGHT4;
			case 1:
				return GL11.GL_LIGHT5;
			case 2:
				return GL11.GL_LIGHT6;
			case 3:
				return GL11.GL_LIGHT7;
		}
		return -1;
	}

	private static FloatBuffer setColorBuffer(float x, float y, float z, float w) {
		colorBuffer.clear();
		colorBuffer.put(x).put(y).put(z).put(w);
		colorBuffer.flip();
		return colorBuffer;
	}
}