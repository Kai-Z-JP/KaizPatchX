package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.util.ColorUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.gui.InternalButton;
import jp.ngt.rtm.gui.InternalGUI;
import jp.ngt.rtm.network.PacketMarkerRPClient;
import jp.ngt.rtm.rail.util.MarkerState;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class RenderMarkerBlock1122 extends RenderMarkerBlockBase {

    private boolean clicking;

    private static final double FIT_RANGE_SQ = 4.0D;

    public boolean isGlobalRenderer(TileEntityMarker tileEntity) {
        return true;
    }

    public void renderTileEntityMarker(TileEntityMarker tileEntity, double par2, double par4, double par6, float par8) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GLHelper.disableLighting();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glTranslatef((float) par2, (float) par4, (float) par6);

        this.renderGUI(tileEntity);
        GL11.glDisable(3553);
        if (tileEntity.getState(MarkerState.GRID) && tileEntity.getGrid() != null) {
            this.renderGrid(tileEntity);
        }
        if (tileEntity.getState(MarkerState.LINE1) || tileEntity.getState(MarkerState.LINE2)) {
            RailPosition rp0 = tileEntity.getMarkerRP();
            double x = rp0.posX - (double) rp0.blockX;
            double y = rp0.posY - (double) rp0.blockY;
            double z = rp0.posZ - (double) rp0.blockZ;
            if (tileEntity.getState(MarkerState.LINE1) && tileEntity.getRailMaps() != null) {
                this.renderLine(tileEntity, (float) x, (float) y, (float) z);
            }
            if (tileEntity.getCoreMarker() != null) {
                this.renderAnchor(tileEntity, (float) x, (float) y, (float) z);
            }
        }
        if (tileEntity.getState(MarkerState.DISTANCE)) {
            this.renderDistanceMark(tileEntity);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLHelper.enableLighting();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void renderGUI(TileEntityMarker marker) {
        if (marker.gui == null) {
            int buttonColor = 61440;
            float buttonWidth = 2.8F;
            float buttonHeight = 0.5F;
            float guiHeight = (buttonHeight + 0.1F) * 5.0F + 0.1F;
            float startY = 0.5F;
            float startX = -(buttonWidth + 0.2F) / 2.0F;
            marker.gui = (new InternalGUI(startX, startY, buttonWidth + 0.2F, guiHeight)).setColor(65535);
            marker.buttons = new InternalButton[5];
            startX += 0.1F;
            startY += 0.1F;
            marker.buttons[0] = (new InternalButton(startX, startY, buttonWidth, buttonHeight)).setColor(buttonColor).setListner(button -> marker.flipState(MarkerState.ANCHOR21));
            startY += buttonHeight + 0.1F;
            marker.buttons[1] = (new InternalButton(startX, startY, buttonWidth, buttonHeight)).setColor(buttonColor).setListner(button -> System.nanoTime());
            startY += buttonHeight + 0.1F;
            marker.buttons[2] = (new InternalButton(startX, startY, buttonWidth, buttonHeight)).setColor(buttonColor).setListner(button -> marker.flipState(MarkerState.LINE1));
            startY += buttonHeight + 0.1F;
            marker.buttons[3] = (new InternalButton(startX, startY, buttonWidth, buttonHeight)).setColor(buttonColor).setListner(button -> marker.flipState(MarkerState.GRID));
            startY += buttonHeight + 0.1F;
            marker.buttons[4] = (new InternalButton(startX, startY, buttonWidth, buttonHeight)).setColor(buttonColor).setListner(button -> marker.flipState(MarkerState.DISTANCE));
            IntStream.range(0, marker.buttons.length).forEach(i -> marker.gui.addButton(marker.buttons[i]));
        }
        marker.buttons[0].setText(marker.getStateString(MarkerState.ANCHOR21), 16777215, 0.05F);
        marker.buttons[1].setText(marker.getStateString(MarkerState.LINE2), 16777215, 0.05F);
        marker.buttons[2].setText(marker.getStateString(MarkerState.LINE1), 16777215, 0.05F);
        marker.buttons[3].setText(marker.getStateString(MarkerState.GRID), 16777215, 0.05F);
        marker.buttons[4].setText(marker.getStateString(MarkerState.DISTANCE), 16777215, 0.05F);
        GL11.glPushMatrix();
        float y = 0.5F;
        if (marker.getState(MarkerState.LINE1)) {
            y = 1.0F;
        }
        if (marker.getState(MarkerState.LINE2) && y < (marker.getMarkerRP()).constLimitHP) {
            y = (marker.getMarkerRP()).constLimitHP;
        }
        GL11.glTranslatef(0.5F, y, 0.5F);
        GL11.glRotatef(-(RenderManager.instance.playerViewY) + 180.0F, 0.0F, 1.0F, 0.0F);
        marker.gui.render();
        GL11.glPopMatrix();
    }

    private void renderAnchor(TileEntityMarker marker, float x, float y, float z) {
        changeAnchor(marker);
        if (!Mouse.isButtonDown(1) && NGTUtilClient.getMinecraft().inGameHasFocus) {
            this.clicking = false;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        MarkerElement hoveredElement = MarkerElement.values()[marker.editMode];
        if (marker.editMode == 0) {
            hoveredElement = renderAnchorLine(marker, true, null);
        }
        if (marker.editMode == 0 && hoveredElement != MarkerElement.NONE && Mouse.isButtonDown(1) && !this.clicking && NGTUtilClient.getMinecraft().inGameHasFocus) {
            this.clicking = true;
            marker.editMode = hoveredElement.ordinal();
            marker.startPlayerPitch = (NGTUtilClient.getMinecraft()).thePlayer.rotationPitch;
            marker.startMarkerHeight = (marker.getMarkerRP()).height;
        }
        renderAnchorLine(marker, false, hoveredElement);
        GL11.glPopMatrix();
    }

    private MarkerElement renderAnchorLine(TileEntityMarker marker, boolean isPickMode, MarkerElement hoveredElement) {
        float lineWidth = (NGTUtilClient.getMinecraft()).displayHeight * 0.01F;
        if (isPickMode) {
            GLHelper.startMousePicking(lineWidth * 2.0F);
        }
        GL11.glDisable(3553);
        float prevPointSize = GL11.glGetFloat(2833);
        float prevLineWidth = GL11.glGetFloat(2849);
        GL11.glPointSize(lineWidth * 3.0F);
        GL11.glLineWidth(lineWidth);
        RailPosition rp = marker.getMarkerRP();
        int shadow = 12632256;
        if (marker.getState(MarkerState.LINE2) && marker.isCoreMarker()) {
            GL11.glPushMatrix();
            GL11.glRotatef(rp.anchorYaw, 0.0F, 1.0F, 0.0F);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.CONST_LIMIT_WP.ordinal());
            }
            int color = MarkerElement.CONST_LIMIT_WP.getColor();
            color = (hoveredElement == MarkerElement.CONST_LIMIT_WP) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(rp.constLimitWP, rp.constLimitHN, 0.0F, rp.constLimitWP, rp.constLimitHP, 0.0F, color);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.CONST_LIMIT_WN.ordinal());
            }
            color = MarkerElement.CONST_LIMIT_WN.getColor();
            color = (hoveredElement == MarkerElement.CONST_LIMIT_WN) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(rp.constLimitWN, rp.constLimitHP, 0.0F, rp.constLimitWN, rp.constLimitHN, 0.0F, color);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.CONST_LIMIT_HP.ordinal());
            }
            color = MarkerElement.CONST_LIMIT_HP.getColor();
            color = (hoveredElement == MarkerElement.CONST_LIMIT_HP) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(rp.constLimitWP, rp.constLimitHP, 0.0F, rp.constLimitWN, rp.constLimitHP, 0.0F, color);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.CONST_LIMIT_HN.ordinal());
            }
            color = MarkerElement.CONST_LIMIT_HN.getColor();
            color = (hoveredElement == MarkerElement.CONST_LIMIT_HN) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(rp.constLimitWN, rp.constLimitHN, 0.0F, rp.constLimitWP, rp.constLimitHN, 0.0F, color);
            GL11.glPopMatrix();
        }
        if (marker.getState(MarkerState.LINE1)) {
            GL11.glPushMatrix();
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.HEIGHT.ordinal());
            }
            int color = MarkerElement.HEIGHT.getColor();
            color = (hoveredElement == MarkerElement.HEIGHT) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, color);
            GL11.glRotatef(rp.anchorYaw, 0.0F, 1.0F, 0.0F);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.HORIZONTIAL.ordinal());
            }
            color = MarkerElement.HORIZONTIAL.getColor();
            color = (hoveredElement == MarkerElement.HORIZONTIAL) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, rp.anchorLengthHorizontal, color);
            GL11.glPushMatrix();
            GL11.glRotatef(-rp.anchorPitch, 1.0F, 0.0F, 0.0F);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.VERTICAL.ordinal());
            }
            color = MarkerElement.VERTICAL.getColor();
            color = (hoveredElement == MarkerElement.VERTICAL) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, rp.anchorLengthVertical, color);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            float len = 1.0F;
            GL11.glRotatef(rp.cantEdge, 0.0F, 0.0F, 1.0F);
            if (isPickMode) {
                GL11.glLoadName(MarkerElement.CANT_EDGE.ordinal());
            }
            color = MarkerElement.CANT_EDGE.getColor();
            color = (hoveredElement == MarkerElement.CANT_EDGE) ? ColorUtil.multiplicating(color, shadow) : color;
            renderLine(0.0F, 0.0F, 0.0F, len, 0.0F, 0.0F, color);
            renderLine(0.0F, 0.0F, 0.0F, -len, 0.0F, 0.0F, color);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            if (marker.isCoreMarker() && marker.getRailMaps() != null && (marker.getRailMaps()).length == 1) {
                RailMap rm = marker.getRailMaps()[0];
                int max = (int) ((float) rm.getLength() * 2.0F);
                int index = max / 2;
                double[] pos0 = rm.getRailPos(max, 0);
                double[] pos = rm.getRailPos(max, index);
                double h0 = rm.getRailHeight(max, 0);
                double h = rm.getRailHeight(max, index);
                float yaw0 = rm.getRailRotation(max, 0);
                float yaw = rm.getRailRotation(max, index);
                GL11.glRotatef(-rp.anchorYaw, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef((float) (pos[1] - pos0[1]), (float) (h - h0), (float) (pos[0] - pos0[0]));
                GL11.glRotatef(rp.anchorYaw - yaw0 + yaw, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(rp.cantCenter, 0.0F, 0.0F, 1.0F);
                if (isPickMode) {
                    GL11.glLoadName(MarkerElement.CANT_CENTER.ordinal());
                }
                color = MarkerElement.CANT_CENTER.getColor();
                color = (hoveredElement == MarkerElement.CANT_CENTER) ? ColorUtil.multiplicating(color, shadow) : color;
                renderLine(0.0F, 0.0F, 0.0F, len, 0.0F, 0.0F, color);
                renderLine(0.0F, 0.0F, 0.0F, -len, 0.0F, 0.0F, color);
            }
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
        GL11.glPointSize(prevPointSize);
        GL11.glLineWidth(prevLineWidth);
        GL11.glEnable(3553);
        if (marker.getState(MarkerState.LINE1) && !isPickMode) {
            FontRenderer fontRenderer = NGTUtilClient.getMinecraft().fontRenderer;
            float scale = 0.04F;
            GL11.glPushMatrix();
            GL11.glRotatef(-(RenderManager.instance).playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-scale, -scale, scale);
            float x = 3.0F;
            float y = -34.0F;
            fontRenderer.drawString(String.valueOf(rp.height), MathHelper.floor_float(x), MathHelper.floor_float(y), MarkerElement.HEIGHT.getColor(), false);
            y += 6.0F;
            fontRenderer.drawString(String.valueOf(rp.anchorYaw), MathHelper.floor_float(x), MathHelper.floor_float(y), MarkerElement.HORIZONTIAL.getColor(), false);
            y += 6.0F;
            fontRenderer.drawString(String.valueOf(rp.anchorPitch), MathHelper.floor_float(x), MathHelper.floor_float(y), MarkerElement.VERTICAL.getColor(), false);
            y += 6.0F;
            fontRenderer.drawString(String.valueOf(rp.cantEdge), MathHelper.floor_float(x), MathHelper.floor_float(y), MarkerElement.CANT_EDGE.getColor(), false);
            y += 6.0F;
            fontRenderer.drawString(String.valueOf(rp.cantCenter), MathHelper.floor_float(x), MathHelper.floor_float(y), MarkerElement.CANT_CENTER.getColor(), false);
            GL11.glPopMatrix();
        }
        if (isPickMode) {
            int hits = GLHelper.finishMousePicking();
            if (hits > 0) {
                int pickedId = GLHelper.getPickedObjId(0);
                return MarkerElement.values()[pickedId];
            }
        }
        return MarkerElement.NONE;
    }

    private void renderLine(float startX, float startY, float startZ, float endX, float endY, float endZ, int color) {
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawing(1);
        tessellator.setColorOpaque_I(color);
        tessellator.addVertex(startX, startY, startZ);
        tessellator.addVertex(endX, endY, endZ);
        tessellator.draw();
        tessellator.startDrawing(0);
        tessellator.setColorOpaque_I(color);
        tessellator.addVertex(endX, endY, endZ);
        tessellator.draw();
    }

    private boolean changeAnchor(TileEntityMarker marker) {
        if (marker.editMode == 0 || marker.getCoreMarker() == null)
            return false;
        if (marker.editMode > 0 && Mouse.isButtonDown(1) && !this.clicking && NGTUtilClient.getMinecraft().inGameHasFocus) {
            this.clicking = true;
            marker.editMode = 0;
            RTMCore.NETWORK_WRAPPER.sendToServer(new PacketMarkerRPClient(marker.getCoreMarker()));
            return false;
        }
        MarkerElement curElm = MarkerElement.values()[marker.editMode];
        Minecraft mc = NGTUtilClient.getMinecraft();
        RailPosition rp = marker.getMarkerRP();
        float pitchDif = mc.thePlayer.rotationPitch - marker.startPlayerPitch;
        float yawDif = mc.thePlayer.rotationYawHead - marker.startPlayerYaw;
        if (marker.getState(MarkerState.LINE1)) {
            if (curElm == MarkerElement.HEIGHT) {
                int height = marker.startMarkerHeight + (int) (-pitchDif / 1.0F);
                height = (height < 0) ? 0 : (Math.min(height, 15));
                if (height != (marker.getMarkerRP()).height) {
                    rp.height = (byte) height;
                    rp.init();
                    marker.onChangeRailShape();
                    return true;
                }
                return false;
            } else if (curElm == MarkerElement.CANT_EDGE) {
                float cantLimit = 80.0F;
                float cant = (pitchDif < -cantLimit) ? -cantLimit : (Math.min(pitchDif, cantLimit));
                RailPosition neighborRP = getNeighborRail(marker);
                if (neighborRP != null && marker.fitNeighbor) {
                    cant = -neighborRP.cantEdge;
                }
                rp.cantEdge = cant;
                RailMap map = marker.getRailMaps()[0];
                float cantCenter = ((map.getStartRP()).cantEdge + -(map.getEndRP()).cantEdge) * 0.5F;
                map.getStartRP().cantCenter = map.getEndRP().cantCenter = cantCenter * ((rp.cantCenter == map.getStartRP().cantCenter) ? 1 : -1);
                marker.onChangeRailShape();
                return true;
            } else if (curElm == MarkerElement.CANT_CENTER) {
                float cantLimit = 80.0F;
                float cantCenter = (pitchDif < -cantLimit) ? -cantLimit : (Math.min(pitchDif, cantLimit));
                RailMap map = marker.getRailMaps()[0];
                map.getStartRP().cantCenter = map.getEndRP().cantCenter = cantCenter * ((rp.cantCenter == map.getStartRP().cantCenter) ? 1 : -1);
                marker.onChangeRailShape();
                return true;
            }
        }
        if (marker.getState(MarkerState.LINE2)) {
            RailMap map = marker.getRailMaps()[0];
            if (curElm == MarkerElement.CONST_LIMIT_HP) {
                float height = 3.0F + -pitchDif / 10.0F;
                height = Math.max(height, 1.9F);
                (map.getEndRP()).constLimitHP = height;
                marker.onChangeRailShape();
                return true;
            } else if (curElm == MarkerElement.CONST_LIMIT_HN) {
                float height = -pitchDif / 10.0F;
                height = Math.min(height, 0.0F);
                (map.getEndRP()).constLimitHN = height;
                marker.onChangeRailShape();
                return true;
            } else if (curElm == MarkerElement.CONST_LIMIT_WP) {
                float width = 1.5F + -yawDif / 10.0F;
                width = Math.max(width, 0.49F);
                (map.getEndRP()).constLimitWP = width;
                marker.onChangeRailShape();
                return true;
            } else if (curElm == MarkerElement.CONST_LIMIT_WN) {
                float width = -1.5F + -yawDif / 10.0F;
                width = Math.min(width, -0.49F);
                (map.getEndRP()).constLimitWN = width;
                marker.onChangeRailShape();
                return true;
            }
        }
        if (marker.getState(MarkerState.LINE1)) {
            MovingObjectPosition target = BlockUtil.getMOPFromPlayer(mc.thePlayer, 128.0D, true);
            if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return false;
            }
            Vec3 targetVec = target.hitVec;
            boolean fitOpposite = false;
            RailPosition oppositeRP = getOppositeRail(marker);
            if (oppositeRP != null) {
                double dSq = NGTMath.getDistanceSq(targetVec.xCoord, targetVec.zCoord, oppositeRP.posX, oppositeRP.posZ);
                if (dSq <= 4.0D) {
                    targetVec = Vec3.createVectorHelper(oppositeRP.posX, oppositeRP.posY, oppositeRP.posZ);
                    fitOpposite = true;
                }
            }
            if (marker.getState(MarkerState.ANCHOR21)) {
                double d0 = 0.6666666666666666D;
                double x = (targetVec.xCoord - rp.posX) * d0 + rp.posX;
                double y = (targetVec.yCoord - rp.posY) * d0 + rp.posY;
                double z = (targetVec.zCoord - rp.posZ) * d0 + rp.posZ;
                targetVec = Vec3.createVectorHelper(x, y, z);
            }
            double dx = targetVec.xCoord - rp.posX;
            double dz = targetVec.zCoord - rp.posZ;
            if (dx != 0.0D && dz != 0.0D) {
                RailPosition neighborRP = getNeighborRail(marker);
                float dirRad = (float) Math.atan2(dx, dz);
                float length = (float) (dx / MathHelper.sin(dirRad));
                float yaw = NGTMath.toDegrees(dirRad);
                if (curElm == MarkerElement.HORIZONTIAL) {
                    if (neighborRP != null && marker.fitNeighbor) {
                        yaw = MathHelper.wrapAngleTo180_float(neighborRP.anchorYaw + 180.0F);
                    }
                    rp.anchorYaw = yaw;
                    rp.anchorLengthHorizontal = length;
                } else if (curElm == MarkerElement.VERTICAL) {
                    float pitch = MathHelper.wrapAngleTo180_float(yaw - rp.anchorYaw);
                    if (neighborRP != null && marker.fitNeighbor) {
                        pitch = -neighborRP.anchorPitch;
                    } else if (fitOpposite) {
                        double dy = targetVec.yCoord - rp.posY;
                        pitch = (float) NGTMath.toDegrees(Math.atan2(dy, NGTMath.firstSqrt(dx * dx + dz * dz)));
                    }
                    rp.anchorPitch = pitch;
                    rp.anchorLengthVertical = length;
                }
                marker.onChangeRailShape();
                return true;
            }
        }
        return false;
    }

    public enum MarkerElement {
        NONE(0),
        HORIZONTIAL(65312),
        VERTICAL(16746496),
        CANT_EDGE(16711935),
        CANT_CENTER(16711935),
        HEIGHT(16715776),
        CONST_LIMIT_HP(1073407),
        CONST_LIMIT_HN(1073407),
        CONST_LIMIT_WP(1073407),
        CONST_LIMIT_WN(1073407);

        public final int color;

        MarkerElement(int par2) {
            this.color = par2;
        }

        public int getColor() {
            return this.color;
        }
    }
}
