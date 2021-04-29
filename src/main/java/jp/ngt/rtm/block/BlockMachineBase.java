package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.item.ItemInstalledObject;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMachineBase extends BlockContainer {
    protected BlockMachineBase(Material mat) {
        super(mat);
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
        this.clickMachine(world, x, y, z, player);
        return true;
    }

    protected boolean clickMachine(World world, int x, int y, int z, EntityPlayer player) {
        if (player.isSneaking())//NGTUtil.isEquippedItem(player, RTMItem.crowbar))
        {
            if (world.isRemote) {
                player.openGui(RTMCore.instance, RTMCore.guiIdSelectTileEntityModel, player.worldObj, x, y, z);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntityMachineBase tile = (TileEntityMachineBase) world.getTileEntity(x, y, z);
        MachineConfig cfg = tile.getModelSet().getConfig();
        return tile.isGettingPower ? cfg.brightness[1] : cfg.brightness[0];
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityMachineBase) {
            MachineType machineType = ((TileEntityMachineBase) tileEntity).getMachineType();
            ItemStack itemStack = new ItemStack(RTMItem.installedObject);
            itemStack.setItemDamage(ItemInstalledObject.IstlObjType.getType(machineType).id);
            ((ItemInstalledObject) RTMItem.installedObject).setModelName(itemStack, ((TileEntityMachineBase) tileEntity).getModelName());
            ((ItemInstalledObject) RTMItem.installedObject).setModelState(itemStack, ((TileEntityMachineBase) tileEntity).getResourceState());
            return itemStack;
        }
        return null;
    }
}