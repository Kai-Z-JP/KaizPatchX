package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityFluorescent;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFluorescent extends BlockOrnamentBase {
    public BlockFluorescent() {
        super(Material.glass);
        this.setStepSound(soundTypeGlass);
        this.setLightLevel(1.0F);
        this.setResistance(5.0F);
        this.setLightOpacity(0);
        this.setHardness(1.0F);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int par2, int par3, int par4) {
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityFluorescent();
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, par2, par3, par4, this.getItem(par5));
        }
    }

    private ItemStack getItem(int damage) {
        return new ItemStack(RTMItem.installedObject, 1, damage & 2);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return 15;
    }
}