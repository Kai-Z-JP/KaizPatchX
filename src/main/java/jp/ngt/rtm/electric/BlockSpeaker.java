package jp.ngt.rtm.electric;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.BlockMachineBase;
import jp.ngt.rtm.item.ItemInstalledObject;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSpeaker extends BlockMachineBase implements IBlockConnective {
    public BlockSpeaker() {
        super(Material.glass);
        this.setStepSound(soundTypeGlass);
    }

    public TileEntity createNewTileEntity(World world, int var2) {
        return new TileEntitySpeaker();
    }

    protected ItemStack getItem(int meta) {
        return new ItemStack(RTMItem.installedObject, 1, ItemInstalledObject.IstlObjType.SPEAKER.id);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (player.isSneaking()) {
            super.onBlockActivated(world, x, y, z, player, p_149727_6_, p_149727_7_, p_149727_8_, p_149727_9_);
        } else if (world.isRemote) {
            player.openGui(RTMCore.instance, RTMCore.guiIdSpeaker, world, x, y, z);
        }
        return true;
    }

    public boolean canConnect(World world, int x, int y, int z) {
        return true;
    }
}
