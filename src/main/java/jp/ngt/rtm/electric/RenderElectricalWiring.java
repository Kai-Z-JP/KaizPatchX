package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTVec;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import jp.ngt.rtm.modelpack.cfg.ConnectorConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetConnectorClient;
import jp.ngt.rtm.modelpack.modelset.ModelSetWireClient;
import jp.ngt.rtm.render.WirePartsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderElectricalWiring extends TileEntitySpecialRenderer {
    public static final RenderElectricalWiring INSTANCE = new RenderElectricalWiring();
    private final NGTVec vecTmp = new NGTVec(0.0D, 0.0D, 0.0D);

    private RenderElectricalWiring() {
    }

    protected void renderElectricalWiring(TileEntityConnectorBase tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        this.renderConnector(tileEntity, par2, par4, par6, par8);
        this.renderAllWire(tileEntity, par2, par4, par6, par8);

        GL11.glPopMatrix();
    }

    protected void renderConnector(TileEntityConnectorBase tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);
        ModelSetConnectorClient modelSet = (ModelSetConnectorClient) tileEntity.getModelSet();
        ConnectorConfig cfg = modelSet.getConfig();
        int meta = tileEntity.getBlockMetadata() % 6;
        switch (meta) {
            case 0:
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case 1:
                break;
            case 2://Z
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case 3://Z
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case 4://X
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case 5://X
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
        }
        modelSet.modelObj.render(tileEntity, cfg, 0, par8);
        GL11.glPopMatrix();
    }

    protected void renderAllWire(TileEntityConnectorBase tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        NGTVec vec = tileEntity.wirePos;
        GL11.glTranslatef((float) par2 + 0.5F + (float) vec.xCoord, (float) par4 + 0.5F + (float) vec.yCoord, (float) par6 + 0.5F + (float) vec.zCoord);

        tileEntity.getConnectionList().stream()
                .filter(connection -> connection.type.isVisible && connection.isRoot)
                .forEach(connection -> this.renderWire(tileEntity, connection, par2, par4, par6, par8));
        GL11.glPopMatrix();
    }

    private void renderWire(TileEntityConnectorBase tileEntity, Connection connection, double par2, double par4, double par6, float par8) {
        ModelSetWireClient modelSet = (ModelSetWireClient) connection.getModelSet();
        if (modelSet.isDummy()) {
            return;
        }

        if (modelSet.getConfig().doCulling) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        NGTVec vec = this.getConnectedTarget(tileEntity, connection, par8);
        WirePartsRenderer renderer = (WirePartsRenderer) modelSet.modelObj.renderer;
        renderer.renderWire(tileEntity, connection, vec, par8);

        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public NGTVec getConnectedTarget(TileEntityConnectorBase tileEntity, Connection connection, float par8) {
        NGTVec posMain = tileEntity.wirePos;
        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;
        float thisX = (float) tileEntity.xCoord + 0.5F + (float) posMain.xCoord;
        float thisY = (float) tileEntity.yCoord + 0.5F + (float) posMain.yCoord;
        float thisZ = (float) tileEntity.zCoord + 0.5F + (float) posMain.zCoord;
        if (connection.type == ConnectionType.TO_ENTITY) {
            x = (float) connection.x + 0.5F - thisX;
            y = (float) connection.y - thisY;
            z = (float) connection.z + 0.5F - thisZ;
        } else if (connection.type == ConnectionType.TO_PLAYER)//手に持ってる
        {
            EntityPlayer entity = connection.getPlayer(tileEntity.getWorldObj());
            if (entity != null) {
                float f9 = entity.getSwingProgress(par8);
                float f10 = MathHelper.sin(MathHelper.sqrt_float(f9) * (float) Math.PI);
                Vec3 vec3 = Vec3.createVectorHelper(-0.46D, -0.2D, 0.65D);
                vec3.rotateAroundX(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par8) * (float) Math.PI / 180.0F);
                vec3.rotateAroundY(-(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par8) * (float) Math.PI / 180.0F);
                vec3.rotateAroundY(f10 * 0.5F);
                vec3.rotateAroundX(-f10 * 0.7F);
                double x0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) par8 + vec3.xCoord;
                double y0 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) par8 + vec3.yCoord;
                double z0 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) par8 + vec3.zCoord;
                double d6 = entity == Minecraft.getMinecraft().thePlayer ? 0.0D : (double) entity.getEyeHeight();

                if (RenderManager.instance.options.thirdPersonView > 0 || entity != Minecraft.getMinecraft().thePlayer) {
                    float f11 = (entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * par8) * (float) Math.PI / 180.0F;
                    double d7 = MathHelper.sin(f11);
                    double d9 = MathHelper.cos(f11);
                    x0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) par8 - d9 * 0.35D - d7 * 0.85D;
                    y0 = entity.prevPosY + d6 + (entity.posY - entity.prevPosY) * (double) par8 - 0.45D;
                    z0 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) par8 - d7 * 0.35D + d9 * 0.85D;
                }

                x = (float) (x0 - thisX);
                y = (float) (y0 - thisY);
                z = (float) (z0 - thisZ);
            }
        } else {
            TileEntity tile = tileEntity.getWorldObj().getTileEntity(connection.x, connection.y, connection.z);
            if (tile instanceof TileEntityConnectorBase) {
                NGTVec posTarget = ((TileEntityConnectorBase) tile).wirePos;
                if (posTarget != null) {
                    x = (float) connection.x + 0.5F + (float) posTarget.xCoord - thisX;
                    y = (float) connection.y + 0.5F + (float) posTarget.yCoord - thisY;
                    z = (float) connection.z + 0.5F + (float) posTarget.zCoord - thisZ;
                }
            }
        }

        this.vecTmp.setValue(x, y, z);
        return this.vecTmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
        this.renderElectricalWiring((TileEntityConnectorBase) tileentity, d0, d1, d2, f);
    }
}