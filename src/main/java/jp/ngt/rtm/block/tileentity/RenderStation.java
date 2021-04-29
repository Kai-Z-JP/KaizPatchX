package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMBlock;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderStation extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/mark.png");
    private Item stationBlock;

    public void renderStation(TileEntityStation tileEntity, double par2, double par4, double par6, float par8) {
        if (this.stationBlock == null) {
            this.stationBlock = Item.getItemFromBlock(RTMBlock.stationCore);
        }

        //ブロックを手に持ってる時以外は不可視
        ItemStack stack = NGTUtilClient.getMinecraft().thePlayer.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != this.stationBlock) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);
        GLHelper.disableLighting();
        GLHelper.setLightmapMaxBrightness();
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
		/*int x = tileEntity.xCoord & 15;
		int y = tileEntity.yCoord;
		int z = tileEntity.zCoord & 15;
		int yc = tileEntity.yCoord >> 4;
		for(int i = 0; i < 16; ++i)
		{
			int y0 = -y + (i << 4);
			int color = (i == yc) ? 0xFF8000 : 0x00FF00;
			this.renderFrame(-x, y0, -z, 16.0D, 16.0D, 16.0D, color, 0xFF);
		}*/
        NGTRenderer.renderFrame(0.0D, 0.0D, 0.0D, tileEntity.width, tileEntity.height, tileEntity.depth, 0x00FF00, 0xFF);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glTranslatef(0.5F, 1.0F, 0.5F);
        GL11.glRotatef(-RenderManager.instance.playerViewY + 180.0F, 0.0F, 1.0F, 0.0F);
        this.bindTexture(texture);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(8.0D, 0.0D, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(8.0D, 16.0D, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-8.0D, 16.0D, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(-8.0D, 0.0D, 0.0D, 0.0D, 1.0D);
        tessellator.draw();

        GL11.glTranslatef(0.0F, 11.0F, 0.0625F);
        FontRenderer fontRenderer = this.func_147498_b();
        String s = tileEntity.getName();
        int w = fontRenderer.getStringWidth(s);
        float f = 4.0F / (float) w;
        GL11.glScalef(f, -f, -f);
        fontRenderer.drawString(s, -w >> 1, -4, 0x00FF00);

        GLHelper.enableLighting();
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderStation((TileEntityStation) tileEntity, par2, par4, par6, par8);
    }
}