package jp.ngt.rtm.block;

import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityOrnament;
import jp.ngt.rtm.item.ItemInstalledObject;
import jp.ngt.rtm.item.ItemWithModel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public abstract class BlockOrnamentBase extends BlockContainer {
    protected BlockOrnamentBase(Material material) {
        super(material);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (world.isRemote) {
            if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
                player.openGui(RTMCore.instance, RTMCore.guiIdChangeOffset, player.worldObj, x, y, z);
                return true;
            }

            if (player.isSneaking()) {
                player.openGui(RTMCore.instance, RTMCore.guiIdSelectTileEntityModel, player.worldObj, x, y, z);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityOrnament) {
            OrnamentType machineType = ((TileEntityOrnament) tileEntity).getOrnamentType();
            ItemStack itemStack = new ItemStack(RTMItem.installedObject);
            itemStack.setItemDamage(ItemInstalledObject.IstlObjType.getType(machineType).id);
            ((ItemInstalledObject) RTMItem.installedObject).setModelName(itemStack, ((TileEntityOrnament) tileEntity).getModelName());
            ((ItemInstalledObject) RTMItem.installedObject).setModelState(itemStack, ((TileEntityOrnament) tileEntity).getResourceState());

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                ItemWithModel.copyOffsetToItemStack((TileEntityPlaceable) tileEntity, itemStack);
            }
            return itemStack;
        }
        return null;
    }
}
