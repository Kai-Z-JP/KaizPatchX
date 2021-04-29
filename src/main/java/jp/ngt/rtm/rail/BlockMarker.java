package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.*;
import jp.ngt.rtm.item.ItemRail;
import jp.ngt.rtm.item.ItemWrench;
import jp.ngt.rtm.rail.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockMarker extends BlockContainer {
    /**
     * 0:normal, 1:switch, 2:slope
     */
    public final int markerType;
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockMarker(int type) {
        super(Material.glass);
        this.markerType = type;
        this.setLightOpacity(0);
        this.setLightLevel(1.0F);
        this.setStepSound(soundTypeGlass);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
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
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityMarker();
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int par5, float par6, int par7) {
    }

	/*@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z)
    {
		TileEntity tile = blockAccess.getTileEntity(x, y, z);
		if(tile instanceof TileEntityMarker)
		{
			int height = ((TileEntityMarker)tile).getHeight();
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, (float)height * 0.0625F, 1.0F);
		}
    }*/

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        int meta = itemStack.getItemDamage();
        int playerFacing;
        if ((this.markerType == 0 || this.markerType == 1) && meta >= 4) {
            playerFacing = (MathHelper.floor_double(NGTMath.normalizeAngle(entity.rotationYaw + 180.0D) / 90D) & 3);//斜め
        } else {
            playerFacing = (MathHelper.floor_double((NGTMath.normalizeAngle(entity.rotationYaw + 180.0D) / 90D) + 0.5D) & 3);
        }
        int i = meta / 4;
        world.setBlock(x, y, z, this, playerFacing + i * 4, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        ItemStack item = player.inventory.getCurrentItem();
        if (item != null) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (!(tile instanceof TileEntityMarker)) {
                return true;
            }
            TileEntityMarker marker = (TileEntityMarker) tile;

            if (item.getItem() == RTMItem.wrench) {
                if (!world.isRemote) {
                    this.makeRailMap(marker, x, y, z, player);
                }
                ((ItemWrench) RTMItem.wrench).onRightClickMarker(item, world, player, marker);
                return true;
            } else if (item.getItem() == RTMItem.paddle) {
                marker.displayDistance ^= true;
                return true;
            } else if (item.getItem() == Item.getItemFromBlock(RTMBlock.marker) || item.getItem() == Item.getItemFromBlock(RTMBlock.markerSwitch)) {
                if (world.isRemote) {
                    this.makeRailMap(marker, x, y, z, player);
                    player.openGui(RTMCore.instance, RTMCore.guiIdRailMarker, world, x, y, z);
                }
                return true;
            }
        }

        if (!world.isRemote) {
            if (this.onMarkerActivated(world, x, y, z, player, true)) {
                player.addStat(RTMAchievement.layRail, 1);
                if (!player.capabilities.isCreativeMode) {
                    --item.stackSize;
                }
            }
        }

        return true;
    }

    public void makeRailMap(TileEntityMarker marker, int x, int y, int z, EntityPlayer player) {
        if (marker.startY < 0) {
            this.onMarkerActivated(marker.getWorldObj(), x, y, z, player, false);
        } else {
            this.onMarkerActivated(marker.getWorldObj(), marker.startX, marker.startY, marker.startZ, player, false);
        }
    }

    public boolean onMarkerActivated(World world, int x, int y, int z, EntityPlayer player, boolean makeRail) {
        RailProperty prop = this.hasRail(player, makeRail);
        if (prop != null) {
            int type = this.markerType;
            int dis1 = RTMCore.railGeneratingDistance;
            int dis2 = dis1 * 2;
            int hei1 = RTMCore.railGeneratingHeight;
            int hei2 = hei1 * 2;
            boolean isCreative = player == null || player.capabilities.isCreativeMode;

            if (type == 0) {
                for (int i = 0; i < dis2; ++i) {
                    for (int j = 0; j < hei2; ++j) {
                        for (int k = 0; k < dis2; ++k) {
                            int x0 = x - dis1 + i;
                            int y0 = y - hei1 + j;
                            int z0 = z - dis1 + k;
                            if (!(i == dis1 && k == dis1) && world.getBlock(x0, y0, z0) == RTMBlock.marker) {
                                RailPosition rpS = this.getRailPosition(world, x, y, z);
                                RailPosition rpE = this.getRailPosition(world, x0, y0, z0);
                                if (rpS == null || rpE == null) {
                                    continue;//設置後すぐ右クリするとnullなので
                                }

                                return this.createRail0(world, rpS, rpE, prop, makeRail, isCreative);
                            }
                        }
                    }
                }
            } else if (type == 1) {
                List<RailPosition> list = new ArrayList<>();
                for (int i = 0; i < dis2; ++i) {
                    {
                        for (int k = 0; k < dis2; ++k) {
                            int x0 = x - dis1 + i;
                            int z0 = z - dis1 + k;
                            Block block = world.getBlock(x0, y, z0);
                            if (block == RTMBlock.marker || block == RTMBlock.markerSwitch) {
                                list.add(this.getRailPosition(world, x0, y, z0));
                            }
                        }
                    }
                }

                if (list.size() > 0) {
                    return this.createRail1(world, x, y, z, list, prop, makeRail, isCreative);
                }
            } else if (type == 2) {
                return this.createRail2(world, x, y, z, prop, makeRail, isCreative);
            }
        }
        return false;
    }

    /**
     * 通常のレール<br>
     * y0 <= y1でなければならない
     */
    private boolean createRail0(World world, RailPosition start, RailPosition end, RailProperty prop, boolean makeRail, boolean isCreative) {
        RailMap railMap = new RailMap(start, end);

        if (makeRail && railMap.canPlaceRail(world, isCreative, prop)) {
            //railMap.setRail(world, RTMRail.largeRailBase[shape[0]], x0, y0, z0);
            railMap.setRail(world, RTMRail.largeRailBase0, start.blockX, start.blockY, start.blockZ, prop);

            //world.setBlock(x0, y0, z0, RTMRail.largeRailCore[shape[0]], 0, 2);
            world.setBlock(start.blockX, start.blockY, start.blockZ, RTMRail.largeRailCore0, 0, 2);
            TileEntityLargeRailCore tile = (TileEntityLargeRailCore) world.getTileEntity(start.blockX, start.blockY, start.blockZ);
            tile.setRailPositions(new RailPosition[]{start, end});
            tile.setProperty(prop);
            tile.setStartPoint(start.blockX, start.blockY, start.blockZ);

            tile.createRailMap();
            tile.sendPacket();

            if (world.getBlock(end.blockX, end.blockY, end.blockZ) instanceof BlockMarker) {
                world.setBlockToAir(end.blockX, end.blockY, end.blockZ);
            }

            return true;
        } else {
            TileEntity tile = world.getTileEntity(start.blockX, start.blockY, start.blockZ);
            if (tile instanceof TileEntityMarker) {
                List<int[]> list = new ArrayList<>();
                list.add(new int[]{start.blockX, start.blockY, start.blockZ});
                list.add(new int[]{end.blockX, end.blockY, end.blockZ});
                ((TileEntityMarker) tile).setMarkersPos(list);
            }
            return false;
        }
    }

    /**
     * 分岐レール<br>
     *
     * @param list {x, y, z}
     */
    private boolean createRail1(World world, int x, int y, int z, List<RailPosition> list, RailProperty prop, boolean makeRail, boolean isCreative) {
        RailMaker railMaker = new RailMaker(world, list);
        SwitchType st = railMaker.getSwitch();
        if (st == null) {
            return false;
        }
        RailMapSwitch[] arrayOfRailMapSwitch = st.getAllRailMap();
        if (arrayOfRailMapSwitch == null) {
            return false;
        }
        boolean flag = false;
        for (RailMapSwitch railMapSwitch : arrayOfRailMapSwitch) {
            flag = !railMapSwitch.canPlaceRail(world, isCreative, prop);
        }
        if (!makeRail || flag) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityMarker) {
                List<int[]> posList = new ArrayList<>();
                for (RailPosition rp : list) {
                    posList.add(new int[]{rp.blockX, rp.blockY, rp.blockZ});
                }
                ((TileEntityMarker) tileEntity).setMarkersPos(posList);
            }
            return false;
        }
        for (RailMapSwitch railMapSwitch : arrayOfRailMapSwitch) {
            railMapSwitch.setRail(world, RTMRail.largeRailBase0, x, y, z, prop);
        }
        for (RailPosition rp : list) {
            world.setBlock(rp.blockX, rp.blockY, rp.blockZ, RTMRail.largeRailSwitchBase0, 0, 3);
            TileEntityLargeRailSwitchBase tileEntityLargeRailSwitchBase = (TileEntityLargeRailSwitchBase) world.getTileEntity(rp.blockX, rp.blockY, rp.blockZ);
            tileEntityLargeRailSwitchBase.setStartPoint(x, y, z);
        }
        world.setBlock(x, y, z, RTMRail.largeRailSwitchCore0, 0, 3);
        TileEntityLargeRailSwitchCore tile = (TileEntityLargeRailSwitchCore) world.getTileEntity(x, y, z);
        tile.setRailPositions(list.toArray(new RailPosition[0]));
        tile.setProperty(prop);
        tile.setStartPoint(x, y, z);
        tile.createRailMap();
        tile.sendPacket();
        return true;
    }

    /**
     * 坂レール
     */
    private boolean createRail2(World world, int x0, int y0, int z0, RailProperty prop, boolean makeRail, boolean isCreative) {
        int meta = world.getBlockMetadata(x0, y0, z0);
        byte dir0 = BlockMarker.getMarkerDir(RTMBlock.markerSlope, meta);
        byte dir1 = (byte) ((dir0 + 4) & 7);
        double d0 = meta < 4 ? 15.0D : (meta < 8 ? 7.0D : (meta < 12 ? 3.0D : 1.0D));
        float f0 = NGTMath.toRadians((float) dir0 * 45.0F);
        int x1 = x0 + MathHelper.floor_double(MathHelper.sin(f0) * d0);
        int z1 = z0 + MathHelper.floor_double(MathHelper.cos(f0) * d0);
        byte type = (byte) (meta < 4 ? 0 : (meta < 8 ? 1 : (meta < 12 ? 2 : 3)));

        RailPosition rp0 = new RailPosition(x0, y0, z0, dir0);
        RailPosition rp1 = new RailPosition(x1, y0, z1, dir1);
        RailMapSlope railMap = new RailMapSlope(rp0, rp1, type);

        if (makeRail && railMap.canPlaceRail(world, isCreative, prop)) {
            railMap.setRail(world, RTMRail.largeRailSlopeBase0, x0, y0, z0, prop);

            world.setBlock(x0, y0, z0, RTMRail.largeRailSlopeCore0, 0, 2);
            TileEntityLargeRailSlopeCore tile = (TileEntityLargeRailSlopeCore) world.getTileEntity(x0, y0, z0);
            tile.setRailPositions(new RailPosition[]{rp0, rp1});
            tile.setProperty(prop);
            tile.setSlopeType(type);
            tile.setStartPoint(x0, y0, z0);

            tile.createRailMap();
            tile.sendPacket();
            return true;
        } else {
            TileEntity tile = world.getTileEntity(x0, y0, z0);
            if (tile instanceof TileEntityMarker) {
                List<int[]> list = new ArrayList<>();
                list.add(new int[]{x0, y0, z0});
                list.add(new int[]{x1, y0, z1});
                ((TileEntityMarker) tile).setMarkersPos(list);
            }
            return false;
        }
    }

    public static byte getMarkerDir(Block block, int meta) {
        int i0 = meta & 3;
        int i1 = ((6 - i0) & 3) * 2;
        if ((block == RTMBlock.marker || block == RTMBlock.markerSwitch) && meta >= 4) {
            i1 = (i1 + 7) & 7;
        }
        return (byte) i1;
    }

    private RailPosition getRailPosition(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMarker) {
            TileEntityMarker marker = (TileEntityMarker) tile;
            return marker.getMarkerRP();
        }
        return null;
    }

    public RailProperty hasRail(EntityPlayer player, boolean par2) {
        if (player == null) {
            return ItemRail.getDefaultProperty();
        }

        if (PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_RAIL)) {
            ItemStack item = player.inventory.getCurrentItem();
            if (item != null && item.getItem() == RTMItem.itemLargeRail) {
                return ItemRail.getProperty(item);
            }

            if (player.capabilities.isCreativeMode || !par2) {
                return ItemRail.getDefaultProperty();
            }
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs tab, List list) {
        switch (this.markerType) {
            case 0:
            case 1:
                list.add(new ItemStack(par1, 1, 0));
                list.add(new ItemStack(par1, 1, 4));
                break;
            case 2:
                list.add(new ItemStack(par1, 1, 0));
                list.add(new ItemStack(par1, 1, 4));
                list.add(new ItemStack(par1, 1, 8));
                list.add(new ItemStack(par1, 1, 12));
                break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int par1, int par2) {
        int i = ((this.markerType == 0 || this.markerType == 1) ? 7 : 3);
        return this.icons[par2 & i];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.icons = new IIcon[8];
        this.icons[0] = register.registerIcon("rtm:marker_0");
        this.icons[1] = register.registerIcon("rtm:marker_1");
        this.icons[2] = register.registerIcon("rtm:marker_2");
        this.icons[3] = register.registerIcon("rtm:marker_3");
        this.icons[4] = register.registerIcon("rtm:marker2_0");
        this.icons[5] = register.registerIcon("rtm:marker2_1");
        this.icons[6] = register.registerIcon("rtm:marker2_2");
        this.icons[7] = register.registerIcon("rtm:marker2_3");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int par1) {
        switch (this.markerType) {
            //case 0: return par1 < 4 ? 0xFF0000 : 0xFFFFFF;
            case 0:
                return 0xFF0000;
            case 1:
                return 0x0000FF;
            case 2:
                switch (par1 / 4) {
                    case 0:
                        return 0xFFFF00;
                    case 1:
                        return 0xDDDD00;
                    case 2:
                        return 0xBBBB00;
                    case 3:
                        return 0x999900;
                }
            default:
                return 16777215;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getRenderColor(meta);
    }
}