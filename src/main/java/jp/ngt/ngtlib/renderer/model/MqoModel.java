package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import net.minecraftforge.client.model.ModelFormatException;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Metasequoiaのモデルデータ
 */
@SideOnly(Side.CLIENT)
public class MqoModel extends PolygonModel {
    private static final Pattern GROUP_PATTERN = Pattern.compile("\"(.+?)\"");
    private static final Pattern VERTEX_INDEX_PATTERN = Pattern.compile("V\\((.+?)\\)");
    private static final Pattern UV_PATTERN = Pattern.compile("UV\\((.+?)\\)");
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("M\\((.+?)\\)");

    private static final byte Type_Object = 0;
    private static final byte Type_Vertex = 1;
    private static final byte Type_Face = 2;
    private static final byte Type_Material = 3;
    private static final byte Type_Thumbnail = 4;

    private Map<String, Material> materials;
    private List<Vertex> currentVertices;
    private Map<Vertex, Vertex> mirrorVertex;

    private byte currentType = -1;
    private byte mirrorType = -1;

    protected MqoModel(InputStream[] is, String name, int mode, VecAccuracy par3) throws ModelFormatException {
        super(is, name, mode, par3);
    }

    @Override
    protected void init(InputStream[] is) throws ModelFormatException {
        //呼び出し順序的に、ここで初期化しとかないといけない
        this.materials = new HashMap<>();
        this.currentVertices = new ArrayList<>(256);
        this.mirrorVertex = new HashMap<>(256);

        super.init(is);
    }

    @Override
    protected void parseLine(String currentLine, int lineCount) {
        if (currentLine.isEmpty()) {
            return;
        }

        if (this.currentType >= 0) {
            if (currentLine.startsWith("}")) {
                this.currentType = -1;
            } else if (this.currentType == Type_Face) {
                if (this.currentGroupObject == null) {
                    this.currentGroupObject = new GroupObject("Default", this.drawMode);
                }

                Face face = this.parseFace(currentLine, lineCount);

                if (face != null) {
                    this.currentGroupObject.faces.add(face);
                    if (this.mirrorType >= 0) {
                        Face mirror = face.getMirror(this.mirrorType, this.mirrorVertex, this.accuracy);
                        this.currentGroupObject.faces.add(mirror);
                    }
                }
            } else if (this.currentType == Type_Vertex) {
                Vertex vertex = this.parseVertex(currentLine, lineCount);
                if (vertex != null) {
                    this.currentVertices.add(vertex);
                    this.calcSizeBox(vertex);
                }
            } else if (this.currentType == Type_Thumbnail) {
            } else if (this.currentType == Type_Material) {
                this.parseMaterial(currentLine, lineCount);
            }
        } else {
            if (currentLine.startsWith("vertex ")) {
                this.currentType = Type_Vertex;
                this.vertices.addAll(this.currentVertices);
                this.currentVertices.clear();
            } else if (currentLine.startsWith("face ")) {
                this.currentType = Type_Face;
            } else if (currentLine.startsWith("Material ")) {
                this.currentType = Type_Material;
            } else if (currentLine.startsWith("Object ")) {
                GroupObject group = this.parseGroupObject(currentLine, lineCount);

                if (this.currentGroupObject != null) {
                    this.groupObjects.add(this.currentGroupObject);
                }

                this.currentGroupObject = group;
                this.mirrorType = -1;
                this.mirrorVertex.clear();
            } else if (currentLine.startsWith("mirror_axis "))//{x,y,z}={1,2,4}
            {
                String[] sa = this.split(currentLine, ' ');
                int axis = Integer.parseInt(sa[1]);
                this.mirrorType = (byte) (axis == 1 ? 0 : (axis == 2 ? 1 : 2));
            } else if (currentLine.startsWith("facet ")) {
                String[] sa = this.split(currentLine, ' ');
                this.currentGroupObject.smoothingAngle = this.getFloat(sa[1]);
            } else if (currentLine.startsWith("Thumbnail ")) {
                this.currentType = Type_Thumbnail;
            }
        }
    }

    @Override
    protected void postInit() {
        this.groupObjects.add(this.currentGroupObject);
        this.vertices.addAll(this.currentVertices);
        this.currentVertices.clear();
        this.mirrorVertex.clear();
    }

    private void parseMaterial(String line, int lineCount) throws ModelFormatException {
        String[] tokens = this.split(line, ' ');
        if (tokens.length > 1) {
            String matName = tokens[0].replaceAll("\"", "");
            Material material = new Material((byte) this.materials.size(), null);
            this.materials.put(matName, material);
        } else {
            throw new ModelFormatException("Error parsing material ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "'");
        }
    }

    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        String[] tokens = this.split(line, ' ');
        try {
            if (tokens.length == 2) {
                return Vertex.create(this.getCorrectValue(tokens[0]), this.getCorrectValue(tokens[1]), 0.0F, this.accuracy);
            } else if (tokens.length == 3) {
                return Vertex.create(this.getCorrectValue(tokens[0]), this.getCorrectValue(tokens[1]), this.getCorrectValue(tokens[2]), this.accuracy);
            }
        } catch (NumberFormatException e) {
            throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
        }
        return null;
    }

