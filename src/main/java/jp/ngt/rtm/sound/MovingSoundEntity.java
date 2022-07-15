package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMConfig;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundEntity extends MovingSound {
    protected final Entity entity;
    private float prevVolume = -1.0F;
    private float prevPitch = -1.0F;

    public MovingSoundEntity(Entity par1Entity, ResourceLocation par2Sound, boolean par3Repeat) {
        super(par2Sound);
        this.entity = par1Entity;
        this.repeat = par3Repeat;
        this.field_147665_h = 0;
        this.field_147666_i = AttenuationType.LINEAR;
    }

    @Override
    public void update() {
        if (this.entity.isDead) {
            this.donePlaying = true;
            return;
        }

        this.xPosF = (float) this.entity.posX;
        this.yPosF = (float) this.entity.posY;
        this.zPosF = (float) this.entity.posZ;

        if (this.prevVolume >= 0.0F) {
            this.volume = this.prevVolume;
            this.prevVolume = -1.0F;
        }

        if (this.prevPitch >= 0.0F) {
            this.field_147663_c = this.prevPitch;
            this.prevPitch = -1.0F;
        }
        //this.volume = RTMCore.trainSoundVol;
    }

    public void stop() {
        this.donePlaying = true;
    }

    public void setVolume(float par1) {
        float vol = par1 * RTMConfig.trainSoundVol;
        //this.prevVolume = vol < 0.0F ? 0.0F : (vol > 2.0F ? 2.0F : vol);
        this.prevVolume = Math.max(vol, 0.0F);
    }

    public void setPitch(float par1) {
        //this.prevPitch = par1 < 0.0F ? 0.0F : (par1 > 2.0F ? 2.0F : par1);
        this.prevPitch = Math.max(par1, 0.0F);
    }
}