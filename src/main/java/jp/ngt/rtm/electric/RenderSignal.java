package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignalClient;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderSignal extends TileEntitySpecialRenderer {
    public void renderTileEntitySignalAt(TileEntitySignal tileEntity, double par2, double par4, double par6, float par8) {
        ModelSetSignalClient modelSet = (ModelSetSignalClient) tileEntity.getModelSet();

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4, (float) par6 + 0.5F);

        float dir = tileEntity.getBlockDirection();
        GL11.glRotatef(dir, 0.0F, 1.0F, 0.0F);
        int pass = MinecraftForgeClient.getRenderPass();
        modelSet.model.render(tileEntity, modelSet.getConfig(), pass, par8);

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderTileEntitySignalAt((TileEntitySignal) tileEntity, par2, par4, par6, par8);
    }
}