    /**
     * 文字列を浮動小数に変換<br>
     * MQOでは単位がcmなのでmに補正
     */
    private float getCorrectValue(String s) {
        return this.getFloat(s) * 0.01F;//精度的にはDoubleでなくてもOK?
    }

    private @Nullable
    Face parseFace(String line, int lineCount) throws ModelFormatException {
        String[] tokens = this.split(line, ' ');
        String mat = this.getMaterial(line);
        int matId = mat.length() == 0 ? 0 : Integer.parseInt(mat);
        int vertexCount = Integer.parseInt(tokens[0]);

        if (vertexCount == 4 && this.drawMode == GL11.GL_QUADS) {
            return this.parseFaceQuads(line, (byte) matId, lineCount);
        } else {
            //点と線は除外
            return (vertexCount < 3) ? null : this.parsePolygon(line, (byte) matId, lineCount, vertexCount);
        }
    }

    /**
     * 四角ポリゴンの生成<br>
     * ※GL_QUAD限定
     */
    private Face parseFaceQuads(String line, byte matId, int lineCount) {
        Face face = new Face(4, matId);
        String vertexIndex = this.getVertexIndex(line);
        String[] vertexes = this.split(vertexIndex, ' ');
        String uv = this.getUV(line);
        String[] uvs = (uv.length() == 0) ? null : this.split(uv, ' ');

        IntStream.range(0, 4).forEach(i -> {
            Vertex vertex = this.currentVertices.get(Integer.parseInt(vertexes[i]));
            float u = (uvs == null) ? 0.0F : this.getFloat(uvs[i * 2]);
            float v = (uvs == null) ? 0.0F : this.getFloat(uvs[(i * 2) + 1]);
            TextureCoordinate tex = TextureCoordinate.create(u, v, this.accuracy);
            face.addVertex(3 - i, vertex, tex);
        });

        face.calculateFaceNormal(this.accuracy);
        return face;
    }

    private Face parsePolygon(String line, byte matId, int lineCount, int vertexCount) {
        if (this.drawMode != GL11.GL_TRIANGLES) {
            String msg = String.format("Error parsing face ('%s', line %d) in file '%s'", line, lineCount, this.fileName);
            throw new ModelFormatException(msg);
        }

        int size = (vertexCount - 2) * 3;//三角面化時の頂点数
        Face face = new Face(size, matId);

        String vertexIndex = this.getVertexIndex(line);
        String[] vertexes = this.split(vertexIndex, ' ');
        String uv = this.getUV(line);
        String[] uvs = (uv.length() == 0) ? null : this.split(uv, ' ');

        for (int i = 0; i < size; ++i) {
            int index = (i % 3 == 0) ? 0 : (i / 3) + (i % 3);
            index = (vertexCount - index) % vertexCount;//メタセコは面の張り方が逆(0,3,2,1)
            Vertex vertex = this.currentVertices.get(Integer.parseInt(vertexes[index]));
            float u = (uvs == null) ? 0.0F : this.getFloat(uvs[index * 2]);
            float v = (uvs == null) ? 0.0F : this.getFloat(uvs[(index * 2) + 1]);
            TextureCoordinate tex = TextureCoordinate.create(u, v, this.accuracy);
            face.addVertex(i, vertex, tex);
        }

        face.calculateFaceNormal(this.accuracy);
        return face;
    }

    private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        String s = this.getGroupObjectName(line);
        if (s != null && s.length() > 0) {
            return new GroupObject(s, this.drawMode);
        } else {
            throw new ModelFormatException("Error parsing object ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "'");
        }
    }

    private String getVertexIndex(String line) {
        return this.getMatchedString(VERTEX_INDEX_PATTERN.matcher(line));
    }

    private String getUV(String line) {
        return this.getMatchedString(UV_PATTERN.matcher(line));
    }

    private String getMaterial(String line) {
        return this.getMatchedString(MATERIAL_PATTERN.matcher(line));
    }

    /**
     * オブジェクト名を取得
     */
    private String getGroupObjectName(String line) {
        return this.getMatchedString(GROUP_PATTERN.matcher(line));
    }

    private String getMatchedString(Matcher matcher) {
        try {
            matcher.find();
            return matcher.group(1);
        } catch (IllegalStateException e) {
            //e.printStackTrace();
            return "";
        }
    }

    @Override
    public FileType getType() {
        return FileType.MQO;
    }

    @Override
    public Map<String, Material> getMaterials() {
        return this.materials;
    }
}