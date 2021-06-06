package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachineClient;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderMachine extends TileEntitySpecialRenderer {
    public static final RenderMachine INSTANCE = new RenderMachine();

//	private final IModelNGT model_vendor = ModelLoader.loadModel(new ResourceLocation("rtm", "models/TicketVendor.mqo"), VecAccuracy.MEDIUM, GL11.GL_QUADS);
//	private static final ResourceLocation tex_vendor = new ResourceLocation("rtm", "textures/machine/vendor.png");

    private RenderMachine() {
    }

    private void renderMachine(TileEntityMachineBase par1, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4, (float) par6 + 0.5F);

//		if (par1.getMachinleType() == MachineType.Vendor) {
//			GL11.glRotatef(par1.getRotation(), 0.0F, 1.0F, 0.0F);
//			this.renderVendor((TileEntityTicketVendor) par1, par2, par4, par6, par8);
//		} else {
        ModelSetMachineClient modelSet = (ModelSetMachineClient) par1.getModelSet();
        MachineConfig cfg = modelSet.getConfig();
        GL11.glTranslatef(0.0F, 0.5F, 0.0F);
        if (cfg.rotateByMetadata) {
            switch (par1.getBlockMetadata()) {
                case 0:
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    break;//-y
                case 1:
                    break;
                case 2:
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    break;//-z
                case 3:
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                    break;//+z
                case 4:
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                    break;//+x
                case 5:
                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                    break;//-x
            }
        }
        GL11.glTranslatef(0.0F, -0.5F, 0.0F);
        GL11.glTranslatef(par1.getOffsetX(), 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, par1.getOffsetY(), 0.0F);
        GL11.glTranslatef(0.0F, 0.0F, par1.getOffsetZ());
        float yaw = par1.getRotation();
        if (cfg.rotateByMetadata && par1.getBlockMetadata() == 0) {
            yaw = -yaw;
        }
        GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
        int pass = MinecraftForgeClient.getRenderPass();
        modelSet.modelObj.render(par1, cfg, pass, par8);
//		}

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
        this.renderMachine((TileEntityMachineBase) tileEntity, par2, par4, par6, par8);
    }

//	public void renderVendor(TileEntityTicketVendor par1, double par2, double par4, double par6, float par8) {
//		this.bindTexture(tex_vendor);
//		this.model_vendor.renderAll(false);
//	}
}