package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.sound.SoundPlayer;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityCrossingGate extends TileEntityMachineBase {
	/**
	 * 棒の動き
	 */
	public int barMoveCount = 0;
	/**
	 * ライト点滅
	 */
	public int lightCount = -1;
	private int tickCountOnActive;

	private final SoundPlayer soundPlayer = SoundPlayer.create();//NoClassナントカ対策

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord)) {
			if (this.barMoveCount < 90) {
				++this.barMoveCount;
			}

			if (this.tickCountOnActive < 360) {
				++this.tickCountOnActive;
			} else {
				this.tickCountOnActive = 0;
			}

			if (this.tickCountOnActive % 10 == 0) {
				this.lightCount = this.lightCount <= 0 ? 1 : 0;
			}

			if (this.worldObj.isRemote) {
				if (!this.soundPlayer.isPlaying() && this.getModelSet().sound_Running != null) {
					this.soundPlayer.playSound(this, this.getModelSet().sound_Running, true);
				}
			}
		} else {
			if (this.barMoveCount > 0) {
				--this.barMoveCount;
			}

			this.tickCountOnActive = 0;
			this.lightCount = -1;

			if (this.worldObj.isRemote) {
				if (this.soundPlayer.isPlaying()) {
					this.soundPlayer.stopSound();
				}
			}
		}
	}

	@Override
	public void onActivate() {
	}

	@Override
	public void setModelName(String par1) {
		super.setModelName(par1);

		if (this.worldObj != null && this.worldObj.isRemote) {
			if (this.soundPlayer.isPlaying()) {
				this.soundPlayer.stopSound();
			}
		}
	}

	@Override
	public MachineType getMachinleType() {
		return MachineType.Gate;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		float[] box = this.getResourceState().getResourceSet().getConfig().renderAABB;
		return AxisAlignedBB.getBoundingBox(
				this.xCoord + box[0], this.yCoord + box[1], this.zCoord + box[2], this.xCoord + box[3], this.yCoord + box[4], this.zCoord + box[5]);
	}

	@Override
	protected String getDefaultName() {
		return "CrossingGate01R";
	}
}