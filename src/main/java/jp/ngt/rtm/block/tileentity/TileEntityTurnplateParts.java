package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import net.minecraft.block.Block;

public class TileEntityTurnplateParts extends TileEntityLargeRailBase {
	@Override
	public void updateEntity() {
		super.updateEntity();
	}

	@Override
	public Block getBlockType() {
		if (this.blockType == null) {
			Block block = this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord);
			this.blockType = block;
		}
		return this.blockType;
	}
}