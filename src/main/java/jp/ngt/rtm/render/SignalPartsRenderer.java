package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.electric.TileEntitySignal;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignalClient;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

@SideOnly(Side.CLIENT)
public class SignalPartsRenderer extends TileEntityPartsRenderer<ModelSetSignalClient> {
    public SignalPartsRenderer(String... par1) {
        super(par1);
    }

    public int getTick(TileEntity par1) {
        return par1 == null ? 0 : ((TileEntitySignal) par1).tick;
    }

    public float getBlockDirection(TileEntity par1) {
        if (par1 == null) {
            return 0.0F;
        }
        return ((TileEntitySignal) par1).getBlockDirection();
    }

    public float getPitch(TileEntity par1) {
        if (par1 == null) {
            return 0.0F;
        }
        return ((TileEntitySignal) par1).getRotationPitch();
    }

    public float getRoll(TileEntity par1) {
        if (par1 == null) {
            return 0.0F;
        }
        return ((TileEntitySignal) par1).getRotationRoll();
    }

    public float getRotation(TileEntity par1) {
        if (par1 == null) {
            return 0.0F;
        }
        return ((TileEntitySignal) par1).getRotationYaw();
    }

    public int getSignal(TileEntity par1) {
        if (par1 == null) {
            return 0;
        }
        return ((TileEntitySignal) par1).getSignal();
    }

    public Block getBlock(TileEntity par1) {
        if (par1 == null) {
            return Blocks.air;
        }
        return ((TileEntitySignal) par1).getRenderBlock();
    }

    public boolean isOpaqueCube(TileEntity par1) {
        return this.getBlock(par1).isOpaqueCube();
    }
}