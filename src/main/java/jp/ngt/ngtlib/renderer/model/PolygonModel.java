package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.IRenderer;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import kotlin.Pair;
import net.minecraftforge.client.model.ModelFormatException;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SideOnly(Side.CLIENT)
public abstract class PolygonModel implements IModelNGT {
    protected String fileName;
    protected int drawMode;
    public VecAccuracy accuracy;
    public float[] sizeBox = new float[6];

    /**
     * 全ての頂点
     */
    public final List<Vertex> vertices = new ArrayList<>(1024);
    public final List<GroupObject> groupObjects = new ArrayList<>(16);
    protected GroupObject currentGroupObject;

    protected PolygonModel() {
    }

    protected PolygonModel(String name, int mode, VecAccuracy par3) {
        this.fileName = name;
        this.drawMode = mode;
        this.accuracy = par3;
    }

    public PolygonModel(InputStream[] is, String name, int mode, VecAccuracy par3) throws ModelFormatException {
        this.fileName = name;
        this.drawMode = mode;
        this.accuracy = par3;
        //NGTLog.startTimer();
        this.init(is);
        //NGTLog.stopTimer(name + ",init");
        //NGTLog.startTimer();
        this.calcVertexNormals();
        //NGTLog.stopTimer(name + ",calc");
        this.vertices.clear();
    }

    /**
     * @param is [0]:モデル本体, [1]:設定ファイル等(objで使用)
     */
    protected void init(InputStream[] is) throws ModelFormatException {
        this.loadModel(is[0]);
    }

    int lineCount;//スコープの関係でここに置く
    static Pattern repS = Pattern.compile("\\s+");//s.replaceAll()とやってること同じ

