package jp.ngt.rtm.block;

import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityLight;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public class BlockLight extends BlockMachineBase {
    public BlockLight() {
        super(Material.glass);
        this.setStepSound(soundTypeGlass);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityLight();
    }

    @Override
    public Item getItemDropped(int par1, Random rand, int par3) {
        return null;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, par2, par3, par4, new ItemStack(RTMItem.installedObject, 1, 19));
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (world.isRemote || PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_ORNAMENT)) {
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        return false;
    }
}