package jp.ngt.rtm.util;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.AxisAlignedBB;

@Deprecated
public class MirrorFrustrum implements ICamera {
	public MirrorFrustrum() {
		;
	}

	@Override
	public boolean isBoundingBoxInFrustum(AxisAlignedBB aabb) {
		return false;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		;
	}
}