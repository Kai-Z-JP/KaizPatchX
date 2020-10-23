package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.renderer.IRenderer;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;

import java.util.List;
import java.util.Map;

/**
 * ポリゴンモデルの面
 */
@SideOnly(Side.CLIENT)
public class Face {
	private static final float[][] MIRROR_PATTERN = {
			{-1.0F, 1.0F, 1.0F},
			{1.0F, -1.0F, 1.0F},
			{1.0F, 1.0F, -1.0F}};

	/**
	 * マテリアルの番号(0~127)
	 */
	public final byte materialId;
	/**
	 * 頂点
	 */
	public final Vertex[] vertices;
	/**
	 * UV
	 */
	public final TextureCoordinate[] textureCoordinates;
	/**
	 * 面の法線ベクトル
	 */
	public Vertex faceNormal;
	/**
	 * 頂点の法線ベクトル
	 */
	public Vertex[] vertexNormals;

	public Face(int size, int material) {
		this.vertices = new Vertex[size];
		this.textureCoordinates = new TextureCoordinate[size];
		this.materialId = (byte) material;
	}

	public void addVertex(int index, Vertex v, TextureCoordinate t) {
		this.vertices[index] = v;
		this.textureCoordinates[index] = t;
	}

	public void calculateFaceNormal(VecAccuracy accuracy) {
		Vec3 vec = this.calcCrossProduct().normalize();
		this.faceNormal = Vertex.create(vec, accuracy);
	}

	public void calcVertexNormals(Map<Vertex, List<Face>> map, float angleCos, VecAccuracy accuracy) {
		this.vertexNormals = new Vertex[this.vertices.length];

		for (int i = 0; i < this.vertexNormals.length; ++i) {
			if (i > 2) {
				//重複頂点は法線算出を除外
				int i0 = i % 3;
				if (i0 == 0) {
					this.vertexNormals[i] = this.vertexNormals[0];
					continue;
				} else if (i0 == 1) {
					this.vertexNormals[i] = this.vertexNormals[i - 2];
					continue;
				}
			}

			Vec3 vec = this.faceNormal.toVec();
			Vertex vtx = this.vertices[i];
			List<Face> list = map.get(vtx);
			for (Face face : list) {
				if (face == this) {
					continue;
				}

				Vec3 v1 = this.faceNormal.toVec();
				Vec3 v2 = face.faceNormal.toVec();
				double d0 = v1.getAngleCos(v2);//dotでの比較は小さいポリゴンで破綻する
				if (d0 >= angleCos)//法線ベクトル同士の角がスムージング角より小さければ足す
				{
					double angle = v1.getAngle(v2);
					vec = vec.add(face.faceNormal.toVec());
				}
			}
			vec = vec.normalize();
			this.vertexNormals[i] = Vertex.create(vec, accuracy);
		}
	}

	/**
	 * 外積(各三角面の外積の合計)
	 */
	private Vec3 calcCrossProduct() {
		Vec3 vec = Vec3.ZERO;
		for (int i = 0; i < (this.vertices.length / 3); ++i)//各三角面の法線の平均を算出
		{
			int idx = i * 3;
			Vec3 v1 = PooledVec3.create(
					this.vertices[idx + 1].getX() - this.vertices[idx].getX(),
					this.vertices[idx + 1].getY() - this.vertices[idx].getY(),
					this.vertices[idx + 1].getZ() - this.vertices[idx].getZ());
			Vec3 v2 = PooledVec3.create(
					this.vertices[idx + 2].getX() - this.vertices[idx].getX(),
					this.vertices[idx + 2].getY() - this.vertices[idx].getY(),
					this.vertices[idx + 2].getZ() - this.vertices[idx].getZ());
			vec = vec.add(v1.crossProduct(v2).normalize());
			//vec = vec.add(v1.crossProduct(v2));
		}
		return vec;
	}

	public void addFaceForRender(IRenderer tessellator, boolean smoothing) {
		NGTRenderHelper.addFace(this, tessellator, smoothing);
	}

	/**
	 * @param type ミラー生成する軸(x:0, y:1, z:2)
	 */
	public Face getMirror(int type, Map<Vertex, Vertex> mirrorVertex, VecAccuracy accuracy) {
		int size = this.vertices.length;
		Face face = new Face(size, this.materialId);

		for (int i = 0; i < size; ++i) {
			Vertex vtx = this.vertices[i];
			Vertex vtx2;
			//張り方をi=0始点で補正
			int index = (i > 0) ? (size - i) : 0;

			if (mirrorVertex.containsKey(vtx)) {
				vtx2 = mirrorVertex.get(vtx);//元で共有してる頂点はミラー後も共有するように
			} else {
				float x = vtx.getX() * MIRROR_PATTERN[type][0];
				float y = vtx.getY() * MIRROR_PATTERN[type][1];
				float z = vtx.getZ() * MIRROR_PATTERN[type][2];
				//ミラー軸の値が0ならば、頂点を共有
				boolean flag = (type == 0 && x == 0.0F) || (type == 1 && y == 0.0F) || (type == 2 && z == 0.0F);
				vtx2 = flag ? vtx : Vertex.create(x, y, z, accuracy);
				mirrorVertex.put(vtx, vtx2);
			}

			face.addVertex(index, vtx2, this.textureCoordinates[i]);
		}

		return face;
	}

	/**
	 * DeepCopy, アウトラインモデル用
	 */
	public Face copy() {
		Face face = new Face(this.vertices.length, this.materialId);
		for (int i = 0; i < this.vertices.length; ++i) {
			face.addVertex(i, this.vertices[i].copy(VecAccuracy.MEDIUM), this.textureCoordinates[i].copy());
		}
		return face;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object instanceof Face) {
			Face face = (Face) object;
			if (this.vertices.length != face.vertices.length) {
				return false;
			}

			for (int i = 0; i < this.vertices.length; ++i) {
				if (!this.vertices[i].equals(face.vertices[i])) {
					return false;
				}
			}

			return true;
		}

		return false;
	}
}