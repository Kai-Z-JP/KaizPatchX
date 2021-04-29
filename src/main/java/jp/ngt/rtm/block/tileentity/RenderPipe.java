package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderPipe extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/blocks/pipe.png");

    private DisplayList displayListPipe;
    private DisplayList displayListSphere;

    private void renderPipeAt(TileEntityPipe tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);
        this.bindTexture(texture);

        int meta = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

        if (meta == 0) {
            meta = ((meta - 1) / 2) + 1;
            switch (tileEntity.getDirection()) {
                case 0:
                    this.render_A(90.0F, 0.0F, 0.0F, 1.0F, 0.5F);
                    this.render_A(-90.0F, 0.0F, 0.0F, 1.0F, 0.5F);
                    break;
                case 1:
                    this.render_A(0.0F, 0.0F, 0.0F, 0.0F, 0.5F);
                    this.render_A(180.0F, 0.0F, 0.0F, 1.0F, 0.5F);
                    break;
                case 2:
                    this.render_A(90.0F, 1.0F, 0.0F, 0.0F, 0.5F);
                    this.render_A(-90.0F, 1.0F, 0.0F, 0.0F, 0.5F);
                    break;
            }
        } else {
            meta = meta / 2;
            float size = (meta == 0 ? 0.5F : (meta == 1 ? 0.4375F : (meta == 2 ? 0.375F : 0.3125F)));

            byte[] con0 = tileEntity.connection;
            int con2 = 0;
            byte flag0 = 0;
            byte flag1 = 0;
            byte flag2 = 0;
            for (byte b : con0) {
                con2 += (b == 2 || b == 3) ? 1 : 0;
            }

            if (con0[0] == 2 || con0[0] == 3)//-y
            {
                this.render_A(180.0F, 1.0F, 0.0F, 0.0F, size);
                if (con2 == 1) {
                    this.render_A(0.0F, 0.0F, 0.0F, 0.0F, size);
                } else {
                    flag0 += 1;
                }
            } else if (con0[0] == 1) {
                this.render_C(180.0F, 1.0F, 0.0F, 0.0F, size);
            }

            if (con0[1] == 2 || con0[1] == 3)//+y
            {
                this.render_A(0.0F, 0.0F, 0.0F, 0.0F, size);
                if (con2 == 1) {
                    this.render_A(180.0F, 1.0F, 0.0F, 0.0F, size);
                } else {
                    flag0 += 1;
                }
            } else if (con0[1] == 1) {
                this.render_C(0.0F, 0.0F, 0.0F, 0.0F, size);
            }

            if (con0[2] == 2 || con0[2] == 3)//-z
            {
                this.render_A(-90.0F, 1.0F, 0.0F, 0.0F, size);
                if (con2 == 1) {
                    this.render_A(90.0F, 1.0F, 0.0F, 0.0F, size);
                } else {
                    flag1 += 1;
                }
            } else if (con0[2] == 1) {
                this.render_C(-90.0F, 1.0F, 0.0F, 0.0F, size);
            }

            if (con0[3] == 2 || con0[3] == 3)//+z
            {
                this.render_A(90.0F, 1.0F, 0.0F, 0.0F, size);
                if (con2 == 1) {
                    this.render_A(-90.0F, 1.0F, 0.0F, 0.0F, size);
                } else {
                    flag1 += 1;
                }
            } else if (con0[3] == 1) {
                this.render_C(90.0F, 1.0F, 0.0F, 0.0F, size);
            }


            if (con0[4] == 2 || con0[4] == 3)//+x
            {
                this.render_A(90.0F, 0.0F, 0.0F, 1.0F, size);
                if (con2 == 1) {
                    this.render_A(-90.0F, 0.0F, 0.0F, 1.0F, size);
                } else {
                    flag2 += 1;
                }
            } else if (con0[4] == 1) {
                this.render_C(90.0F, 0.0F, 0.0F, 1.0F, size);
            }

            if (con0[5] == 2 || con0[5] == 3)//-x
            {
                this.render_A(-90.0F, 0.0F, 0.0F, 1.0F, size);
                if (con2 == 1) {
                    this.render_A(90.0F, 0.0F, 0.0F, 1.0F, size);
                } else {
                    flag2 += 1;
                }
            } else if (con0[5] == 1) {
                this.render_C(-90.0F, 0.0F, 0.0F, 1.0F, size);
            }

            if (flag0 >= 2 || flag1 >= 2 || flag2 >= 2 || con2 != 1)//曲がってる,接続なし
            {
                this.render_B(size);
            }
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
        this.renderPipeAt((TileEntityPipe) par1, par2, par4, par6, par8);
    }

    private void render_A(float rotation, float x, float y, float z, float size) {
        GL11.glPushMatrix();
        GL11.glRotatef(rotation, x, y, z);
        float sc = size * 2.0F;
        GL11.glScalef(sc, 1.0F, sc);
        this.renderPipeModel();
        GL11.glPopMatrix();
    }

    private void render_B(float size) {
        GL11.glPushMatrix();
        float sc = size * 2.0F;
        GL11.glScalef(sc, sc, sc);
        this.renderSphereModel();
        GL11.glPopMatrix();
    }

    private void render_C(float rotation, float x, float y, float z, float size) {
        GL11.glPushMatrix();
        GL11.glRotatef(rotation, x, y, z);
        GL11.glTranslatef(0.0F, size - 0.125F, 0.0F);
        float sc = size * 2.0F;
        GL11.glScalef(size, 1.25F - sc, size);
        this.renderPipeModel();
        GL11.glPopMatrix();
    }

    private void renderPipeModel() {
        if (!GLHelper.isValid(this.displayListPipe)) {
            this.displayListPipe = GLHelper.generateGLList();
            GLHelper.startCompile(this.displayListPipe);
            IModelNGT model_pipe = ModelLoader.loadModel(new ResourceLocation("rtm", "models/Model_Pipe.mqo"), VecAccuracy.LOW, GL11.GL_QUADS);
            model_pipe.renderAll(RTMCore.smoothing);
            GLHelper.endCompile();
        } else {
            GLHelper.callList(this.displayListPipe);
        }
    }

    private void renderSphereModel() {
        if (!GLHelper.isValid(this.displayListSphere)) {
            this.displayListSphere = GLHelper.generateGLList();
            GLHelper.startCompile(this.displayListSphere);
            IModelNGT model_sphere = ModelLoader.loadModel(new ResourceLocation("rtm", "models/Model_Sphere.obj"), VecAccuracy.LOW);
            model_sphere.renderAll(RTMCore.smoothing);
            GLHelper.endCompile();
        } else {
            GLHelper.callList(this.displayListSphere);
        }
    }
}