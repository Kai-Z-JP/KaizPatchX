package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityInsulator extends TileEntityConnectorBase {
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return NGTUtil.getChunkLoadDistanceSq();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord - 32, this.yCoord - 16, this.zCoord - 32, this.xCoord + 32, this.yCoord + 16, this.zCoord + 32);
		return bb;
	}

	@Override
	public String getSubType() {
		return "Relay";
	}

	@Override
	protected String getDefaultName() {
		return "Insulator01";
	}
}