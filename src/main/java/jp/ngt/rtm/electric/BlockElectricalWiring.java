package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemInstalledObject;
import jp.ngt.rtm.item.ItemWithModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class BlockElectricalWiring extends BlockContainer implements IBlockConnective {
    protected BlockElectricalWiring(Material material) {
        super(material);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (world.isRemote) {
            if (NGTUtil.isEquippedItem(player, RTMItem.installedObject)) {
                ItemStack itemStack = player.getCurrentEquippedItem();
                ItemInstalledObject itemInst = (ItemInstalledObject) player.getCurrentEquippedItem().getItem();
                TileEntityConnectorBase connector = (TileEntityConnectorBase) world.getTileEntity(x, y, z);
                if (connector.getSubType().equals(itemInst.getSubType(itemStack))) {
                    player.openGui(RTMCore.instance, RTMCore.guiIdChangeOffset, player.worldObj, x, y, z);
                    return true;
                }
            }
            return true;
        } else {
            if (NGTUtil.isEquippedItem(player, RTMItem.installedObject)) {
                ItemStack itemStack = player.getCurrentEquippedItem();
                ItemInstalledObject itemInst = (ItemInstalledObject) player.getCurrentEquippedItem().getItem();
                TileEntityConnectorBase connector = (TileEntityConnectorBase) world.getTileEntity(x, y, z);
                if (connector.getSubType().equals(itemInst.getSubType(itemStack))) {
                    return true;
                }
            }
            TileEntityElectricalWiring tile = (TileEntityElectricalWiring) world.getTileEntity(x, y, z);
            return tile.onRightClick(player);
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
        }
        return itemStack;
    }

    protected abstract ItemStack getItem(int damage);
}