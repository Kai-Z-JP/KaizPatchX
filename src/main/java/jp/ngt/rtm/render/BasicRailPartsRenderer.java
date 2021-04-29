package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;

@SideOnly(Side.CLIENT)
public class BasicRailPartsRenderer extends RailPartsRenderer {
    @Override
    public void init(ModelSetRailClient par1, ModelObject par2) {
        super.init(par1, par2);
    }

    @Override
    protected void renderRailStatic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
        this.renderStaticParts(tileEntity, x, y, z);
    }

    @Override
    protected void renderRailDynamic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
    }

    @Override
    protected boolean shouldRenderObject(TileEntityLargeRailCore tileEntity, String objName, int len, int pos) {
        return true;
    }
}