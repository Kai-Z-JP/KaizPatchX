package jp.ngt.rtm.block;

import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.item.ItemInstalledObject;
import jp.ngt.rtm.item.ItemWithModel;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

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
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMachineBase) {
            TileEntityMachineBase tileEntityMachineBase = (TileEntityMachineBase) tile;
            MachineConfig cfg = tileEntityMachineBase.getModelSet().getConfig();
            return tileEntityMachineBase.isGettingPower ? cfg.brightness[1] : cfg.brightness[0];
        } else {
            return this.getLightValue();
        }
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

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                ItemWithModel.copyOffsetToItemStack((TileEntityPlaceable) tileEntity, itemStack);
            }
            return itemStack;
        }
        return null;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        this.updateBlockState(world, x, y, z);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        this.updateBlockState(world, x, y, z);
    }

    protected void updateBlockState(World world, int x, int y, int z) {
        TileEntityMachineBase tile = (TileEntityMachineBase) world.getTileEntity(x, y, z);

        boolean b = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (tile.isGettingPower ^ b) {
            tile.setGettingPower(b);
        }
    }
}