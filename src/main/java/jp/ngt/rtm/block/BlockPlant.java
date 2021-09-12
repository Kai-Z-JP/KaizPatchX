package jp.ngt.rtm.block;

import jp.ngt.rtm.block.tileentity.TileEntityPlantOrnament;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPlant extends BlockContainer {
    public BlockPlant() {
        super(Material.plants);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityPlantOrnament();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
        return true;
    }

    @Override
    public String getUnlocalizedName() {
        return "plant_ornament";
    }
}
