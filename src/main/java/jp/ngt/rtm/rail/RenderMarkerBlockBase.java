package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public abstract class RenderMarkerBlockBase {
    protected final String[] displayStrings = new String[RTMConfig.markerDisplayDistance / 10];

    public RenderMarkerBlockBase() {
        IntStream.range(0, this.displayStrings.length).forEach(i -> this.displayStrings[i] = (i + 1) * 10 + "m");
    }

    public abstract void renderTileEntityMarker(TileEntityMarker tileEntity, double par2, double par4, double par6, float par8);

    protected void renderDistanceMark(TileEntityMarker marker) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.5F, 0.0625F, 0.5F);
        int meta = marker.getBlockMetadata();
        Block block = marker.getBlockType();
        int color = block == RTMBlock.marker ? 0xFF0000 : 0x0000FF;
        float dir = BlockMarker.getMarkerDir(marker.getBlockType(), meta) * 45.0F;
        GL11.glRotatef(dir, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float size = 0.4F;
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(color);
        IntStream.range(1, this.displayStrings.length).mapToObj(i -> i * 10.0F).forEach(moveZ ->
                IntStream.rangeClosed(-1, 1)
                        .mapToObj(k -> moveZ * k)
                        .forEach(moveX -> {
                            if (!RTMConfig.markerDistanceMoreRealPosition) {
                                tessellator.addVertex(-size + moveX, 0.0F, size + moveZ);
                                tessellator.addVertex(-size + moveX, 0.0F, -size + moveZ);
                                tessellator.addVertex(size + moveX, 0.0F, -size + moveZ);
                                tessellator.addVertex(size + moveX, 0.0F, size + moveZ);
                            } else {
                                tessellator.addVertex(-0.4F + moveX, 0.0F, -0.4F + moveZ);
                                tessellator.addVertex(-0.4F + moveX, 0.0F, -0.6F + moveZ);
                                tessellator.addVertex(0.4F + moveX, 0.0F, -0.6F + moveZ);
                                tessellator.addVertex(0.4F + moveX, 0.0F, -0.4F + moveZ);
                                tessellator.addVertex(-0.1F + moveX, 0.0F, -0.1F + moveZ);
                                tessellator.addVertex(-0.1F + moveX, 0.0F, -0.9F + moveZ);
                                tessellator.addVertex(0.1F + moveX, 0.0F, -0.9F + moveZ);
                                tessellator.addVertex(0.1F + moveX, 0.0F, -0.1F + moveZ);
                            }
                        }));
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
        for (int j = 0; j < this.displayStrings.length; j++) {
            float moveZ = (j + 1) * 10.0F;
            for (int k = -1; k <= 1; k++) {
                float moveX = moveZ * k;
                GL11.glPushMatrix();
                if (!RTMConfig.markerDistanceMoreRealPosition) {
                    GL11.glTranslatef(moveX, 0.0F, moveZ);
                } else {
                    GL11.glTranslatef(moveX, 0.0F, moveZ - 0.5F);
                }
                GL11.glRotatef(-RenderManager.instance.playerViewY - dir, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(-0.25F, -0.25F, 0.25F);
                String s = this.displayStrings[j];
                int stringWidth = fontRenderer.getStringWidth(s);
                fontRenderer.drawString(s, -stringWidth / 2, -10, color);
                GL11.glPopMatrix();
            }
        }
        GL11.glPopMatrix();
    }

    protected void renderLine(TileEntityMarker tileEntity, float x, float y, float z) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);

        Tessellator tessellator = Tessellator.instance;
        for (RailMap rm : tileEntity.getRailMaps()) {
            GL11.glPushMatrix();
            float x0 = (float) (rm.getStartRP().posX - tileEntity.getMarkerRP().posX);
            float y0 = (float) (rm.getStartRP().posY - tileEntity.getMarkerRP().posY);
            float z0 = (float) (rm.getStartRP().posZ - tileEntity.getMarkerRP().posZ);
            GL11.glTranslatef(x0, y0, z0);
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
            tessellator.setColorOpaque_I(0x004000);
            int max = (int) ((float) rm.getLength() * 2.0F);
            double[] p2 = rm.getRailPos(max, 0);
            double h2 = rm.getRailHeight(max, 0);
            for (int i = 0; i < max + 1; ++i) {
                double[] p1 = rm.getRailPos(max, i);
                tessellator.addVertex(p1[1] - p2[1], rm.getRailHeight(max, i) - h2, p1[0] - p2[0]);
            }
            tessellator.draw();
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
    }

    protected void renderGrid(TileEntityMarker marker) {
        GL11.glPushMatrix();
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(1);
        tessellator.setColorOpaque_I(0);
        for (int[] ia : marker.getGrid()) {
            renderFrame(tessellator, (ia[0] - marker.xCoord), (ia[1] - marker.yCoord), (ia[2] - marker.zCoord), 1.0F, 1.0F, 1.0F);
        }
        tessellator.draw();
        GL11.glPopMatrix();
    }

    protected RailPosition getOppositeRail(TileEntityMarker tileEntity) {
        if (tileEntity.getRailMaps() == null) {
            return null;
        }

        RailPosition rp = tileEntity.getMarkerRP();
        RailPosition oppositeRP = null;
        for (RailMap map : tileEntity.getRailMaps()) {
            if (map.getStartRP().equals(rp)) {
                oppositeRP = map.getEndRP();
                break;
            } else if (map.getEndRP().equals(rp)) {
                oppositeRP = map.getStartRP();
                break;
            }
        }
        return oppositeRP;
    }

    protected RailPosition getNeighborRail(TileEntityMarker tileEntity) {
        int[] pos = tileEntity.getMarkerRP().getNeighborPos();
        TileEntity tile = tileEntity.getWorldObj().getTileEntity(pos[0], pos[1], pos[2]);
        if (!(tile instanceof TileEntityLargeRailBase)) {
            return null;
        }

        TileEntityLargeRailCore core = ((TileEntityLargeRailBase) tile).getRailCore();
        if (core == null) {
            return null;
        }

        double distanceSq = Double.MAX_VALUE;
        RailPosition rp = null;
        for (RailMap map : core.getAllRailMaps()) {
            double d2 = NGTMath.getDistanceSq(tileEntity.getMarkerRP().posX, tileEntity.getMarkerRP().posZ, map.getStartRP().posX, map.getStartRP().posZ);
            if (d2 < distanceSq) {
                distanceSq = d2;
                rp = map.getStartRP();
            }

            d2 = NGTMath.getDistanceSq(tileEntity.getMarkerRP().posX, tileEntity.getMarkerRP().posZ, map.getEndRP().posX, map.getEndRP().posZ);
            if (d2 < distanceSq) {
                distanceSq = d2;
                rp = map.getEndRP();
            }
        }

        return rp;
    }

    protected void renderFrame(Tessellator tessellator, double minX, double minY, double minZ, double width, double height, double depth) {
        double maxX = minX + width;
        double maxY = minY + height;
        double maxZ = minZ + depth;

        tessellator.addVertex(minX, minY, minZ);//minY
        tessellator.addVertex(maxX, minY, minZ);

        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(maxX, minY, maxZ);

        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(minX, minY, maxZ);

        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, minY, maxZ);

        tessellator.addVertex(minX, minY, minZ);//tate
        tessellator.addVertex(minX, maxY, minZ);

        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);

        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);

        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.addVertex(maxX, maxY, maxZ);

        tessellator.addVertex(minX, maxY, minZ);//maxY
        tessellator.addVertex(maxX, maxY, minZ);

        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.addVertex(maxX, maxY, maxZ);

        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(minX, maxY, maxZ);

        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, maxZ);
    }
}