package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.ModelPackManager;
import net.minecraft.util.ResourceLocation;

/**
 * Material + ライト用テクスチャ
 */
@SideOnly(Side.CLIENT)
public class TextureSet {
	public final Material material;
	public final ResourceLocation[] subTextures;
	public final boolean doAlphaBlend;

	public TextureSet(Material par1, int subTexturesSize, boolean par3Alpha, String... args) {
		this.material = par1;
		this.doAlphaBlend = par3Alpha;

		if (subTexturesSize > 0) {
			this.subTextures = new ResourceLocation[subTexturesSize];
			String textureName = par1.texture.getResourcePath();
			int index = textureName.indexOf(".png");
			for (int i = 0; i < subTexturesSize; ++i) {
				if (args.length > 0) {
					this.subTextures[i] = new ResourceLocation(args[i]);
				} else {
					String s = new StringBuilder(textureName).insert(index, "_light" + i).toString();
					this.subTextures[i] = ModelPackManager.INSTANCE.getResource(s);
				}
			}
		} else {
			this.subTextures = null;
		}
	}
}