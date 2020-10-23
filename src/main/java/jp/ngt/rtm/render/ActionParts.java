package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.model.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ActionParts extends Parts {
	private static final float OUTLINE_THICKNESS = 0.005F;

	public final ActionType behavior;

	private GroupObject[] outlineModels;

	public int id;

	public ActionParts(ActionType par1, String... par2) {
		super(par2);
		this.behavior = par1;
	}

	public void init(PartsRenderer renderer) {
		super.init(renderer);
		setupOutlineModel(renderer);
	}

	private void setupOutlineModel(PartsRenderer renderer) {
		GroupObject[] objs = getObjects(renderer.modelObj.model);
		this.outlineModels = new GroupObject[objs.length];
		for (int i = 0; i < objs.length; i++) {
			GroupObject go = objs[i].copy("outline_" + i);
			go.smoothingAngle = 180.0F;
			go.calcVertexNormals(VecAccuracy.MEDIUM);
			for (int j = 0; j < go.faces.size(); j++) {
				Face face = go.faces.get(j);
				for (int k = 0; k < face.vertices.length; k++) {
					Vertex vNormal = face.vertexNormals[k].copy(VecAccuracy.MEDIUM);
					face.vertices[k] = face.vertices[k].add(vNormal.expand(0.005F));
				}
				face.calculateFaceNormal(VecAccuracy.MEDIUM);
			}
			this.outlineModels[i] = go;
		}
	}

	public void render(PartsRenderer renderer) {
		if (renderer.currentPass == RenderPass.PICK.id) {
			GL11.glLoadName(this.id);
			boolean smoothing = (renderer.modelSet.getConfig()).smoothing;
			IModelNGT model = renderer.modelObj.model;
			model.renderOnly(smoothing, this.objNames);
		} else {
			super.render(renderer);
		}
		boolean hit = equals(renderer.hittedActionParts);
		if (renderer.currentPass == RenderPass.LIGHT.id && hit) {
			int color = Mouse.isButtonDown(1) ? 16744448 : 16777215;
			renderOutline(renderer, color);
		}
	}

	private void renderOutline(PartsRenderer par1, int color) {
		GL11.glPushMatrix();
		GL11.glCullFace(1028);
		GLHelper.disableLighting();
		GL11.glDisable(3553);
		GLHelper.setColor(color, 255);
		for (GroupObject obj : this.outlineModels) {
			obj.render(false);
		}
		GLHelper.setColor(16777215, 255);
		GL11.glEnable(3553);
		GLHelper.enableLighting();
		GL11.glCullFace(1029);
		GL11.glPopMatrix();
	}

	public boolean ignoreMatId(PartsRenderer renderer) {
		return (renderer.currentPass == RenderPass.PICK.id);
	}
}
