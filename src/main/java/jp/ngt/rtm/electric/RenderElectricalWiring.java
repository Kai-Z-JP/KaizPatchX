package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import jp.ngt.rtm.modelpack.cfg.ConnectorConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetConnectorClient;
import jp.ngt.rtm.modelpack.modelset.ModelSetWireClient;
import jp.ngt.rtm.render.RenderPass;
import jp.ngt.rtm.render.WirePartsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderElectricalWiring extends TileEntitySpecialRenderer {
    public static final RenderElectricalWiring INSTANCE = new RenderElectricalWiring();

    private RenderElectricalWiring() {
    }

    protected void renderElectricalWiring(TileEntityElectricalWiring tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        int pass = MinecraftForgeClient.getRenderPass();
        if (tileEntity instanceof TileEntityConnectorBase) {
            this.renderConnector((TileEntityConnectorBase) tileEntity, par2, par4, par6, par8, pass);
        } else {
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        }
        this.renderAllWire(tileEntity, par2, par4, par6, par8, pass);

        GL11.glPopMatrix();
    }

    protected void renderConnector(TileEntityConnectorBase tileEntity, double par2, double par4, double par6, float par8, int pass) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.5F, (float) par6 + 0.5F);
        GL11.glTranslatef(tileEntity.getOffsetX(), tileEntity.getOffsetY(), tileEntity.getOffsetZ());
        GL11.glRotatef(tileEntity.getRotation(), 0.0F, 1.0F, 0.0F);
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
        modelSet.modelObj.render(tileEntity, cfg, pass, par8);
        GL11.glPopMatrix();
    }

    protected void renderAllWire(TileEntityElectricalWiring tileEntity, double par2, double par4, double par6, float par8, int pass) {
        GL11.glPushMatrix();
        Vec3 vec = tileEntity.getWirePos();
        GL11.glTranslatef((float) par2 + 0.5F + (float) vec.getX(), (float) par4 + 0.5F + (float) vec.getY(), (float) par6 + 0.5F + (float) vec.getZ());

        tileEntity.getConnectionList().stream()
                .filter(connection -> connection.type.isVisible && connection.isRoot)
                .forEach(connection -> this.renderWire(tileEntity, connection, par2, par4, par6, par8, pass));
        GL11.glPopMatrix();
    }

    private void renderWire(TileEntityElectricalWiring tileEntity, Connection connection, double par2, double par4, double par6, float par8, int pass) {
        ModelSetWireClient modelSet = (ModelSetWireClient) connection.getModelSet();
        if (modelSet.isDummy()) {
            return;
        }

        if (modelSet.getConfig().doCulling) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        if (modelSet.getConfig().smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        Vec3 vec = this.getConnectedTarget(tileEntity, connection, par8);
        WirePartsRenderer renderer = (WirePartsRenderer) modelSet.modelObj.renderer;
        if (pass == 0) {
            renderer.renderWire(tileEntity, connection, vec, par8, RenderPass.NORMAL);
        } else if (pass == 1) {
            if (modelSet.modelObj.light) {
                GLHelper.disableLighting();
                GLHelper.setLightmapMaxBrightness();
                renderer.renderWire(tileEntity, connection, vec, par8, RenderPass.LIGHT);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GLHelper.enableLighting();
            }

            if (modelSet.modelObj.alphaBlend) {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                renderer.renderWire(tileEntity, connection, vec, par8, RenderPass.TRANSPARENT);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }

        if (modelSet.getConfig().smoothing) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public Vec3 getConnectedTarget(TileEntityElectricalWiring tileEntity, Connection connection, float par8) {
        Vec3 posMain = tileEntity.getWirePos();
        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;
        float thisX = (float) tileEntity.xCoord + 0.5F + (float) posMain.getX();
        float thisY = (float) tileEntity.yCoord + (tileEntity instanceof TileEntityDummyEW ? 0.0F : 0.5F) + (float) posMain.getY();
        float thisZ = (float) tileEntity.zCoord + 0.5F + (float) posMain.getZ();
        if (connection.type == ConnectionType.TO_ENTITY) {
            TileEntityElectricalWiring tile = ((List<Entity>) tileEntity.getWorldObj().loadedEntityList).stream()
                    .filter(EntityElectricalWiring.class::isInstance)
                    .map(EntityElectricalWiring.class::cast)
                    .map(entity -> entity.tileEW)
                    .filter(target -> target.xCoord == connection.x && target.yCoord == connection.y && target.zCoord == connection.z)
                    .findFirst()
                    .orElse(null);
            if (tile != null) {
                Vec3 posTarget = tile.getWirePos();
                if (posTarget != null) {
                    x = (float) connection.x + 0.5F + (float) posTarget.getX() - thisX;
                    y = (float) connection.y + (float) posTarget.getY() - thisY;
                    z = (float) connection.z + 0.5F + (float) posTarget.getZ() - thisZ;
                }
            }
        } else if (connection.type == ConnectionType.TO_PLAYER)//手に持ってる
        {
            EntityPlayer entity = connection.getPlayer(tileEntity.getWorldObj());
            if (entity != null) {
                float f9 = entity.getSwingProgress(par8);
                float f10 = MathHelper.sin(MathHelper.sqrt_float(f9) * (float) Math.PI);
                Vec3 vec3 = PooledVec3.create(-0.46D, -0.2D, 0.65D);
                vec3 = vec3.rotateAroundX(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par8));
                vec3 = vec3.rotateAroundY(-(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par8));
                vec3 = vec3.rotateAroundY(f10 * 0.5F);
                vec3 = vec3.rotateAroundX(-f10 * 0.7F);
                double x0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) par8 + vec3.getX();
                double y0 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) par8 + vec3.getY();
                double z0 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) par8 + vec3.getZ();
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
                Vec3 posTarget = ((TileEntityConnectorBase) tile).wirePos;
                if (posTarget != null) {
                    x = (float) connection.x + 0.5F + (float) posTarget.getX() - thisX;
                    y = (float) connection.y + 0.5F + (float) posTarget.getY() - thisY;
                    z = (float) connection.z + 0.5F + (float) posTarget.getZ() - thisZ;
                }
            }
        }

        return PooledVec3.create(x, y, z);
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
        this.renderElectricalWiring((TileEntityElectricalWiring) tileentity, d0, d1, d2, f);
    }
}