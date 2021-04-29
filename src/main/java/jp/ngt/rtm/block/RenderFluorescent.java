package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.rtm.block.tileentity.ModelFluorescent;
import jp.ngt.rtm.block.tileentity.TileEntityFluorescent;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderFluorescent extends TileEntitySpecialRenderer {
    private final ModelFluorescent model = new ModelFluorescent();
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/fluorescent.png");

    private void render(TileEntityFluorescent tileEntity, double par2, double par4, double par6, float par8) {
        int meta = tileEntity.getBlockMetadata();
        float move = meta == 4 ? 0.375F : 0.4375F;
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);

        switch (tileEntity.getDir()) {
            case 0:
                GL11.glTranslatef(0.0f, move, 0.0f);
                break;
            case 1:
                GL11.glTranslatef(0.0f, 0.0f, move);
                break;
            case 2:
                GL11.glTranslatef(0.0f, -move, 0.0f);
                break;
            case 3:
                GL11.glTranslatef(0.0f, 0.0f, -move);
                break;
            case 4:
                GL11.glTranslatef(0.0f, move, 0.0f);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case 5:
                GL11.glTranslatef(move, 0.0f, 0.0f);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case 6:
                GL11.glTranslatef(0.0f, -move, 0.0f);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case 7:
                GL11.glTranslatef(-move, 0.0f, 0.0f);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
        }

        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        this.bindTexture(texture);

        //NGTRenderHelper.setLightmapMaxBrightness();
        this.model.render(meta, 0, 0.0625F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GLHelper.disableLighting();
        this.model.render(meta, 1, 0.0625F);
        GLHelper.enableLighting();
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
        this.render((TileEntityFluorescent) par1, par2, par4, par6, par8);
    }
}