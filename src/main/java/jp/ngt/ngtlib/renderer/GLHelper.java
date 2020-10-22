package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.Locker;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class GLHelper {
	public static final GLHelper INSTANCE = new GLHelper();
	public static final Locker LOCKER = new Locker();

	private List<DisplayList> activeGLLists = new ArrayList<DisplayList>();
	private List<DisplayList> deleteGLLists = new ArrayList<DisplayList>();

	private GLHelper() {
	}

	public static void clearGLList() {
		LOCKER.lock();
		//if(INSTANCE.deleteGLLists.size() > 16)
		{
			for (DisplayList dl : INSTANCE.deleteGLLists) {
				if (GL11.glIsList(dl.value))//RuntimeException防止
				{
					//GLAllocation.deleteDisplayLists(glList);
					GL11.glDeleteLists(dl.value, 1);
				}
			}
			NGTLog.debug("Clear " + INSTANCE.deleteGLLists.size() + " GL Lists");
			INSTANCE.deleteGLLists.clear();
		}
		LOCKER.unlock();
	}

	/**
	 * ディスプレイリストの再生成
	 */
	public static void initGLList() {
		clearGLList();

		LOCKER.lock();
		if (!INSTANCE.activeGLLists.isEmpty()) {
			for (DisplayList dl : INSTANCE.activeGLLists) {
				if (GL11.glIsList(dl.value)) {
					GL11.glDeleteLists(dl.value, 1);
					dl.value = 0;
				}
			}
			INSTANCE.activeGLLists.clear();

		}
		LOCKER.unlock();
	}

	public static void deleteGLList(DisplayList par1) {
		LOCKER.lock();
		if (par1 != null) {
			INSTANCE.activeGLLists.remove(par1);
			INSTANCE.deleteGLLists.add(par1);
		}
		LOCKER.unlock();
	}

	public static DisplayList generateGLList() {
		LOCKER.lock();
		DisplayList list = new DisplayList(GL11.glGenLists(1));
		INSTANCE.activeGLLists.add(list);
		LOCKER.unlock();
		return list;
	}

	public static boolean isValid(DisplayList par1) {
		return par1 != null && par1.value > 0;
	}

	public static void startCompile(DisplayList par1) {
		LOCKER.lock();
		GL11.glNewList(par1.value, GL11.GL_COMPILE);
	}

	public static void endCompile() {
		GL11.glEndList();
		LOCKER.unlock();
	}

	public static void callList(DisplayList par1) {
		GL11.glCallList(par1.value);
	}

	public static void setBrightness(int par1) {
		int x = par1 & 0xFFFF;
		int y = par1 >> 16;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) x, (float) y);
	}

	public static void setLightmapMaxBrightness() {
		//i%0x10000,i/0x10000
		//240より大きいとGeForce系で暗くなる?
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
	}

	public static void enableLighting() {
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public static void disableLighting() {
		GL11.glDisable(GL11.GL_LIGHTING);
	}
}