    //https://qiita.com/penguinshunya/items/353bb1c555f337b0cf6d
    private void loadModel(InputStream inputStream) {
        Pair<Charset, InputStream> pair = detectCharset(inputStream);
        inputStream = pair.component2();
        
        //readLine()より若干早い
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(inputStream, pair.component1())).lines()) {
            stream.forEach(this::preParse);
        }
        this.postInit();
        //String s = Files.lines(Paths.get(path), Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
    }

    // allocating huge array makes GC many times so cache them
    // but the buffer will be not necessary after finish loading models,
    // so it should be weak/soft reference.
    private static final ThreadLocal<SoftReference<byte[]>> buffer = new ThreadLocal<>();

    // windows-31j: Shift_JIS with Microsoft Extension. Also known as Microsoft Code Page 932
    private static final Charset[] tryingCharsets = new Charset[] {
            StandardCharsets.UTF_8,
            Charset.forName("windows-31j"),
    };

    private static byte[] getBuffer() {
        SoftReference<byte[]> ref = buffer.get();
        byte[] bytes = ref == null ? null : ref.get();
        if (bytes == null) {
            // 4 Mi bytes
            buffer.set(new SoftReference<>(bytes = new byte[1024 * 1024]));
        }
        return bytes;
    }

    // TODO: test
    private static Pair<Charset, InputStream> detectCharset(InputStream inputStream) {
        byte[] buf = getBuffer();
        int c = 0;
        try {
            // read bytes fully to buf.
            int i;
            while ((i = inputStream.read(buf, c, buf.length - c)) != -1) {
                c += i;
                if (c == buf.length)
                    break;
            }
        } catch (IOException e) {
            throw new ModelFormatException("On read file for charset detection", e);
        }
        // empty: any charset should return empty string so use default one
        if (c == 0) return new Pair<>(Charset.defaultCharset(), inputStream);
        InputStream returnInputStream = new SequenceInputStream(new ByteArrayInputStream(buf, 0, c), inputStream);
        for (Charset tryingCharset : tryingCharsets) {
            String s = new String(buf, 0, c, tryingCharset);
            // trim last few chars to not make error for last bytes
            s = s.substring(0, s.length() - 10);
            
            // No U+FFFD should mean no decoding error.
            if (s.indexOf('\ufffd') == -1)
                return new Pair<>(tryingCharset, returnInputStream);
        }
        // no charsets are valid: use UTF8
        return new Pair<>(StandardCharsets.UTF_8, returnInputStream);
    }

    //https://kujirahand.com/blog/index.php?Java%E3%81%A7%E3%83%86%E3%82%AD%E3%82%B9%E3%83%88%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%82%92%E8%AA%AD%E3%81%BF%E8%BE%BC%E3%82%80%E6%96%B9%E6%B3%95%E3%81%A7%E3%81%A9%E3%82%8C%E3%81%8C%E4%B8%80%E7%95%AA%E9%80%9F%E3%81%84
    /*private void loadModel2(InputStream inputStream)
    {
    	//stream使用より若干遅い
    	try
    	{
    		//行単位でなくバイナリ一括読み込み→Str変換
    		byte[] data = new byte[inputStream.available()];
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.read(data);
            bis.close();
            String s = new String(data, StandardCharsets.UTF_8);
            String[] sa = s.split("\n");
            for(int count = 0; count < sa.length; ++count)
            {
            	String line = sa[count].replaceAll("\\s+", " ").trim();//空白文字を置換
                this.parseLine(line, count + 1);
            }
            this.postInit();
    	}
    	catch(IOException e)
    	{
    		throw new ModelFormatException("On read lines", e);
    	}
    	//String s = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }*/

    protected abstract void parseLine(String currentLine, int lineCount);

    /**
     * 全ての行を読み込んだ後に呼ばれる
     */
    protected abstract void postInit();

    /**
     * 頂点法線ベクトルの設定
     */
    private void calcVertexNormals() {
        this.groupObjects.forEach(obj -> obj.calcVertexNormals(this.accuracy));
    }

    /**
     * サイズBox更新
     */
    protected final void calcSizeBox(Vertex vtx) {
        if (vtx.getX() < this.sizeBox[0]) {
            this.sizeBox[0] = vtx.getX();
        } else if (vtx.getX() > this.sizeBox[3]) {
            this.sizeBox[3] = vtx.getX();
        }

        if (vtx.getY() < this.sizeBox[1]) {
            this.sizeBox[1] = vtx.getY();
        } else if (vtx.getY() > this.sizeBox[4]) {
            this.sizeBox[4] = vtx.getY();
        }

        if (vtx.getZ() < this.sizeBox[2]) {
            this.sizeBox[2] = vtx.getZ();
        } else if (vtx.getZ() > this.sizeBox[5]) {
            this.sizeBox[5] = vtx.getZ();
        }
    }

    //1.12
    /*@Override
    public final float[] getSize()
    {
    	return this.sizeBox;
    }*/

    @Override
    public void renderAll(boolean smoothing) {
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawing(this.drawMode);
        this.tessellateAll(tessellator, smoothing);
        tessellator.draw();

        if (smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
    }

    public void tessellateAll(IRenderer tessellator, boolean smoothing) {
        this.groupObjects.forEach(groupObject -> groupObject.render(tessellator, smoothing));
    }

    @Override
    public void renderOnly(boolean smoothing, String... groupNames) {
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        this.groupObjects.forEach(groupObject -> Arrays.stream(groupNames).filter(groupName -> groupName.equalsIgnoreCase(groupObject.name)).forEach(groupName -> groupObject.render(smoothing)));

        if (smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
    }

    @Override
    public void renderPart(boolean smoothing, String partName) {
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        for (GroupObject groupObject : this.groupObjects) {
            if (partName.equalsIgnoreCase(groupObject.name)) {
                groupObject.render(smoothing);
                return;
            }
        }

        if (smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
    }

    @Override
    public int getDrawMode() {
        return this.drawMode;
    }

    @Override
    public List<GroupObject> getGroupObjects() {
        return this.groupObjects;
    }

    /////////////////////////////////////////////////////////////////////////////

    protected final float getFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e)//MQ3で"-NAN"が含まれる対策
        {
            return 0.0F;
        }
    }

    private final List<String> tempList = new ArrayList<>();

    //速さ変わらない?

    /**
     * 正規表現を使わないsplit
     */
    protected final String[] split(String target, char regex) {
        this.tempList.clear();
        int index = 0;
        while (index >= 0) {
            int nextHit = target.indexOf(regex, index);
            if (nextHit < 0)//次のマッチ箇所なし
            {
                this.tempList.add(target.substring(index));
                break;
            } else if (index < nextHit) {
                this.tempList.add(target.substring(index, nextHit));
            }
            index = nextHit + 1;
        }
        return this.tempList.toArray(new String[0]);
    }

    /**
     * 正規表現を使わないsplit
     */
    protected final String[] split(String target, String regex) {
        this.tempList.clear();
        int index = 0;
        while (index >= 0) {
            int nextHit = target.indexOf(regex, index);
            if (nextHit < 0)//次のマッチ箇所なし
            {
                this.tempList.add(target.substring(index));
                break;
            } else if (index < nextHit) {
                this.tempList.add(target.substring(index, nextHit));
            }
            index = nextHit + regex.length();
        }
        return this.tempList.toArray(new String[0]);
    }

    private void preParse(String line) {
        line = repS.matcher(line).replaceAll(" ").trim();//空白文字を置換
        this.parseLine(line, ++lineCount);
    }
}
