package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundTileEntity extends MovingSoundCustom<TileEntity> {

    public MovingSoundTileEntity(TileEntity par1Entity, ResourceLocation par2Sound, boolean par3Repeat) {
        this(par1Entity, par2Sound, par3Repeat, 16.0F);
    }

    public MovingSoundTileEntity(TileEntity par1Entity, ResourceLocation par2Sound, boolean par3Repeat, float range) {
        super(par1Entity, par2Sound, par3Repeat, range);

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

        super.update();
    }
}