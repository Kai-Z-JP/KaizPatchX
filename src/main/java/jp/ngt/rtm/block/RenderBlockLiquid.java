package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.RenderBlockLiquidBase;
import jp.ngt.rtm.RTMBlock;

@SideOnly(Side.CLIENT)
public class RenderBlockLiquid extends RenderBlockLiquidBase {
	@Override
	public int getRenderId() {
		return RTMBlock.renderIdLiquid;
	}
}