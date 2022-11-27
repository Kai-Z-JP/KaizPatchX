package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundEntity extends MovingSoundCustom<Entity> {

    public MovingSoundEntity(Entity par1Entity, ResourceLocation par2Sound, boolean par3Repeat) {
        this(par1Entity, par2Sound, par3Repeat, 16.0F);
    }

    public MovingSoundEntity(Entity par1Entity, ResourceLocation par2Sound, boolean par3Repeat, float range) {
        super(par1Entity, par2Sound, par3Repeat, range);
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

        super.update();
    }
}