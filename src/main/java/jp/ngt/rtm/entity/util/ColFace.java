package jp.ngt.rtm.entity.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.renderer.model.Face;
import jp.ngt.ngtlib.util.ColorUtil;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public final class ColFace {
    //最初から設定
    public Vec3[] vertices;
    public Vec3 normal;
    private Vec3[] vNormal;

    //後で算出
    public Vec3[] vtxExpanded;
    public Vec3 center;

    public int color = -1;

    public ColFace() {
    }

    @SideOnly(Side.CLIENT)
    public void setData(Face face) {
        int vtxCount = (face.vertices.length / 3) + 2;
        this.vertices = new Vec3[vtxCount];
        this.vNormal = new Vec3[vtxCount];
        for (int i = 0; i < vtxCount; ++i)//三角面を結合
        {
            int idx0 = i < 2 ? i : i + ((i - 2) * 2);
            this.vertices[i] = face.vertices[idx0].toVec();
            this.vNormal[i] = face.vertexNormals[idx0].toVec();
        }
        this.normal = face.faceNormal.toVec();
        this.init();
    }

    public void init() {
        double expand = 0.125D;
        int len = this.vertices.length;
        this.vtxExpanded = new Vec3[len];
        for (int i = 0; i < len; ++i) {
            Vec3 v0 = this.vertices[i];
            Vec3 v1 = this.vertices[(i + 1) % len];
            Vec3 v2 = this.vertices[(i + 2) % len];
            Vec3 v01n = v1.sub(v0).normalize();
            Vec3 v21n = v1.sub(v2).normalize();
            this.vtxExpanded[(i + 1) % len] = v1.add(v01n.multi(expand).add(v21n.multi(expand)));
        }

        Vec3 sum = Vec3.ZERO;
        for (Vec3 vec : this.vertices) {
            sum = sum.add(vec);
        }
        this.center = sum.multi(1.0D / (double) this.vertices.length);//重心
    }

    public ColFace rotateAroundX(float rotation) {
        ColFace face = this.copy();
        for (int i = 0; i < face.vertices.length; ++i) {
            face.vertices[i] = face.vertices[i].rotateAroundX(rotation);
        }
        return face;
    }

    public ColFace rotateAroundY(float rotation) {
        ColFace face = this.copy();
        for (int i = 0; i < face.vertices.length; ++i) {
            face.vertices[i] = face.vertices[i].rotateAroundY(rotation);
        }
        return face;
    }

    public ColFace copy() {
        ColFace face = new ColFace();
        face.vertices = new Vec3[this.vertices.length];
        for (int i = 0; i < this.vertices.length; ++i) {
            face.vertices[i] = this.vertices[i];
        }
        face.normal = this.normal;
        return face;
    }

    //collision////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return 接していないときはゼロベクトル
     */
    public Vec3 getCollisionVec(Vec3 sphereCenter, float r) {
        this.color = -1;
        Vec3 vZ = this.vertices[0];
        Vec3 vN = this.normal;
        Vec3 vQ = sphereCenter.sub(vZ);//面のv0を原点とする

        //球の中心Qを通り平面(法線N,点P)に垂直な直線と平面との交点X [X = Q - (Q･N)N]
        double dot = vQ.dotProduct(vN);
        Vec3 vX = vQ.sub(vN.multi(dot));

        //XQの長さが半径r以内か
        //boolean inRange = dot <= r;
        //Qが面の表側にあるか
        boolean isFrontSide = this.isSameDir(vQ.sub(vX), vN);
        if (isFrontSide)// && inRange)
        {
            //範囲判定:ポリゴンの各辺とXとの外積の向きがNと同じか
            Vec3 vZX = vZ.add(vX);

            boolean inFace = true;
            for (int i = 0; i < this.vtxExpanded.length; ++i) {
                Vec3 v0 = this.vtxExpanded[i];
                Vec3 v1 = this.vtxExpanded[(i + 1) % this.vtxExpanded.length];
                Vec3 cross = (v1.sub(v0)).crossProduct(vZX.sub(v0));
                boolean isSameDir = this.isSameDir(vN, cross);
                if (!isSameDir) {
                    inFace = false;
                    break;
                }
            }

            if (inFace) {
                this.color = 0x000000;
                return vZX;
                //return vN.multi(r - dot);//反射ベクトル
            }
        }

        return Vec3.ZERO;
    }

    /**
     * 2ベクトルの各成分の正負が同じか
     */
    private boolean isSameDir(Vec3 v1, Vec3 v2) {
		/*v1 = v1.normalize();
		v2 = v2.normalize();
		double absX = Math.abs(v1.getX() - v2.getX());
		double absY = Math.abs(v1.getY() - v2.getY());
		double absZ = Math.abs(v1.getZ() - v2.getZ());
		double limit = 0.25;//小さくすると大きいポリゴンの端の判定ができない
		return (absX < limit) && (absY < limit) && (absZ < limit);*/

        //return v1.crossProduct(v2).equals(Vec3.ZERO);//法線が同じ=外積がゼロ -> 使えない

        return v1.dotProduct(v2) > 0.0D;
    }

    /**
     * 面を包括するAABBを取得
     */
    public AxisAlignedBB toBox(Entity myself) {
        double minX = this.vertices[0].getX();
        double maxX = this.vertices[0].getX();
        double minY = this.vertices[0].getY();
        double maxY = this.vertices[0].getY();
        double minZ = this.vertices[0].getZ();
        double maxZ = this.vertices[0].getZ();
        for (Vec3 vtx : this.vertices) {
            //呼び出し元で回転適用済み
            //vtx = vtx.rotateAroundY(myself.rotationYaw);
            //vtx = vtx.rotateAroundX(myself.rotationPitch);
            minX = Math.min(vtx.getX(), minX);
            maxX = Math.max(vtx.getX(), maxX);
            minY = Math.min(vtx.getY(), minY);
            maxY = Math.max(vtx.getY(), maxY);
            minZ = Math.min(vtx.getZ(), minZ);
            maxZ = Math.max(vtx.getZ(), maxZ);
        }

        double thickness = 0.25D;

        if (maxX - minX < thickness) {
            if (this.normal.getX() >= 0.0D) {
                minX -= this.normal.getX() * thickness;
            } else {
                maxX += this.normal.getX() * thickness;
            }
        }

        if (maxY - minY < thickness) {
            if (this.normal.getY() >= 0.0D) {
                minY -= this.normal.getY() * thickness;
            } else {
                maxY += this.normal.getY() * thickness;
            }
        }

        if (maxZ - minZ < thickness) {
            if (this.normal.getZ() >= 0.0D) {
                minZ -= this.normal.getZ() * thickness;
            } else {
                maxZ += this.normal.getZ() * thickness;
            }
        }

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        double offsetY = (myself instanceof EntityVehicleBase) ? myself.getYOffset() : 0.0D;
        return aabb.offset(myself.posX, myself.posY + offsetY, myself.posZ);
    }

    //render////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SideOnly(Side.CLIENT)
    public void renderFrame(NGTTessellator tessellator, boolean mustRender) {
        int col = (mustRender && this.color < 0) ? 0 : this.color;

        if (col >= 0) {
            float lineWidth = (col > 0) ? 15.0F : 5.0F;

            //通常部分
            tessellator.setColorRGBA_I(col, 0xFF);
            for (int i = 0; i < this.vertices.length; ++i) {
                Vec3 v0 = this.vertices[i];
                tessellator.addVertex((float) v0.getX(), (float) v0.getY(), (float) v0.getZ());
                Vec3 v1 = this.vertices[(i + 1) % this.vertices.length];
                tessellator.addVertex((float) v1.getX(), (float) v1.getY(), (float) v1.getZ());
            }

            //拡張部分
			/*tessellator.setColorRGBA_I(0x808080, 0xFF);
			for(int i = 0; i < this.vtxExpanded.length; ++i)
			{
				Vec3 v0 = this.vtxExpanded[i];
				tessellator.addVertex((float)v0.getX(), (float)v0.getY(), (float)v0.getZ());
				Vec3 v1 = this.vtxExpanded[(i + 1) % this.vtxExpanded.length];
				tessellator.addVertex((float)v1.getX(), (float)v1.getY(), (float)v1.getZ());
			}*/

            GL11.glLineWidth(2.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderNormal(NGTTessellator tessellator) {
        this.renderNormal(tessellator, this.normal, this.center, 0x00FFFF);

        for (int i = 0; i < this.vNormal.length; ++i) {
            this.renderNormal(tessellator, this.vNormal[i], this.vertices[i], 0xFF00FF);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderNormal(NGTTessellator tessellator, Vec3 vec, Vec3 rootVec, int color) {
        final float scale = 0.125F;
        tessellator.setColorRGBA_I(color, 0xFF);
        tessellator.addVertex((float) rootVec.getX(), (float) rootVec.getY(), (float) rootVec.getZ());
        float x = (float) (rootVec.getX() + vec.getX() * scale);
        float y = (float) (rootVec.getY() + vec.getY() * scale);
        float z = (float) (rootVec.getZ() + vec.getZ() * scale);
        tessellator.addVertex(x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public void addColor(int value) {
        this.color = ColorUtil.add(this.color, value);
    }
}
