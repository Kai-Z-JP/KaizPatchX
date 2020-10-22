package jp.ngt.ngtlib.util;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

import java.util.List;

@SideOnly(Side.CLIENT)
public final class NGTUtilClient {
	public static Minecraft getMinecraft() {
		return FMLClientHandler.instance().getClient();
	}

	public static void bindTexture(ResourceLocation par1) {
		getMinecraft().renderEngine.bindTexture(par1);
	}

	public static void checkGLError(String par1) {
		checkGLError(par1, false);
	}

	public static void checkGLError(String par1, boolean par2) {
		int i = GL11.glGetError();
		if (i != 0) {
			if (par2) {
				return;
			}
			NGTLog.debug("GL_ERROR" + "@" + par1);
			//NGTLog.debug(i + ": " + GLU.gluErrorString(i));
			NGTLog.debug(i + ": " + Util.translateGLErrorString(i));
			//GL43.glDebugMessageCallback(new KHRDebugCallback());
			//int program = GL20.glCreateProgram();
			//NGTLog.debug(GL20.glGetProgramInfoLog(program, 256));
			//NGTLog.debug(GL20.glGetShaderInfoLog(shader, maxLength));
		}
	}

	public static int getLightValue(World world, int x, int y, int z) {
		if (world.blockExists(x, 0, z)) {
			int sky = world.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
			int block = world.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
			return sky > block ? sky : block;
		}
		return 0;
	}

	static byte hasShader = -1;

	public static boolean usingShader() {
		if (hasShader < 0) {
			/*hasShader = 0;
			List<ModContainer> list = Loader.instance().getActiveModList();
			for(ModContainer container : list)
			{
				if(container.getModId().equals("shadersmod"))
				{
					hasShader = 1;break;
				}
			}*/

			hasShader = 0;
			List<String> list = CoreModManager.getLoadedCoremods();
			for (String name : list) {
				if (name.contains(NGTCore.shaderModName)) {
					hasShader = 1;
					break;
				}
			}
		}
		return hasShader == 1;
	}

	public static void playSound(ISound sound) {
		getMinecraft().getSoundHandler().playSound(sound);
		//getMinecraft().getSoundHandler().playDelayedSound(sound, 0);
	}
}