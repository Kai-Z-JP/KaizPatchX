package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.block.tileentity.TileEntityMirror;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMirror extends BlockContainer {
    public final MirrorType mirrorType;
    @SideOnly(Side.CLIENT)
    protected IIcon icon_front;

    public BlockMirror(MirrorType par1) {
        super(Material.glass);
        this.mirrorType = par1;
        this.setStepSound(Block.soundTypeGlass);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityMirror();
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
        if (this.mirrorType == MirrorType.Mono_Panel) {
            int meta = blockAccess.getBlockMetadata(x, y, z);
            switch (meta) {
                case 0:
                    this.setBlockBounds(0.0F, 0.9375F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;
                case 1:
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
                    break;
                case 2:
                    this.setBlockBounds(0.0F, 0.0F, 0.9375F, 1.0F, 1.0F, 1.0F);
                    break;
                case 3:
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0625F);
                    break;
                case 4:
                    this.setBlockBounds(0.9375F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;
                case 5:
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0625F, 1.0F, 1.0F);
                    break;
            }
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (this.mirrorType == MirrorType.Mono_Panel) {
            return side == meta ? this.icon_front : this.blockIcon;
        } else {
            return this.icon_front;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1) {
        this.blockIcon = par1.registerIcon("rtm:mirror");
        this.icon_front = par1.registerIcon("rtm:tp");
    }

    public enum MirrorType {
        Mono_Panel,
        Hexa_Cube
    }
}