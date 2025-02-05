package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityCrossingGate;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCrossingGate extends BlockMachineBase {
    public BlockCrossingGate() {
        super(Material.rock);
        this.setStepSound(soundTypeGlass);
        this.setLightOpacity(0);
        this.setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 3.0F, 0.875F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityCrossingGate();
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, x, y, z, new ItemStack(RTMItem.installedObject, 1, 5));
        }
    }
}