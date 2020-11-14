package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.io.NGTZ;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SideOnly(Side.CLIENT)
public class NGTZModel implements IModelNGT {
	private final List<NGTOParts> objects = new ArrayList();
	private final float scale;

	private final ArrayList<GroupObject> parts = new ArrayList<>();
	private final Map<String, Material> materials = new HashMap<>();

	public NGTZModel(ResourceLocation par1, float par2) {
		Map<String, NGTObject> objs = (new NGTZ(par1)).getObjects();
		objs.entrySet().stream().map(set -> new NGTOParts(set.getKey(), set.getValue())).forEach(this.objects::add);
		this.materials.put(NGTOModel.GROUP_NAME, new Material((byte) 0, TextureMap.locationBlocksTexture));
		this.scale = par2;
	}

	@Override
	public void renderAll(boolean smoothing) {
		this.objects.forEach(obj -> obj.render(this.scale));
	}

	@Override
	public void renderOnly(boolean smoothing, String... groupNames) {
		this.objects.forEach(obj -> Arrays.stream(groupNames).filter(s -> s.equals(obj.name)).forEach(s -> obj.render(this.scale)));
	}

	@Override
	public void renderPart(boolean smoothing, String partName) {
		this.objects.stream().filter(obj -> partName.equals(obj.name)).forEach(obj -> obj.render(this.scale));
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
		return FileType.NGTZ;
	}

	private static class NGTOParts {
		private final String name;
		private final NGTObject ngto;
		private DisplayList[] glLists;
		private NGTWorld world;

		public NGTOParts(String par1, NGTObject par2) {
			this.name = par1;
			this.ngto = par2;
		}

		public void render(float scale) {
			if (this.world == null) {
				if (NGTUtil.getClientWorld() == null) {
					return;
				}
				this.world = new NGTWorld(NGTUtil.getClientWorld(), this.ngto);
			}
			GL11.glPushMatrix();
			GL11.glScalef(scale, scale, scale);
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
	}
}