package jp.ngt.rtm.block;

import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.block.tileentity.TileEntitySignBoard;
import jp.ngt.rtm.item.ItemInstalledObject;
import jp.ngt.rtm.item.ItemWithModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class BlockSignBoard extends BlockContainer {
    public BlockSignBoard() {
        super(Material.circuits);
        this.setLightOpacity(0);
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
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntitySignBoard();
    }

    @Override
    public Item getItemDropped(int par1, Random random, int par3) {
        return null;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!world.isRemote) {
            this.dropBlockAsItem(world, par2, par3, par4, new ItemStack(RTMItem.installedObject, 1, 17));
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntitySignBoard) {
            ItemStack itemStack = new ItemStack(RTMItem.installedObject);
            itemStack.setItemDamage(ItemInstalledObject.IstlObjType.SIGNBOARD.id);

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                ItemWithModel.copyOffsetToItemStack((TileEntityPlaceable) tileEntity, itemStack);
            }
            return itemStack;
        }
        return null;
    }

	/*@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
		TileEntitySignBoard tile = (TileEntitySignBoard)world.getTileEntity(x, y, z);
		float h = tile.currentProperty.height;
		float w = tile.currentProperty.width;
		float d = tile.currentProperty.depth;
		return AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);
    }*/

	/*@Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
		TileEntitySignBoard tile = (TileEntitySignBoard)world.getTileEntity(x, y, z);
		byte dir = tile.getDirection();
		int meta = world.getBlockMetadata(x, y, z);
		float cx = 0.0F;
		float cy = 0.0F;
		float cz = 0.0F;
		float h = tile.currentProperty.height / 2.0F;
		float w = tile.currentProperty.width / 2.0F;
		float d = tile.currentProperty.depth / 2.0F;

		if(meta == 0)
		{
			cy = 0.5F - h;
		}
		else if(meta == 1)
		{
			cy = h - 0.5F;
		}
		else
		{
			if(dir == 1 && meta == 4)
			{
				cx = 0.5F - d;
			}
			else if(dir == 3 && meta == 5)
			{
				cx = d - 0.5F;
			}
			else if(dir == 0 && meta == 3)
			{
				cz = d - 0.5F;
			}
			else if(dir == 2 && meta == 2)
			{
				cz = 0.5F - d;
			}
			else if((dir == 0 && meta == 4) || (dir == 2 && meta == 5))
			{
				cx = 0.5F - w;
			}
			else if((dir == 1 && meta == 3) || (dir == 3 && meta == 2))
			{
				cz = 0.5F - w;
			}
			else if(dir == 0 || dir == 2)
			{
				cx = w - 0.5F;
			}
			else
			{
				cz = w - 0.5F;
			}
		}

		if(dir == 1 || dir == 3)
		{
			float f0 = w;
			w = d;
			d = f0;
		}

		this.setBlockBounds(0.5F + cx - w , 0.5F + cy - h, 0.5F + cz - d, 0.5F + cx + w, 0.5F + cy + h, 0.5F + cz + d);
    }*/

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (world.isRemote) {
            if (NGTUtil.isEquippedItem(player, RTMItem.crowbar)) {
                player.openGui(RTMCore.instance, RTMCore.guiIdChangeOffset, player.worldObj, x, y, z);
                return true;
            }

            player.openGui(RTMCore.instance, RTMCore.guiIdSelectTexture, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntitySignBoard tile = (TileEntitySignBoard) world.getTileEntity(x, y, z);
        if (tile != null) {
            int value = tile.getProperty().lightValue;
            if (value >= 0) {
                return value;
            } else if (value == -16) {
                return NGTMath.RANDOM.nextInt(6) * 3;
            } else if (tile.isGettingPower) {
                return -value;
            }
        }
        return 0;
    }

    @Deprecated
    private boolean isGettingPower(IBlockAccess world, int x, int y, int z) {
        for (int i = 0; i < BlockUtil.facing.length; ++i) {
            int[] ia = BlockUtil.facing[i];
            if (this.getIndirectPowerLevelTo(world, x + ia[0], y + ia[1], z + ia[2], i) > 0) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    private int getIndirectPowerLevelTo(IBlockAccess world, int x, int y, int z, int side) {
        if (world instanceof World) {
            return ((World) world).getIndirectPowerLevelTo(x, y, z, side);
        }
        Block block = world.getBlock(x, y, z);
        return block.shouldCheckWeakPower(world, x, y, z, side) ? 15 : block.isProvidingWeakPower(world, x, y, z, side);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (world.isRemote || PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_ORNAMENT)) {
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        return false;
    }
}