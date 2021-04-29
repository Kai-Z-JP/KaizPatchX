package jp.ngt.rtm.item;

import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.electric.TileEntitySignal;
import jp.ngt.rtm.modelpack.cfg.SignalConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemSignal extends ItemWithModel {
    public ItemSignal() {
        super();
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            int x = par4;
            int z = par6;
            boolean flag = false;

            if (par7 == 0)//up
            {
                //--y;
                return true;
            } else if (par7 == 1)//down
            {
                //++y;
                return true;
            } else if (par7 == 2)//south
            {
                --z;
            } else if (par7 == 3)//north
            {
                ++z;
            } else if (par7 == 4)//east
            {
                --x;
            } else if (par7 == 5)//west
            {
                ++x;
            }

            Block target = world.getBlock(par4, par5, par6);
            if (target == RTMBlock.signal) {
                return true;
            }

            int meta = world.getBlockMetadata(par4, par5, par6);
            world.setBlock(par4, par5, par6, RTMBlock.signal, meta, 3);

            TileEntity tile = world.getTileEntity(par4, par5, par6);
            if (tile instanceof TileEntitySignal) {
                TileEntitySignal teSignal = ((TileEntitySignal) tile);
                int dir = par7 == 2 ? 2 : (par7 == 4 ? 3 : (par7 == 3 ? 0 : 1));
                teSignal.setSignalProperty(this.getModelName(itemStack), target, dir, player);
                flag = true;
            }

            if (flag) {
                Block block = RTMBlock.signal;
                world.playSoundEffect((double) par4 + 0.5D, (double) par5 + 0.5D, (double) par6 + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --itemStack.stackSize;
            }

        }
        return true;
    }

    @Override
    protected String getModelType(ItemStack itemStack) {
        return SignalConfig.TYPE;
    }

    @Override
    protected String getDefaultModelName(ItemStack itemStack) {
        return "4colorB";
    }
}