package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.io.NGTFileLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelFormatException;
import org.lwjgl.opengl.GL11;

import java.io.*;

@SideOnly(Side.CLIENT)
public final class ModelLoader {
	/**
	 * @param args {DrawMode(int), ModelConfig(Str)}
	 */
	public static PolygonModel loadModel(String path, VecAccuracy par1, Object... args) {
		return loadModel(new ResourceLocation("minecraft", path), par1, args);
	}

	public static PolygonModel loadModel(ResourceLocation resource, VecAccuracy par1, Object... args) {
		String fileName = resource.toString();

		try {
			InputStream is = NGTFileLoader.getInputStream(resource);

			if (FileType.OBJ.match(resource.getResourcePath())) {
				String mtlFileName = resource.getResourcePath().replaceAll(".obj", ".mtl");
				ResourceLocation mtlFile = new ResourceLocation(resource.getResourceDomain(), mtlFileName);
				InputStream is2 = null;
				try {
                    is2 = NGTFileLoader.getInputStream(mtlFile);
                } catch (IOException ignored) {
                }
				return loadModel(new InputStream[]{is, is2}, fileName, par1, args);
			} else {
				return loadModel(new InputStream[]{is}, fileName, par1, args);
			}
		} catch (IOException e) {
			throw new ModelFormatException("Failed to load model : " + fileName, e);
		}
	}

	public static PolygonModel loadModel(File file, VecAccuracy par1, Object... args) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));

			if (FileType.OBJ.match(file.getName())) {
				String mtlFileName = file.getName().replaceAll(".obj", ".mtl");
				File mtlFile = new File(file.getParentFile(), mtlFileName);
				InputStream is2 = null;
                try {
                    is2 = new BufferedInputStream(new FileInputStream(mtlFile));
                } catch (IOException ignored) {
                }
				return loadModel(new InputStream[]{is, is2}, file.getName(), par1, args);
			} else {
				return loadModel(new InputStream[]{is}, file.getName(), par1, args);
			}
		} catch (IOException e) {
			throw new ModelFormatException("Failed to load model : " + file.getName(), e);
		}
	}

	public static PolygonModel loadModel(InputStream[] is, String name, VecAccuracy par1, Object... args) {
		if (FileType.OBJ.match(name)) {
			return new ObjModel(is, name, par1);
		} else if (FileType.MQO.match(name)) {
			if (args.length > 0) {
				return new MqoModel(is, name, (Integer) args[0], par1);
			} else {
				return new MqoModel(is, name, GL11.GL_TRIANGLES, par1);
			}
		} else if (FileType.MQOZ.match(name)) {
			return new MqozModel(is, name, GL11.GL_TRIANGLES, par1);
		}
		/*else if(FileType.NPM.match(name))//1.12
		{
			EncryptedModel em = EncryptedModel.getInstance(is[0], (byte[])args[1]);
			return em.getModel(name, par1, args);
		}*/
		return null;
	}
}