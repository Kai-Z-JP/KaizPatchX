package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.IRenderer;
import jp.ngt.ngtlib.renderer.NGTTessellator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class GroupObject {
	public String name;
	public byte drawMode;
	public float smoothingAngle;
	public ArrayList<Face> faces = new ArrayList<>();

	public GroupObject(int par1) {
		this("", par1);
	}

	public GroupObject(String par1, int par2) {
		this.name = par1;
		this.drawMode = (byte) par2;
	}

	public void calcVertexNormals(VecAccuracy accuracy) {
		//頂点を共有している面のリストを格納
		Map<Vertex, List<Face>> faceMap = new HashMap<>(this.faces.size() * 4);

		for (Face face : this.faces) {
			if (face.faceNormal == null) {
				face.calculateFaceNormal(accuracy);
			}

			for (int i = 0; i < face.vertices.length; ++i) {
				if (!((i == 0) || (i == 1) || (i % 3 == 2))) {
					continue;//重複する頂点はパス
				}

				Vertex vtx = face.vertices[i];
				List<Face> list = faceMap.computeIfAbsent(vtx, k -> new ArrayList<>());

				if (!list.contains(face)) {
					list.add(face);
				}
			}
		}

		float angleCos = NGTMath.cos(this.smoothingAngle);//精度問題なし
		for (Face face : this.faces) {
			face.calcVertexNormals(faceMap, angleCos, accuracy);
		}
	}

	public void render(boolean smoothing) {
		if (this.faces.size() > 0) {
			NGTTessellator tessellator = NGTTessellator.instance;
			tessellator.startDrawing(this.drawMode);
			this.render(tessellator, smoothing);
			tessellator.draw();
		}
	}

	public void render(IRenderer tessellator, boolean smoothing) {
		if (this.faces.size() > 0) {
			for (Face face : faces) {
				face.addFaceForRender(tessellator, smoothing);
			}
		}
	}

	public GroupObject copy(String name) {
		GroupObject go = new GroupObject(name, this.drawMode);
		for (Face origFace : this.faces) {
			Face face = origFace.copy();
			go.faces.add(face);
		}
		return go;
	}

	protected final class FaceSet {
		public final Face face;
		public final int index;

		public FaceSet(Face p1, int p2) {
			this.face = p1;
			this.index = p2;
		}
	}
}