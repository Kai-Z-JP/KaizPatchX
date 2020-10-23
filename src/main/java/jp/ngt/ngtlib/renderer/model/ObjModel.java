package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import net.minecraftforge.client.model.ModelFormatException;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ForgeのWavefrontObjectがマルチスレッド未対応のためRTMなどではこちらを使用<br>
 * 四角ポリゴン対応済み
 */
@SideOnly(Side.CLIENT)
public final class ObjModel extends PolygonModel {
	public static final float SMOOTHING = 60.0F;

	/**
	 * 小数点含む必要あり
	 */
	private static final Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(v( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
	private static final Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
	private static final Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *$)");
	private static final Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
	private static final Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
	private static final Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
	private static final Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
	private static final Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d]+) *\\n)|([go]( [\\w\\d]+) *$)");

	private ArrayList<Vertex> vertexNormals;
	private ArrayList<TextureCoordinate> textureCoordinates;
	private Map<String, Material> materials;

	private byte currentMaterial;

	protected ObjModel(InputStream[] is, String name, VecAccuracy par2) throws ModelFormatException {
		super(is, name, GL11.GL_TRIANGLES, par2);
	}

	@Override
	protected void init(InputStream[] is) throws ModelFormatException {
		//呼び出し順序的に、ここで初期化しとかないといけない
		this.vertexNormals = new ArrayList<Vertex>();
		this.textureCoordinates = new ArrayList<TextureCoordinate>();

		InputStream is2 = null;
		if (is.length >= 2) {
			is2 = is[1];
		}
		this.materials = (new MtlParser(is2)).getMaterials();

		super.init(is);
	}

	@Override
	protected void parseLine(String currentLine, int lineCount) {
		if (currentLine.isEmpty()) {
			return;
		}
		//currentLine.startsWith("#")

		if (currentLine.startsWith("f ")) {
			if (this.currentGroupObject == null) {
				this.currentGroupObject = new GroupObject("Default", GL11.GL_TRIANGLES);
				this.currentGroupObject.smoothingAngle = SMOOTHING;
			}

			Face face = this.parseFace(currentLine, lineCount);

			if (face != null) {
				this.currentGroupObject.faces.add(face);
			}
		} else if (currentLine.startsWith("vt ")) {
			TextureCoordinate textureCoordinate = this.parseTextureCoordinate(currentLine, lineCount);
			if (textureCoordinate != null) {
				this.textureCoordinates.add(textureCoordinate);
			}
		} else if (currentLine.startsWith("v ")) {
			Vertex vertex = this.parseVertex(currentLine, lineCount);
			if (vertex != null) {
				this.vertices.add(vertex);
				this.calcSizeBox(vertex);
			}
		} else if (currentLine.startsWith("usemtl ")) {
			String[] sa = this.split(currentLine, ' ');
			Material mat = this.materials.get(sa[1]);
			if (mat != null) {
				this.currentMaterial = mat.id;
			}
		} else if (currentLine.startsWith("vn ")) {
			Vertex vertex = this.parseVertexNormal(currentLine, lineCount);
			if (vertex != null) {
				this.vertexNormals.add(vertex);
			}
		} else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
			GroupObject group = this.parseGroupObject(currentLine, lineCount);

			if (group != null) {
				if (this.currentGroupObject != null) {
					this.groupObjects.add(this.currentGroupObject);
				}
			}

			this.currentGroupObject = group;
			this.currentGroupObject.smoothingAngle = SMOOTHING;
		}
	}

	@Override
	protected void postInit() {
		this.groupObjects.add(this.currentGroupObject);
		this.vertexNormals.clear();
		this.textureCoordinates.clear();
	}

	/**
	 * 頂点生成
	 */
	private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
		if (isValidVertexLine(line)) {
			line = line.substring(line.indexOf(' ') + 1);
			String[] tokens = this.split(line, ' ');

			try {
				if (tokens.length == 2) {
					return Vertex.create(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), 0.0F, this.accuracy);
				} else if (tokens.length == 3) {
					return Vertex.create(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), this.accuracy);
				}
			} catch (NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	/**
	 * 頂点法線生成
	 */
	private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
		if (isValidVertexNormalLine(line)) {
			line = line.substring(line.indexOf(' ') + 1);
			String[] tokens = this.split(line, ' ');

			try {
				if (tokens.length == 3) {
					return Vertex.create(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), this.accuracy);
				}
			} catch (NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
		if (isValidTextureCoordinateLine(line)) {
			line = line.substring(line.indexOf(' ') + 1);
			String[] tokens = this.split(line, ' ');

			try {
				return TextureCoordinate.create(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]), this.accuracy);
			} catch (NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}
	}

	private Face parseFace(String line, int lineCount) throws ModelFormatException {
		if (isValidFaceLine(line)) {
			String trimmedLine = line.substring(line.indexOf(' ') + 1);
			String[] tokens = this.split(trimmedLine, ' ');

			if (tokens.length > 2) {
				return this.parsePolygon(line, tokens, lineCount);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}
		return null;
	}

	private Face parsePolygon(String line, String[] tokens, int lineCount) {
		byte type = getValidType(line);
		if (type >= 0) {
			int size = (tokens.length - 2) * 3;//三角面化時の頂点数
			Face face = new Face(size, this.currentMaterial);

			if (type == 0 || type == 2) {
				//face.vertexNormals = new Vertex[6];
			}

			for (int i = 0; i < size; ++i) {
				int index = (i % 3 == 0) ? 0 : (i / 3) + (i % 3);
				Vertex vertex = null;
				TextureCoordinate tex = null;

				if (type < 3) {
					String[] subTokens = null;

					if (type == 0 || type == 1) {
						subTokens = this.split(tokens[index], '/');
					} else if (type == 2) {
						subTokens = this.split(tokens[index], "//");
					}

					vertex = this.vertices.get(Integer.parseInt(subTokens[0]) - 1);

					if (type == 0 || type == 1) {
						tex = this.textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
					}

					if (type == 0 || type == 2) {
						//face.vertexNormals[i] = this.vertexNormals.get(Integer.parseInt(subTokens[type == 0 ? 2 : 1]) - 1);
					}
				} else {
					vertex = this.vertices.get(Integer.parseInt(tokens[index]) - 1);
				}

				face.addVertex(i, vertex, tex);
			}

			face.calculateFaceNormal(this.accuracy);
			return face;
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}
	}

	private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
		if (isValidGroupObjectLine(line)) {
			String trimmedLine = line.substring(line.indexOf(' ') + 1);
			if (trimmedLine.length() > 0) {
				return new GroupObject(trimmedLine, GL11.GL_TRIANGLES);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}
		return null;
	}

	private static boolean isValidVertexLine(String line) {
		return vertexPattern.matcher(line).matches();
	}

	private static boolean isValidVertexNormalLine(String line) {
		return vertexNormalPattern.matcher(line).matches();
	}

	private static boolean isValidTextureCoordinateLine(String line) {
		return textureCoordinatePattern.matcher(line).matches();
	}

	/***
	 * Verifies that the given line from the model file is a valid face that is described by vertices, texture coordinates, and vertex normals
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches the format "f v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
	 */
	private static boolean isValidFace_V_VT_VN_Line(String line) {
		return face_V_VT_VN_Pattern.matcher(line).matches();
	}

	/***
	 * Verifies that the given line from the model file is a valid face that is described by vertices and texture coordinates
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches the format "f v1/vt1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
	 */
	private static boolean isValidFace_V_VT_Line(String line) {
		return face_V_VT_Pattern.matcher(line).matches();
	}

	/***
	 * Verifies that the given line from the model file is a valid face that is described by vertices and vertex normals
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches the format "f v1//vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
	 */
	private static boolean isValidFace_V_VN_Line(String line) {
		return face_V_VN_Pattern.matcher(line).matches();
	}

	/***
	 * Verifies that the given line from the model file is a valid face that is described by only vertices
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches the format "f v1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
	 */
	private static boolean isValidFace_V_Line(String line) {
		return face_V_Pattern.matcher(line).matches();
	}

	/***
	 * Verifies that the given line from the model file is a valid face of any of the possible face formats
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
	 */
	private static boolean isValidFaceLine(String line) {
		return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
	}

	/**
	 * @return 0:V_VT_VN<br>
	 * 1:V_VT<br>
	 * 2:V_VN<br>
	 * 3:V<br>
	 * -1:一致せず
	 */
	private static byte getValidType(String line) {
		if (isValidFace_V_VT_VN_Line(line))// f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
		{
			return 0;
		} else if (isValidFace_V_VT_Line(line))// f v1/vt1 v2/vt2 v3/vt3 ...
		{
			return 1;
		} else if (isValidFace_V_VN_Line(line))// f v1//vn1 v2//vn2 v3//vn3 ...
		{
			return 2;
		} else if (isValidFace_V_Line(line))// f v1 v2 v3 ...
		{
			return 3;
		}
		return -1;
	}

	private static boolean isValidGroupObjectLine(String line) {
		return groupObjectPattern.matcher(line).matches();
	}

	@Override
	public FileType getType() {
		return FileType.OBJ;
	}

	@Override
	public int getDrawMode() {
		return GL11.GL_TRIANGLES;
	}

	@Override
	public Map<String, Material> getMaterials() {
		return this.materials;
	}
}