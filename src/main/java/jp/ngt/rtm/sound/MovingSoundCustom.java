package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMConfig;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public abstract class MovingSoundCustom<T> extends MovingSound {
    protected final T entity;
    private float prevVolume = -1.0F;
    private float prevPitch = -1.0F;

    public MovingSoundCustom(T entity, ResourceLocation sound, boolean repeat, float range) {
        super(sound);
        this.entity = entity;
        this.repeat = repeat;
        this.field_147665_h = 0;
        this.field_147666_i = AttenuationType.LINEAR;
        this.volume = range / 16.0F;
    }

    @Override
    public void update() {
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
        float vol = par1 * RTMConfig.trainSoundVol;
        this.prevVolume = Math.max(vol, 0.0F);
    }

    public void setPitch(float par1) {
        this.prevPitch = Math.max(par1, 0.0F);
    }
}