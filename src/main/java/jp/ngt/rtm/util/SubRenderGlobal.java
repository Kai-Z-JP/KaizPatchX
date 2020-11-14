package jp.ngt.rtm.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.block.tileentity.MirrorObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * RenderGlobalの代替
 */
@SideOnly(Side.CLIENT)
public final class SubRenderGlobal {
	private final MirrorObject mirror;
	private final List<WorldRenderer> watchableRenderers = new LinkedList<>();
	/*private int posX;
	private int posY;
	private int posZ;*/

	public SubRenderGlobal(MirrorObject par1) {
		this.mirror = par1;
		/*this.posX = par1.blockX;
		this.posY = par1.blockY;
		this.posZ = par1.blockZ;//(par1.blockZ >> 4) << 4;*/
	}

	public void renderBlocks(ICamera cam, int pass) {
		Minecraft mc = NGTUtilClient.getMinecraft();
		mc.entityRenderer.enableLightmap(pass);

		WorldRenderer[] array = getRenderers(mc.renderGlobal);
		Arrays.stream(array).forEach(renderer -> this.callWorldRenderer(renderer, pass));

		mc.entityRenderer.disableLightmap(pass);

		//mc.renderGlobal.renderAllRenderLists(0, 0.0D);
	}

	private void callWorldRenderer(WorldRenderer renderer, int pass) {
		/*int x = (renderer.posX >> 4) - (this.posX >> 4);
		int y = (renderer.posY >> 4) - (this.posY >> 4);
		int z = (renderer.posZ >> 4) - (this.posZ >> 4);
		float[] normal = this.mirror.face.normal;
		int i0 = x * (int)normal[0] + y * (int)normal[1] + z * (int)normal[2];
		if(i0 < 0){return;}*/

		int gl = renderer.getGLCallListForPass(pass);//視野内にあるかここで確認済み
		if (gl < 0) {
			return;
		}

		EntityLivingBase viewer = this.mirror.getViewer();
		this.callLists(gl, (float) (renderer.posXMinus - viewer.posX), (float) (renderer.posYMinus - viewer.posY), (float) (renderer.posZMinus - viewer.posZ));
	}

	private void callLists(int gl, float x, float y, float z) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		GL11.glCallList(gl);
		GL11.glPopMatrix();
	}

	public static WorldRenderer[] getRenderers(RenderGlobal renderGlobal) {
		return (WorldRenderer[]) NGTUtil.getField(RenderGlobal.class, renderGlobal, "worldRenderers", "field_72765_l");
	}
}