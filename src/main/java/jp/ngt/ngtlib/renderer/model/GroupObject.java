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
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public final class GroupObject {
    public String name;
    public byte drawMode;
    public float smoothingAngle;
    public List<Face> faces = new ArrayList<>();

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

        //重複する頂点はパス
        this.faces.forEach(face -> {
            if (face.faceNormal == null) {
                face.calculateFaceNormal(accuracy);
            }
            IntStream.range(0, face.vertices.length).filter(i -> (i == 0) || (i == 1) || (i % 3 == 2)).mapToObj(i -> face.vertices[i]).map(vtx -> faceMap.computeIfAbsent(vtx, k -> new ArrayList<>())).filter(list -> !list.contains(face)).forEach(list -> list.add(face));
        });

        float angleCos = NGTMath.cos(this.smoothingAngle);//精度問題なし
        this.faces.forEach(face -> face.calcVertexNormals(faceMap, angleCos, accuracy));
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
            faces.forEach(face -> face.addFaceForRender(tessellator, smoothing));
        }
    }

    public GroupObject copy(String name) {
        GroupObject go = new GroupObject(name, this.drawMode);
        this.faces.stream().map(Face::copy).forEach(face -> go.faces.add(face));
        return go;
    }

    protected static final class FaceSet {
        public final Face face;
        public final int index;

        public FaceSet(Face p1, int p2) {
            this.face = p1;
            this.index = p2;
        }
    }
}