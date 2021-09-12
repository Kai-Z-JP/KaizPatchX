package jp.ngt.rtm.block;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntityFluorescent;
import jp.ngt.rtm.item.ItemInstalledObject;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFluorescent extends BlockContainer {
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int par2, int par3, int par4) {
        return null;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
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

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityFluorescent) {
            ItemStack itemStack = new ItemStack(RTMItem.installedObject);
            itemStack.setItemDamage(ItemInstalledObject.IstlObjType.FLUORESCENT.id);
            ((ItemInstalledObject) RTMItem.installedObject).setModelName(itemStack, ((TileEntityFluorescent) tileEntity).getModelName());
            ((ItemInstalledObject) RTMItem.installedObject).setModelState(itemStack, ((TileEntityFluorescent) tileEntity).getResourceState());
            return itemStack;
        }
        return null;
    }
}