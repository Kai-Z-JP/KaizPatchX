package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class RenderMarkerBlock1710 extends RenderMarkerBlockBase {
    private static final double FIT_RANGE_SQ = 2.0D * 2.0D;

    public void renderTileEntityMarker(TileEntityMarker tileEntity, double par2, double par4, double par6, float par8) {
        if (!tileEntity.displayDistance) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GLHelper.disableLighting();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);

        if (tileEntity.getDisplayMode() == 1 && tileEntity.getGrid() != null) {
            this.renderGrid(tileEntity);
        }

        if (tileEntity.getDisplayMode() == 2) {
            RailPosition rp0 = tileEntity.getMarkerRP();
            double x = rp0.posX - (double) rp0.blockX;
            double y = rp0.posY - (double) rp0.blockY;
            double z = rp0.posZ - (double) rp0.blockZ;

            if (tileEntity.getRailMaps() != null) {
                this.renderLine(tileEntity, (float) x, (float) y, (float) z);
            }

            if (tileEntity.getCoreMarker() != null) {
                this.renderAnchor(tileEntity, (float) x, (float) y, (float) z);
            }
        }

        if (tileEntity.displayDistance) {
            this.renderDistanceMark(tileEntity);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    public void renderAnchor(TileEntityMarker tileEntity, float x, float y, float z) {
        this.checkKey(tileEntity);

        this.changeAnchor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);

        RailPosition rp = tileEntity.getMarkerRP();
        MarkerElement curElm = MarkerElement.values()[tileEntity.editMode];

        GL11.glPushMatrix();
        GL11.glRotatef(rp.anchorYaw, 0.0F, 1.0F, 0.0F);
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorOpaque_I(MarkerElement.HORIZONTIAL.getColor(curElm));
        tessellator.addVertex(0.0F, 0.0F, 0.0F);
        tessellator.addVertex(0.0F, 0.0F, rp.anchorLengthHorizontal);
        tessellator.draw();

        GL11.glPushMatrix();
        GL11.glRotatef(-rp.anchorPitch, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorOpaque_I(MarkerElement.VERTICAL.getColor(curElm));
        tessellator.addVertex(0.0F, 0.0F, 0.0F);
        tessellator.addVertex(0.0F, 0.0F, rp.anchorLengthVertical);
        tessellator.draw();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        float len = 1.0F;
        GL11.glRotatef(rp.cantEdge, 0.0F, 0.0F, 1.0F);
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorOpaque_I(MarkerElement.CANT.getColor(curElm));
        tessellator.addVertex(len, 0.0F, 0.0F);
        tessellator.addVertex(-len, 0.0F, 0.0F);
        tessellator.draw();
        GL11.glPopMatrix();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glRotatef(-RenderManager.instance.playerViewY + 180.0F, 0.0F, 1.0F, 0.0F);
        float scale = 0.03F;
        GL11.glScalef(scale, -scale, scale);
        FontRenderer renderer = NGTUtilClient.getMinecraft().fontRenderer;
        renderer.drawString("key=0 None", 0, -10, MarkerElement.NONE.getColor(curElm));
        renderer.drawString("key=1 Horizontial", 0, -20, MarkerElement.HORIZONTIAL.getColor(curElm));
        renderer.drawString("key=2 Vertical", 0, -30, MarkerElement.VERTICAL.getColor(curElm));
        renderer.drawString("key=3 Cant", 0, -40, MarkerElement.CANT.getColor(curElm));
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();

    }

    private void checkKey(TileEntityMarker tileEntity) {
        if (Minecraft.getMinecraft().inGameHasFocus) {
            Arrays.stream(MarkerElement.values()).filter(element -> Keyboard.isKeyDown(element.key)).findFirst().ifPresent(element -> tileEntity.editMode = element.ordinal());
        }
    }

    private void changeAnchor(TileEntityMarker tileEntity)//Minecraft.1518-this.objectMouseOver.hitVec
    {
        if (tileEntity.getCoreMarker() == null) {
            return;
        }

        Minecraft mc = NGTUtilClient.getMinecraft();
        if (!tileEntity.followMouseMoving || !mc.thePlayer.equals(tileEntity.followingPlayer)) {
            return;
        }

        MovingObjectPosition target = BlockUtil.getMOPFromPlayer(mc.thePlayer, 128.0D, true);
        if (target == null || target.typeOfHit != MovingObjectType.BLOCK) {
            return;
        }

        MarkerElement curElm = MarkerElement.values()[tileEntity.editMode];
        RailPosition rp = tileEntity.getMarkerRP();
        Vec3 vec3 = target.hitVec;
        boolean fitOpposite = false;

        RailPosition oppositeRP = this.getOppositeRail(tileEntity);
        if (oppositeRP != null) {
            double dSq = NGTMath.getDistanceSq(vec3.xCoord, vec3.zCoord, oppositeRP.posX, oppositeRP.posZ);
            if (dSq <= FIT_RANGE_SQ) {
                vec3 = Vec3.createVectorHelper(oppositeRP.posX, oppositeRP.posY, oppositeRP.posZ);
                fitOpposite = true;
            }
        }

        RailPosition neighborRP = this.getNeighborRail(tileEntity);

        double dx = vec3.xCoord - rp.posX;
        double dz = vec3.zCoord - rp.posZ;
        if (dx != 0.0D && dz != 0.0D) {
            float dirRad = (float) Math.atan2(dx, dz);
            float length = (float) (dx / MathHelper.sin(dirRad));
            float yaw = NGTMath.toDegrees(dirRad);

            if (curElm == MarkerElement.HORIZONTIAL) {
                if (neighborRP != null && tileEntity.fitNeighbor) {
                    yaw = MathHelper.wrapAngleTo180_float(neighborRP.anchorYaw + 180.0F);
                }
                rp.anchorYaw = yaw;
                rp.anchorLengthHorizontal = length;
            } else if (curElm == MarkerElement.VERTICAL) {
                float pitch = MathHelper.wrapAngleTo180_float(yaw - rp.anchorYaw);
                if (neighborRP != null && tileEntity.fitNeighbor) {
                    pitch = -neighborRP.anchorPitch;
                } else if (fitOpposite) {
                    double dy = vec3.yCoord - rp.posY;
                    pitch = (float) NGTMath.toDegrees(Math.atan2(dy, NGTMath.firstSqrt(dx * dx + dz * dz)));
                }
                rp.anchorPitch = pitch;
                rp.anchorLengthVertical = length;
            } else if (curElm == MarkerElement.CANT) {
                float cant = MathHelper.wrapAngleTo180_float(yaw - rp.anchorYaw);
                if (neighborRP != null && tileEntity.fitNeighbor) {
                    cant = -neighborRP.cantEdge;
                }
                rp.cantEdge = cant;
                RailMap map = tileEntity.getRailMaps()[0];
                float cantAve = (map.getStartRP().cantEdge + map.getEndRP().cantEdge) * 0.5F;
                map.getStartRP().cantCenter = map.getEndRP().cantCenter = cantAve;
            }

            tileEntity.getCoreMarker().updateRailMap();
        }
    }

    public enum MarkerElement {
        NONE(Keyboard.KEY_0, 0x000000),
        HORIZONTIAL(Keyboard.KEY_1, 0x00FF20),
        VERTICAL(Keyboard.KEY_2, 0xFF8800),
        CANT(Keyboard.KEY_3, 0xFF00FF);

        public final int key;
        public final int color;

        MarkerElement(int par1, int par2) {
            this.key = par1;
            this.color = par2;
        }

        public int getColor(MarkerElement par1) {
            boolean flag = (par1 == this) || par1 == MarkerElement.NONE;
            int r = (this.color >> 16 & 0xFF) / (flag ? 1 : 2);
            int g = (this.color >> 8 & 0xFF) / (flag ? 1 : 2);
            int b = (this.color & 0xFF) / (flag ? 1 : 2);
            return (r << 16) | (g << 8) | b;
        }
    }
}