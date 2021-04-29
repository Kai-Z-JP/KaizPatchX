package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.PolygonModel;
import jp.ngt.ngtlib.renderer.model.VecAccuracy;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderConverter extends TileEntitySpecialRenderer {
    private final PolygonModel model = ModelLoader.loadModel(new ResourceLocation("rtm", "models/Model_Converter.obj"), VecAccuracy.MEDIUM);
    private static final ResourceLocation[] textures = {new ResourceLocation("rtm", "textures/tileentity/converter_empty.png"),
            new ResourceLocation("rtm", "textures/tileentity/converter.png"),
            new ResourceLocation("rtm", "textures/tileentity/converter_burning.png"),
            new ResourceLocation("rtm", "textures/tileentity/converter_finish.png")};

    private void renderConverterAt(TileEntityConverterCore tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4, (float) par6 + 0.5F);
        this.bindTexture(textures[tileEntity.getMode()]);
        GL11.glRotatef((float) -tileEntity.getDirection() * 90.0F, 0.0F, 1.0F, 0.0F);
        this.model.renderPart(RTMCore.smoothing, "dai");
        GL11.glRotatef(tileEntity.getPitch(), 1.0F, 0.0F, 0.0F);
        this.model.renderPart(RTMCore.smoothing, "jiku");
        this.model.renderPart(RTMCore.smoothing, "body1");
        this.model.renderPart(RTMCore.smoothing, "body2");
        GL11.glPopMatrix();
    }

    public void renderTileEntityAt(TileEntity par1, double par2, double par4, double par6, float par8) {
        this.renderConverterAt((TileEntityConverterCore) par1, par2, par4, par6, par8);
    }
}