package jp.ngt.rtm.gui.camera;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class Camera {
    public static final Camera INSTANCE = new Camera();

    private static final int DELAY = 10;

    private static final int GAUS_MAX = 24;

    private static final int DEPTH_SPLIT = 8;

    private static final float GAUS_COEF = 20.0F;

    private static final float MIN_DEPTH = 0.75F;

    private final float[] gaussianWeights = new float[24];

    private final FloatBuffer gaussianWeightsBuf = GLAllocation.createDirectFloatBuffer(24);

    private final int[] gaussianFBO = new int[]{-1, -1, -1};

    private final int[] gaussianTex = new int[]{-1, -1, -1};

    private final int[] mcScreenTex = new int[]{-1, -1};

    @Deprecated
    private int mcDepthTex = -1;

    private int dofShader = -1;

    private FloatBuffer depthBuffer;

    private IntBuffer depthColorBuffer;

    private boolean isActive;

    private boolean mcTexRendered;

    private float scale = 1.0F;

    private float sensitivity = 1.0F;

    private float focus = 0.5F;

    private int focusMode;

    private float fovModification = 1.0F;

    private float frameBufU = 1.0F;

    private float frameBufV = 1.0F;

    private int tickCount;

    private Camera() {
        int i;
        for (i = 0; i < this.gaussianFBO.length; i++) {
            this.gaussianFBO[i] = GL30.glGenFramebuffers();
            this.gaussianTex[i] = GL11.glGenTextures();
        }
        for (i = 0; i < 2; i++)
            this.mcScreenTex[i] = GL11.glGenTextures();
        this.mcDepthTex = GL11.glGenTextures();
    }

    private void init(int w, int h) {
        int i;
        for (i = 0; i < this.gaussianFBO.length; i++) {
            initTexture(this.gaussianTex[i], w, h);
            GL30.glBindFramebuffer(36160, this.gaussianFBO[i]);
            GL30.glFramebufferTexture2D(36160, 36064, 3553, this.gaussianTex[i], 0);
            GL30.glBindFramebuffer(36160, 0);
        }
        for (i = 0; i < 2; i++)
            initTexture(this.mcScreenTex[i], w, h);
        try {
            String vsh = NGTText.getText(new ResourceLocation("rtm", "shaders/dof.vsh"), true);
            String fsh = NGTText.getText(new ResourceLocation("rtm", "shaders/dof.fsh"), true);
            this.dofShader = GLHelper.getShaderProgram(vsh, fsh);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.depthBuffer == null || this.depthBuffer.capacity() < w * h) {
            this.depthBuffer = GLAllocation.createDirectFloatBuffer(w * h);
            this.depthColorBuffer = GLAllocation.createDirectIntBuffer(w * h);
        }
    }

    private void initTexture(int texture, int w, int h) {
        GL11.glBindTexture(3553, texture);
        GL11.glTexImage2D(3553, 0, 32856, w, h, 0, 6408, 5121, (IntBuffer) null);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glBindTexture(3553, 0);
    }

    public void onRenderGameOverlayPre() {
        if (!this.isActive)
            return;
        if (this.sensitivity < 1.0F)
            if (this.sensitivity < 1.0F) {
                if (this.tickCount >= 10) {
                    this.tickCount = 0;
                    copyScreenBuf(0);
                }
                this.tickCount++;
            }
    }

    public void onRenderWorldPost() {
        if (!this.isActive)
            return;
        if (this.focusMode > 0) {
            copyDepthBuffer();
            copyScreenBuf(1);
        }
    }

    private void copyScreenBuf(int texId) {
        this.mcTexRendered = true;
        Minecraft mc = Minecraft.getMinecraft();
        int w = (mc.getFramebuffer()).framebufferTextureWidth;
        int h = (mc.getFramebuffer()).framebufferTextureHeight;
        GL43.glCopyImageSubData((mc.getFramebuffer()).framebufferTexture, 3553, 0, 0, 0, 0, this.mcScreenTex[texId], 3553, 0, 0, 0, 0, w, h, 1);
        this.frameBufU = (mc.getFramebuffer()).framebufferWidth / (mc.getFramebuffer()).framebufferTextureWidth;
        this.frameBufV = (mc.getFramebuffer()).framebufferHeight / (mc.getFramebuffer()).framebufferTextureHeight;
    }

    private void copyDepthBuffer() {
        Minecraft mc = Minecraft.getMinecraft();
        int w = (mc.getFramebuffer()).framebufferTextureWidth;
        int h = (mc.getFramebuffer()).framebufferTextureHeight;
        this.depthBuffer.clear();
        GL11.glReadPixels(0, 0, w, h, 6402, 5126, this.depthBuffer);
        this.depthColorBuffer.clear();
        for (int i = 0; i < this.depthBuffer.capacity(); i++) {
            float depth = this.depthBuffer.get(i);
            this.depthColorBuffer.put(depth2Color(depth));
        }
        this.depthColorBuffer.flip();
        GL11.glBindTexture(3553, this.gaussianTex[2]);
        GL11.glTexSubImage2D(3553, 0, 0, 0, w, h, 6408, 5121, this.depthColorBuffer);
        if (this.focusMode == 1) {
            int index = h / 2 * w + w / 2;
            int g = ColorUtil.getG(this.depthColorBuffer.get(index));
            this.focus = g / 255.0F;
        }
    }

    private int depth2Color(float depth) {
        int c0 = (int) (fixDepth(depth) * 255.0F);
        return ColorUtil.encode(c0, c0, c0);
    }

    private float fixDepth(float depth) {
        if (depth >= 0.75F) {
            double d0 = ((depth - 0.75F) / 0.25F);
            if (d0 >= 1.0D)
                return 1.0F;
            d0 *= d0;
            d0 *= d0;
            d0 *= d0;
            return (float) (-Math.sqrt(1.0D - d0) + 1.0D);
        }
        return 0.0F;
    }

    public void off() {
        if (this.isActive) {
            this.isActive = false;
            this.mcTexRendered = false;
            this.tickCount = 0;
        }
    }

    public void render(Minecraft mc, RenderGameOverlayEvent event, int guiW, int guiH) {
        int w = mc.displayWidth;
        int h = mc.displayHeight;
        if (!this.isActive) {
            this.isActive = true;
            init(w, h);
        }
        GL11.glDisable(2929);
        if (this.sensitivity < 1.0F)
            renderDelayFrame(mc, guiW, guiH);
        if (this.focusMode > 0 && this.dofShader > 0) {
            checkScreenshot(w, h, "ss_depth");
            renderDOF(mc, guiW, guiH);
        }
        GL11.glEnable(2929);
        updateKeyState();
        renderText(mc, guiW, guiH);
    }

    private void renderDelayFrame(Minecraft mc, int guiW, int guiH) {
        if (!this.mcTexRendered)
            return;
        int alpha = (int) (255.0F * (1.0F - this.sensitivity));
        GL11.glBindTexture(3553, this.mcScreenTex[0]);
        renderBuffer(guiW, guiH, alpha, true);
    }

    private void renderDOF(Minecraft mc, int guiW, int guiH) {
        if (!this.mcTexRendered)
            return;
        ARBShaderObjects.glUseProgramObjectARB(this.dofShader);
        ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "texture0"), 0);
        ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "texture1"), 1);
        ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "width"), guiW);
        ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "height"), guiH);
        GL13.glActiveTexture(33985);
        GL11.glBindTexture(3553, this.gaussianTex[2]);
        for (int frame = 8; frame > 0; frame--) {
            float blurThreshold = frame / 8.0F;
            double blurStrMax = 24.0D * mc.displayWidth / 1500.0D;
            if (blurStrMax > 24.0D)
                blurStrMax = 24.0D;
            float focusDif = Math.abs(blurThreshold - this.focus);
            int range = (int) Math.floor(NGTMath.sigmoid(focusDif * 2.0D, 7.0D) * blurStrMax);
            int maxIndex = calcGaussian(range);
            ARBShaderObjects.glUniform1fARB(
                    ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "threshold"), blurThreshold);
            ARBShaderObjects.glUniform1ARB(
                    ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "weight"), this.gaussianWeightsBuf);
            for (int pass = 0; pass < 2; pass++) {
                ARBShaderObjects.glUniform1iARB(ARBShaderObjects.glGetUniformLocationARB(this.dofShader, "pass"), pass);
                GL30.glBindFramebuffer(36160, this.gaussianFBO[pass]);
                switch (pass) {
                    case 0:
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, this.mcScreenTex[1]);
                        break;
                    case 1:
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, this.gaussianTex[0]);
                        break;
                }
                renderBuffer(guiW, guiH, -1, false);
                GL30.glBindFramebuffer(36160, 0);
            }
        }
        mc.getFramebuffer().bindFramebuffer(false);
        ARBShaderObjects.glUseProgramObjectARB(0);
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, this.gaussianTex[1]);
        renderBuffer(guiW, guiH, 255, true);
    }

    private int calcGaussian(int range) {
        range = NGTMath.clamp(range, 0, 24);
        float[] weight = this.gaussianWeights;
        if (range <= 0) {
            for (int j = 0; j < weight.length; j++)
                weight[j] = (j == 0) ? 1.0F : 0.0F;
            return 0;
        }
        int maxIndex = 0;
        double total = 0.0D;
        double variance = 20.0D * (range * range) / 576.0D;
        int i;
        for (i = 0; i < weight.length; i++) {
            double r = 1.0D + 2.0D * i;
            double w = Math.exp(-0.5D * r * r / variance);
            if (w < 0.00392156862745098D) {
                maxIndex = i - 1;
                break;
            }
            weight[i] = (float) w;
            if (i > 0)
                w *= 2.0D;
            total += w;
        }
        this.gaussianWeightsBuf.clear();
        for (i = 0; i < weight.length; i++) {
            if (i > maxIndex)
                weight[i] = 0.0F;
            weight[i] = (float) (weight[i] / total);
            this.gaussianWeightsBuf.put(weight[i]);
        }
        this.gaussianWeightsBuf.flip();
        return maxIndex;
    }

    private void renderBuffer(int w, int h, int alpha, boolean flip) {
        GL11.glDepthMask(false);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3008);
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawingQuads();
        if (alpha > 0)
            tessellator.setColorRGBA_I(16777215, alpha);
        float fx = w;
        float fy = h;
        float fz = -90.0F;
        float minV = flip ? this.frameBufV : 0.0F;
        float maxV = flip ? 0.0F : this.frameBufV;
        tessellator.addVertexWithUV(0.0F, fy, fz, 0.0F, maxV);
        tessellator.addVertexWithUV(fx, fy, fz, this.frameBufU, maxV);
        tessellator.addVertexWithUV(fx, 0.0F, fz, this.frameBufU, minV);
        tessellator.addVertexWithUV(0.0F, 0.0F, fz, 0.0F, minV);
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderText(Minecraft mc, int guiW, int guiH) {
        String focusStatus;
        int x = 5;
        int y = 5;
        int fontH = 10;
        int color = 65296;
        switch (this.focusMode) {
            case 0:
                focusStatus = "NONE";
                break;
            case 1:
                focusStatus = String.format("AUTO(%.3f)", this.focus);
                break;
            case 2:
                focusStatus = String.format("%.3f", this.focus);
                break;
            default:
                focusStatus = "err";
                break;
        }
        String s = String.format("x%.1f S:%d%% F:%s", this.scale, (int) (this.sensitivity * 100.0F), focusStatus);
        mc.fontRenderer.drawStringWithShadow(s, x, y, color);
        y += fontH;
        mc.fontRenderer.drawStringWithShadow(
                String.format("Zoom:[%c][%c]", CameraKey.ZOOM_OUT.chara, CameraKey.ZOOM_IN.chara), x, y, color);
        y += fontH;
        mc.fontRenderer.drawStringWithShadow(
                String.format("Sensitivity:[%c][%c]", CameraKey.SENSIT_DOWN.chara, CameraKey.SENSIT_UP.chara), x, y, color);
        y += fontH;
        mc.fontRenderer.drawStringWithShadow(
                String.format("Focus:[%c][%c] [%c]", CameraKey.FOCUS_OUT.chara, CameraKey.FOCUS_IN.chara, CameraKey.FOCUS_MODE.chara), x, y, color);
    }

    private void updateKeyState() {
        this.scale = CameraKeySet.ZOOM.updateValue(this.scale);
        float defaultFov = (Minecraft.getMinecraft()).gameSettings.fovSetting;
        double d0 = Math.tan(NGTMath.toRadians(defaultFov * 0.5F)) / this.scale;
        float fov = (float) NGTMath.toDegrees(Math.atan(d0) * 2.0D);
        this.fovModification = fov / defaultFov;
        this.sensitivity = CameraKeySet.SENSITIVITY.updateValue(this.sensitivity);
        if (this.focusMode == 2) {
            this.focus = CameraKeySet.FOCUS.updateValue(this.focus);
        }
        if (CameraKey.FOCUS_MODE.isPressed()) {
            this.focusMode = (this.focusMode + 1) % 3;
        }
    }

    public float getFov() {
        return this.fovModification;
    }

    private void checkScreenshot(int w, int h, String fileName) {
        if (CameraKey.DEBUG.isPressed()) {
            try {
                BufferedImage image = getDepthBufT(w, h);
                File file = new File(NGTFileLoader.getModsDir().get(0), fileName + ".png");
                ImageIO.write(image, "png", file);
                NGTLog.showChatMessage("save:%s", file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BufferedImage getMCScreen(int w, int h) {
        IntBuffer buf = BufferUtils.createIntBuffer(w * h);
        GL11.glBindTexture(3553, (Minecraft.getMinecraft().getFramebuffer()).framebufferTexture);
        GL11.glGetTexImage(3553, 0, 32993, 33639, buf);
        BufferedImage image = new BufferedImage(w, h, 1);
        for (int i = 0; i < buf.capacity(); i++) {
            int color = buf.get(i);
            image.getRaster().getDataBuffer().setElem(i, color);
        }
        return image;
    }

    private BufferedImage getDepthBufF(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, 1);
        for (int i = 0; i < this.depthBuffer.capacity(); i++) {
            float depth = this.depthBuffer.get(i);
            image.getRaster().getDataBuffer().setElem(i, depth2Color(depth));
        }
        return image;
    }

    private BufferedImage getDepthBufI(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, 1);
        for (int i = 0; i < this.depthColorBuffer.capacity(); i++) {
            int color = this.depthColorBuffer.get(i);
            image.getRaster().getDataBuffer().setElem(i, color);
        }
        return image;
    }

    private BufferedImage getDepthBufT(int w, int h) {
        IntBuffer buf = BufferUtils.createIntBuffer(w * h);
        GL11.glBindTexture(3553, this.gaussianTex[2]);
        GL11.glGetTexImage(3553, 0, 6408, 5121, buf);
        BufferedImage image = new BufferedImage(w, h, 1);
        for (int i = 0; i < buf.capacity(); i++) {
            int color = buf.get(i);
            image.getRaster().getDataBuffer().setElem(i, color);
        }
        return image;
    }
}
