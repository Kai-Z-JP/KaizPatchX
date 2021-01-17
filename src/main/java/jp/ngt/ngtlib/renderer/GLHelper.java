package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.Locker;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class GLHelper {
    public static final GLHelper INSTANCE = new GLHelper();
    public static final Locker LOCKER = new Locker();

    private final List<DisplayList> activeGLLists = new ArrayList<>();
    private final List<DisplayList> deleteGLLists = new ArrayList<>();

    private GLHelper() {
    }

    public static void clearGLList() {
        LOCKER.lock();
        //if(INSTANCE.deleteGLLists.size() > 16)
        {
            //RuntimeException防止
            //GLAllocation.deleteDisplayLists(glList);
            INSTANCE.deleteGLLists.stream().filter(dl -> GL11.glIsList(dl.value)).forEach(dl -> GL11.glDeleteLists(dl.value, 1));
            NGTLog.debug("Clear " + INSTANCE.deleteGLLists.size() + " GL Lists");
            INSTANCE.deleteGLLists.clear();
        }
        LOCKER.unlock();
    }

    /**
     * ディスプレイリストの再生成
     */
    public static void initGLList() {
        clearGLList();

        LOCKER.lock();
        if (!INSTANCE.activeGLLists.isEmpty()) {
            INSTANCE.activeGLLists.stream().filter(dl -> GL11.glIsList(dl.value)).forEach(dl -> {
                GL11.glDeleteLists(dl.value, 1);
                dl.value = 0;
            });
            INSTANCE.activeGLLists.clear();

        }
        LOCKER.unlock();
    }

    public static void deleteGLList(DisplayList par1) {
        LOCKER.lock();
        if (par1 != null) {
            INSTANCE.activeGLLists.remove(par1);
            INSTANCE.deleteGLLists.add(par1);
        }
        LOCKER.unlock();
    }

    public static DisplayList generateGLList() {
        LOCKER.lock();
        DisplayList list = new DisplayList(GL11.glGenLists(1));
        INSTANCE.activeGLLists.add(list);
        LOCKER.unlock();
        return list;
    }

    public static boolean isValid(DisplayList par1) {
        return par1 != null && par1.value > 0;
    }

    public static void startCompile(DisplayList par1) {
        LOCKER.lock();
        GL11.glNewList(par1.value, GL11.GL_COMPILE);
    }

    public static void endCompile() {
        GL11.glEndList();
        LOCKER.unlock();
    }

    public static void callList(DisplayList par1) {
        GL11.glCallList(par1.value);
    }

    public static void setColor(int rgb, int alpha) {
        float r = (rgb >> 16) / 255.0F;
        float g = (rgb >> 8 & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        float a = alpha / 255.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static void setBrightness(int par1) {
        int x = par1 & 0xFFFF;
        int y = par1 >> 16;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) x, (float) y);
    }

    public static void setLightmapMaxBrightness() {
        //i%0x10000,i/0x10000
        //240より大きいとGeForce系で暗くなる?
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void enableLighting() {
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static void disableLighting() {
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    public static int getShaderProgram(String vsh, String fsh) {
        int vertShader = createShader(vsh, 35633);
        int fragShader = createShader(fsh, 35632);
        if (vertShader == 0 || fragShader == 0)
            return -1;
        int program = ARBShaderObjects.glCreateProgramObjectARB();
        if (program == 0)
            return -1;
        ARBShaderObjects.glAttachObjectARB(program, vertShader);
        ARBShaderObjects.glAttachObjectARB(program, fragShader);
        ARBShaderObjects.glLinkProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, 35714) == 0) {
            NGTLog.debug(getShaderErrorLog(program));
            return -1;
        }
        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, 35715) == 0) {
            NGTLog.debug(getShaderErrorLog(program));
            return -1;
        }
        return program;
    }

    private static int createShader(String shaderObj, int shaderType) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if (shader == 0)
                return 0;
            byte[] bytes = shaderObj.getBytes();
            ByteBuffer buffer = GLAllocation.createDirectByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            ARBShaderObjects.glShaderSourceARB(shader, buffer);
            ARBShaderObjects.glCompileShaderARB(shader);
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0)
                throw new RuntimeException(getShaderErrorLog(shader));
            return shader;
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    private static String getShaderErrorLog(int shader) {
        return ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader, 35716));
    }

    private static final IntBuffer VIEWPORT_BUF = GLAllocation.createDirectIntBuffer(16);

    private static final IntBuffer SELECT_BUF = GLAllocation.createDirectIntBuffer(1024);
    private static double DEPTH_RANGE;


    public static void startMousePicking(float range) {
        float mouseX = Display.getWidth() / 2.0F;//マウス位置=画面中央
        float mouseY = Display.getHeight() / 2.0F;
        //float fov = NGTUtilClient.getMinecraft().gameSettings.fovSetting;
        //float aspect = (float)NGTUtilClient.getMinecraft().displayWidth / (float)NGTUtilClient.getMinecraft().displayHeight;

        VIEWPORT_BUF.clear();
        SELECT_BUF.clear();

        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUF);//[0, 0, DispW, DispH]
        GL11.glSelectBuffer(SELECT_BUF);//[namesize, minZ, maxZ, no...]*n
        GL11.glRenderMode(GL11.GL_SELECT);
        GL11.glInitNames();
        GL11.glPushName(0);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        //GL11.glLoadIdentity();//不要
        Project.gluPickMatrix(mouseX, VIEWPORT_BUF.get(3) - mouseY, range, range, VIEWPORT_BUF);
        //範囲:0.05F~EntityRenderer.farPlaneDistance * MathHelper.SQRT_2
        //farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16
        //Project.gluPerspective(fov, aspect, 0.05F, 128.0F);//不要
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        DEPTH_RANGE = ((double) NGTUtilClient.getMinecraft().gameSettings.renderDistanceChunks * 16.0D * MathHelper.sqrt_float(2.0F)) - 0.05D;
    }

    public static int finishMousePicking() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        int hits = GL11.glRenderMode(GL11.GL_RENDER);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        return hits;
    }

    public static int getPickedObjId(int count) {
        return SELECT_BUF.get(count * 4 + 3);
    }

    /**
     * 深度値のMin側(0.0~1.0)
     */
    public static double getPickedObjDepth(int count) {
        //深度値はuintなので0.0~1.0に変換
        double depthRaw = (double) Integer.toUnsignedLong(SELECT_BUF.get(count * 4 + 1));
        return (depthRaw / (double) 0xFFFFFFFFL);
    }

    public static void preMoveTexUV(float u, float v) {
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslatef(u, v, 0.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public static void postMoveTexUV() {
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}