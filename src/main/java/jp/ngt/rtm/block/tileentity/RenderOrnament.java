package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.cfg.OrnamentConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetOrnamentClient;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderOrnament<T extends TileEntityOrnament> extends TileEntitySpecialRenderer {
    private void renderOrnament(T par1, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);

        ModelSetOrnamentClient modelSet = (ModelSetOrnamentClient) par1.getModelSet();
        OrnamentConfig cfg = modelSet.getConfig();
        int pass = MinecraftForgeClient.getRenderPass();

        GL11.glTranslatef(par1.getOffsetX(), par1.getOffsetY(), par1.getOffsetZ());
        GL11.glRotatef(par1.getRotation(), 0.0F, 1.0F, 0.0F);

        if (modelSet.model.renderer.getContext() == null) {
            float scale = par1.getRandomScale();
            GL11.glScalef(scale, scale, scale);
        }
        modelSet.model.render(par1, cfg, pass, par8);

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderOrnament((T) tileEntity, par2, par4, par6, par8);
    }
}