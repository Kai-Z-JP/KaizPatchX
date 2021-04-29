package jp.ngt.ngtlib.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityCustom extends TileEntity {
    public void setPos(int x, int y, int z, int prevX, int prevY, int prevZ) {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}