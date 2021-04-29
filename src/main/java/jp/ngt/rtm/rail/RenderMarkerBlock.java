package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@SideOnly(Side.CLIENT)
public class RenderMarkerBlock extends TileEntitySpecialRenderer {
    private static final RenderMarkerBlock1122 INSTANCE1122 = new RenderMarkerBlock1122();
    private static final RenderMarkerBlock1710 INSTANCE1710 = new RenderMarkerBlock1710();

    @Override
    public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8) {
        (RTMCore.use1122Marker ? INSTANCE1122 : INSTANCE1710).renderTileEntityMarker((TileEntityMarker) par1TileEntity, par2, par4, par6, par8);
    }
}