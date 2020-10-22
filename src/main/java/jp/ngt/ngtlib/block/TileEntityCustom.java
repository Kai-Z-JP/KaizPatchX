package jp.ngt.ngtlib.block;

import net.minecraft.tileentity.TileEntity;

public class TileEntityCustom extends TileEntity {
	public void setPos(int x, int y, int z, int prevX, int prevY, int prevZ) {
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
	}
}