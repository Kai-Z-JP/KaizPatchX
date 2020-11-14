package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundTileEntity extends MovingSound {
	protected final TileEntity entity;
	private float prevVolume = -1.0F;
	private float prevPitch = -1.0F;

	public MovingSoundTileEntity(TileEntity par1Entity, ResourceLocation par2Sound, boolean par3Repeat) {
		super(par2Sound);
		this.entity = par1Entity;
		this.repeat = par3Repeat;
		this.field_147665_h = 0;
		this.field_147666_i = AttenuationType.LINEAR;//減衰率, NONEで音量変わらず

		this.xPosF = (float) this.entity.xCoord + 0.5F;
		this.yPosF = (float) this.entity.yCoord + 0.5F;
		this.zPosF = (float) this.entity.zCoord + 0.5F;
	}

	@Override
	public void update() {
		if (this.entity.isInvalid()) {
			this.donePlaying = true;
			return;
		}

		if (this.prevVolume >= 0.0F) {
			this.volume = this.prevVolume;
			this.prevVolume = -1.0F;
		}

		if (this.prevPitch >= 0.0F) {
			this.field_147663_c = this.prevPitch;
			this.prevPitch = -1.0F;
		}
	}

	public void stop() {
		this.donePlaying = true;
	}

	public void setVolume(float par1) {
        float vol = par1 * RTMCore.trainSoundVol;
        this.prevVolume = Math.max(vol, 0.0F);
    }

	public void setPitch(float par1) {
        this.prevPitch = Math.max(par1, 0.0F);
    }
}