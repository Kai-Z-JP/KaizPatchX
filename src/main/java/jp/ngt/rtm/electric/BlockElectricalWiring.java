package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemWithModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public abstract class BlockElectricalWiring extends BlockContainer implements IBlockConnective {
    protected BlockElectricalWiring(Material material) {
        super(material);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (world.isRemote) {
            if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
                player.openGui(RTMCore.instance, RTMCore.guiIdChangeOffset, player.worldObj, x, y, z);
            }
            return true;
        } else {
            if (!NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
                TileEntityElectricalWiring tile = (TileEntityElectricalWiring) world.getTileEntity(x, y, z);
                return tile.onRightClick(player);
            } else {
                return true;
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        TileEntityElectricalWiring tile = (TileEntityElectricalWiring) world.getTileEntity(x, y, z);
        tile.onBlockBreaked();
        super.breakBlock(world, x, y, z, block, par6);//removeTileEntity
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        int meta = world.getBlockMetadata(x, y, z);
        ItemStack itemStack = this.getItem(meta);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityConnectorBase) {
            ((ItemWithModel) RTMItem.installedObject).setModelName(itemStack, ((TileEntityConnectorBase) tileEntity).getModelName());
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                ItemWithModel.copyOffsetToItemStack((TileEntityPlaceable) tileEntity, itemStack);
            }
        }
        return itemStack;
    }

    protected abstract ItemStack getItem(int damage);
}