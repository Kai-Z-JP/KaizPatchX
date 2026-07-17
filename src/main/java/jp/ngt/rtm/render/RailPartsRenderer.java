package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.IRenderer;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.renderer.PolygonRenderer;
import jp.ngt.ngtlib.renderer.model.Face;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RailPartsRenderer extends TileEntityPartsRenderer<ModelSetRailClient> {
    private static final int BRIGHTNESS_PENDING_RENDER_KEY = Integer.MIN_VALUE;
    protected int currentRailIndex;
    private final FloatBuffer convBuf;
    private boolean isCompilingStaticRail;
    private boolean renderedStaticGeometry;

    public RailPartsRenderer(String... par1) {
        super(par1);
        this.convBuf = FloatBuffer.wrap(new float[]{
                1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
    }

    @Override
    public void init(ModelSetRailClient par1, ModelObject par2) {
        super.init(par1, par2);
    }

    /**
     * RenderLargeRailから呼ばれる
     */
    public void renderRail(TileEntityLargeRailCore tileEntity, int index, double par2, double par4, double par6, float par8) {
        try {
            this.currentRailIndex = index;
            this.renderRailStatic(tileEntity, par2, par4, par6, par8);

            this.renderRailDynamic(tileEntity, par2, par4, par6, par8);
        } catch (Exception e) {
            throw new RuntimeException("On init script : " + this.modelSet.getConfig().getName(), e);
        }
    }

    /*スクリプト呼び出しメソッド**********************************************************************************/

    /**
     * 形状固定の部分を描画
     */
    protected void renderRailStatic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
        boolean hasGLList = this.prepareStaticDisplayList(tileEntity);
        boolean brightnessReady = true;
        boolean refreshRequested = tileEntity.shouldRerenderRail
                || tileEntity.getStaticRenderKey(this.currentRailIndex) == BRIGHTNESS_PENDING_RENDER_KEY;
        if (hasGLList && refreshRequested) {
            brightnessReady = this.areRailBrightnessPositionsLoaded(tileEntity);
            if (brightnessReady && !this.shouldRefreshDisplayList(tileEntity)) {
                tileEntity.shouldRerenderRail = false;
                refreshRequested = false;
            }
        }

        if (!hasGLList || (refreshRequested && brightnessReady)) {
            this.compileStaticRailParts(tileEntity, x, y, z, par8);
            hasGLList = GLHelper.isValid(tileEntity.glLists[this.currentRailIndex]);
        }

        if (hasGLList) {
            this.renderStaticDisplayList(tileEntity, x, y, z);
        }
    }

    /**
     * 形状が変化する部分を描画
     */
    protected void renderRailDynamic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
        ScriptUtil.doScriptFunction(this.script, "renderRailDynamic", tileEntity, x, y, z, par8, 0);
    }

    /**
     * オブジェクトを描画するかどうか
     *
     * @param objName
     * @param len     セグメント総数
     * @param pos     何番目のセグメントか
     */
    protected boolean shouldRenderObject(TileEntityLargeRailCore tileEntity, String objName, int len, int pos) {
        return (Boolean) ScriptUtil.doScriptFunction(this.script, "shouldRenderObject", tileEntity, objName, len, pos);
    }

    /****************************************************************************************************/

    /**
     * 固定パーツをGLListで描画<br>
     * スクリプトから呼び出し
     */
    public void renderStaticParts(TileEntityLargeRailCore tileEntity, double par2, double par4, double par6) {
        if (this.isCompilingStaticRail) {
            this.renderStaticPartsGeometry(tileEntity);
            return;
        }

        boolean hasGLList = this.prepareStaticDisplayList(tileEntity);
        boolean brightnessReady = true;
        boolean refreshRequested = tileEntity.shouldRerenderRail
                || tileEntity.getStaticRenderKey(this.currentRailIndex) == BRIGHTNESS_PENDING_RENDER_KEY;
        if (hasGLList && refreshRequested) {
            brightnessReady = this.areRailBrightnessPositionsLoaded(tileEntity);
            if (brightnessReady && !this.shouldRefreshDisplayList(tileEntity)) {
                tileEntity.shouldRerenderRail = false;
                refreshRequested = false;
            }
        }

        if (!hasGLList || (refreshRequested && brightnessReady)) {
            GLHelper.startCompile(tileEntity.glLists[this.currentRailIndex]);//GL_COMPILE_AND_EXECUTEは画面がチラつく
            try {
                hasGLList = this.renderStaticPartsGeometry(tileEntity);
            } finally {
                GLHelper.endCompile();
            }
        }

        if (hasGLList) {
            this.renderStaticDisplayList(tileEntity, par2, par4, par6);
        }
    }

    private boolean prepareStaticDisplayList(TileEntityLargeRailCore tileEntity) {
        int size = tileEntity.subRails.size() + 1;
        boolean hasGLList = true;
        if (tileEntity.glLists != null && tileEntity.glLists.length != size) {
            Arrays.stream(tileEntity.glLists).forEach(GLHelper::deleteGLList);
            hasGLList = false;
        }
        tileEntity.ensureRenderCacheCapacity();

        if (hasGLList) {
            hasGLList = GLHelper.isValid(tileEntity.glLists[this.currentRailIndex]);
        }

        if (!hasGLList) {
            tileEntity.glLists[this.currentRailIndex] = GLHelper.generateGLList(tileEntity.glLists[this.currentRailIndex]);
        }

        return hasGLList;
    }

    private void compileStaticRailParts(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
        GLHelper.startCompile(tileEntity.glLists[this.currentRailIndex]);//GL_COMPILE_AND_EXECUTEは画面がチラつく
        this.isCompilingStaticRail = true;
        this.renderedStaticGeometry = false;
        try {
            double[] origin = this.getRailRenderOrigin(tileEntity);
            // Static display lists are translated again at draw time, so scripts that
            // apply the rail origin manually need the inverse offset while compiling.
            ScriptUtil.doScriptFunction(this.script, "renderRailStatic",
                    tileEntity, -origin[0], -origin[1], -origin[2], par8, 0);
            if (!this.renderedStaticGeometry) {
                tileEntity.shouldRerenderRail = false;
            }
        } finally {
            this.renderedStaticGeometry = false;
            this.isCompilingStaticRail = false;
            GLHelper.endCompile();
        }
    }

    private boolean renderStaticPartsGeometry(TileEntityLargeRailCore tileEntity) {
        this.renderedStaticGeometry = true;
        float[][] fa = this.createRailPos(tileEntity);
        if (fa == null || fa.length == 0 || (fa.length == 1 && tileEntity.getRailRenderMinimumSplit() == 0)) {
            tileEntity.shouldRerenderRail = true;
            return false;
        }

        boolean brightnessReady = this.areRailBrightnessPositionsLoaded(
                tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, fa);
        int[] brightness = this.getRailBrightness(tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, fa);
        FloatBuffer fb = this.createMatrix(fa);
        List<GroupObject> groups = this.modelSet.model.model.getGroupObjects();
        if (groups.isEmpty()) {
            tileEntity.shouldRerenderRail = true;
            return false;
        }
        int renderKey = brightnessReady
                ? this.createStaticRenderKey(fa, brightness)
                : BRIGHTNESS_PENDING_RENDER_KEY;
        tileEntity.setStaticRenderKey(this.currentRailIndex, renderKey);
        this.tessellateParts(tileEntity, fb, brightness, groups);
        tileEntity.shouldRerenderRail = !brightnessReady;
        return true;
    }

    private boolean shouldRefreshDisplayList(TileEntityLargeRailCore tileEntity) {
        if (this.modelSet.model.model.getGroupObjects().isEmpty()) {
            return true;
        }

        float[][] fa = this.createRailPos(tileEntity);
        if (fa == null || fa.length == 0 || (fa.length == 1 && tileEntity.getRailRenderMinimumSplit() == 0)) {
            return true;
        }

        int[] brightness = this.getRailBrightness(tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, fa);
        int currentKey = this.createStaticRenderKey(fa, brightness);
        return currentKey != tileEntity.getStaticRenderKey(this.currentRailIndex);
    }

    private int createStaticRenderKey(float[][] fa, int[] brightness) {
        int key = 31 * Arrays.deepHashCode(fa) + Arrays.hashCode(brightness);
        return 31 * key + this.createModelRenderKey();
    }

    private int createModelRenderKey() {
        int key = this.modelSet.getConfig().getName().hashCode();
        key = 31 * key + Arrays.hashCode(this.getAllObjNames());
        key = 31 * key + Arrays.hashCode(
                Arrays.stream(this.getModelObject().textures)
                        .map(texture -> texture == null || texture.material == null || texture.material.texture == null
                                ? ""
                                : texture.material.texture.toString())
                        .toArray(String[]::new)
        );
        return key;
    }

    private void renderStaticDisplayList(TileEntityLargeRailCore tileEntity, double par2, double par4, double par6) {
        double[] origin = this.getRailRenderOrigin(tileEntity);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (par2 + origin[0]), (float) (par4 + origin[1]), (float) (par6 + origin[2]));
        this.bindTexture(this.getModelObject().textures[0].material.texture);//ディスプレイリストに入れると生成重い
        GLHelper.callList(tileEntity.glLists[this.currentRailIndex]);
        GL11.glPopMatrix();
    }

    public double[] getRailRenderOrigin(TileEntityLargeRailCore tileEntity) {
        RailPosition rp = tileEntity.getRailPositions()[0];
        return new double[]{
                rp.posX - (double) rp.blockX,
                rp.posY - (double) rp.blockY - 0.0625D,
                rp.posZ - (double) rp.blockZ
        };
    }

    /**
     * 各レールパーツの位置と向き
     *
     * @return {x, y, z, yaw, pitch, roll}
     */
    protected float[][] createRailPos(TileEntityLargeRailCore par1) {
        RailPosition originRP = par1.getRailPositions()[0];
        RailMap[] rms = par1.getAllRailMaps();
        if (rms != null) {
            List<float[]> list = new ArrayList<>();
            for (RailMap rm : rms) {
                int max = (int) ((float) rm.getLength() * 2.0F);
                max = Math.max(max, par1.getRailRenderMinimumSplit());
                double[] stPoint = rm.getRailPos(max, 0);
                //double startH = rm.getRailHeight(max, 0);//カント付けた時端が沈む
                double startH = rm.getStartRP().posY;
                float moveX = (float) (stPoint[1] - originRP.posX);
                float moveZ = (float) (stPoint[0] - originRP.posZ);
                //RM未初期化状態でのリスト生成を防止
                //if(moveX == 0.0F && moveZ == 0.0F){return null;}

                int endIndex = Math.max(0, max - par1.getRailRenderEndOffset());
                for (int i = 0; i <= endIndex; ++i) {
                    double[] curPoint = rm.getRailPos(max, i);
                    float[] array = {
                            moveX + (float) (curPoint[1] - stPoint[1]),
                            (float) (rm.getRailHeight(max, i) - startH),//0.0F,
                            moveZ + (float) (curPoint[0] - stPoint[0]),
                            rm.getRailRotation(max, i),
                            -rm.getRailPitch(max, i),
                            rm.getCant(max, i)
                    };

                    list.add(array);
                }
            }
            return list.toArray(new float[list.size()][5]);
        }
        return null;
    }

    /**
     * レンダリング用同時変換行列を作製
     */
    private FloatBuffer createMatrix(float[][] rp) {
        FloatBuffer buffer = FloatBuffer.allocate(rp.length << 4);
        for (float[] aFloat : rp) {
            FloatBuffer fb = this.convBuf;
            fb = NGTRenderHelper.translate(fb, aFloat[0], aFloat[1], aFloat[2]);
            fb = NGTRenderHelper.rotate(fb, NGTMath.toRadians(aFloat[3]), 'Y');
            fb = NGTRenderHelper.rotate(fb, NGTMath.toRadians(aFloat[4]), 'X');
            fb = NGTRenderHelper.rotate(fb, NGTMath.toRadians(aFloat[5]), 'Z');
            buffer.put(fb);
        }
        return buffer;
    }

    //Entity.getBrightnessForRender()はRenderManager.renderEntityStatic()で呼ばれる

    /**
     * レール上の明るさを一括取得
     */
    protected final int[] getRailBrightness(World world, int x, int y, int z, float[][] rp) {
        final int unavailable = Integer.MIN_VALUE;
        int[] brightness = new int[rp.length];
        int fallback = this.getBrightness(world, x, y, z);
        int previous = unavailable;
        for (int i = 0; i < rp.length; ++i) {
            int x0 = x + MathHelper.floor_double(rp[i][0]);
            int y0 = y + MathHelper.floor_double(rp[i][1]);
            int z0 = z + MathHelper.floor_double(rp[i][2]);
            if (world.blockExists(x0, y0, z0)) {
                brightness[i] = this.getBrightness(world, x0, y0, z0);
                previous = brightness[i];
                fallback = brightness[i];
            } else if (previous != unavailable) {
                brightness[i] = previous;
            } else {
                brightness[i] = unavailable;
            }
        }

        int next = fallback;
        for (int i = brightness.length - 1; i >= 0; --i) {
            if (brightness[i] == unavailable) {
                brightness[i] = next;
            } else {
                next = brightness[i];
            }
        }
        return brightness;
    }

    private boolean areRailBrightnessPositionsLoaded(TileEntityLargeRailCore tileEntity) {
        float[][] positions = this.createRailPos(tileEntity);
        return positions != null && this.areRailBrightnessPositionsLoaded(
                tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, positions);
    }

    private boolean areRailBrightnessPositionsLoaded(World world, int x, int y, int z, float[][] positions) {
        for (float[] position : positions) {
            int x0 = x + MathHelper.floor_double(position[0]);
            int y0 = y + MathHelper.floor_double(position[1]);
            int z0 = z + MathHelper.floor_double(position[2]);
            if (!world.blockExists(x0, y0, z0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定座標の明るさ取得(バグ回避処理付き)
     */
    public int getBrightness(World world, int x, int y, int z) {
        int brightness = this.getWorldBrightness(world, x, y, z);
        if (brightness <= 0) {
            brightness = this.getWorldBrightness(world, x, y + 1, z);//一部黒くなる問題回避
        }
        return brightness;
    }

    /**
     * 指定座標の明るさ取得
     */
    private int getWorldBrightness(World world, int x, int y, int z) {
        return world.blockExists(x, y, z) ? world.getLightBrightnessForSkyBlocks(x, y, z, 0) : 0;
    }

    /**
     * 引数のパーツを一括描画<br>
     * GLListコンパイル用
     *
     * @param matrix     セグメントごとの座標&角度
     * @param brightness セグメントごとの明るさ
     * @param gObjList   セグメントの構成ObjectのList x セグメント数
     */
    private void tessellateParts(TileEntityLargeRailCore tileEntity, FloatBuffer matrix, int[] brightness, List<GroupObject> gObjList) {
        IRenderer tessellator = PolygonRenderer.INSTANCE;
        tessellator.startDrawing(GL11.GL_TRIANGLES);
        int capacity = matrix.capacity() >> 4;
        for (int i = 0; i < capacity; ++i) {
            tessellator.setBrightness(brightness[i]);
            for (GroupObject group : gObjList) {
                if (group.name.startsWith("side")) {
                    boolean isStartCap = i == 0 && tileEntity.shouldRenderRailStartCap();
                    boolean isEndCap = i == capacity - 1 && tileEntity.shouldRenderRailEndCap();
                    if (!isStartCap && !isEndCap) {
                        continue;
                    }
                }//レール全体の端以外は断面を描画しない, +1~2fps

                if (!this.shouldRenderObject(tileEntity, group.name, capacity, i)) {
                    continue;
                }//描画するかスクリプト側で判断

                for (int k = 0; k < group.faces.size(); ++k) {
                    Face face = group.faces.get(k);
                    NGTRenderHelper.addFaceWithMatrix(face, tessellator, matrix, i, false);
                }
            }
        }
        tessellator.draw();
    }

    public String[] getAllObjNames() {
        List<GroupObject> gObj = this.modelObj.model.getGroupObjects();
        return gObj.stream().map(groupObject -> groupObject.name).toArray(String[]::new);
    }

    public boolean isSwitchRail(TileEntityLargeRailCore tileEntity) {
        return tileEntity.getAllRailMaps().length > 1;
    }

    public void renderRailMapStatic(TileEntityLargeRailSwitchCore tileEntity, RailMap rm, int max, int startIndex, int endIndex, Parts... pArray) {
        double[] origPos = rm.getRailPos(max, 0);
        double origHeight = rm.getRailHeight(max, 0);
        RailPosition originRP = tileEntity.getRailPositions()[0];
        //レール全体の始点からの移動差分
        float moveX = (float) (origPos[1] - originRP.posX);
        float moveZ = (float) (origPos[0] - originRP.posZ);

        //頂点-中間点
        for (int i = startIndex; i <= endIndex; i++) {
            double[] p1 = rm.getRailPos(max, i);
            double h = rm.getRailHeight(max, i);
            float x0 = moveX + (float) (p1[1] - origPos[1]);
            float y0 = (float) (h - origHeight);
            float z0 = moveZ + (float) (p1[0] - origPos[0]);
            float yaw = rm.getRailRotation(max, i);
            float pitch = rm.getRailPitch(max, i);
            this.setBrightness(this.getBrightness(tileEntity.getWorldObj(),
                    MathHelper.floor_double(origPos[1] + x0), tileEntity.yCoord, MathHelper.floor_double(origPos[0] + z0)));
            GL11.glPushMatrix();
            GL11.glTranslatef(x0, y0, z0);
            GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);
            Arrays.stream(pArray).forEach(parts -> parts.render(this));
            GL11.glPopMatrix();
        }
    }

    public ModelObject getModelObject() {
        return this.modelSet.model;
    }

    public void setBrightness(int par1) {
        GLHelper.setBrightness(par1);
    }
}
