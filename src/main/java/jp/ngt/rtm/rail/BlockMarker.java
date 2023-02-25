package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.NGTVec;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockMarker extends BlockContainer {
    /**
     * 0:normal, 1:switch, 10:straight
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

    public static int getFacing(EntityLivingBase placer, boolean isDiagonal) {
        int playerFacing = MathHelper.floor_double(NGTMath.normalizeAngle(placer.rotationYaw + 180.0D) / 45.0D + 0.5D) & 7;
        playerFacing = playerFacing / 2 + (playerFacing % 2 == 0 ? 0 : 4);
        return playerFacing;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        int meta = itemStack.getItemDamage();
        int playerFacing = MathHelper.floor_double(NGTMath.normalizeAngle(entity.rotationYaw + 180.0D) / 45.0D + 0.5D) & 7;
        playerFacing = playerFacing / 2 + (playerFacing % 2 == 0 ? 0 : 4);
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

            if (item.getItem() == RTMItem.wrench && (this.markerType == 0 || this.markerType == 1)) {
                if (!world.isRemote) {
                    this.makeRailMap(marker, x, y, z, player);
                }
                ((ItemWrench) RTMItem.wrench).onRightClickMarker(item, world, player, marker);
                return true;
            } else if (item.getItem() == RTMItem.paddle) {
                marker.displayDistance ^= true;
                return true;
            } else if (item.getItem() == Item.getItemFromBlock(this) && (this.markerType == 0 || this.markerType == 1)) {
                if (!world.isRemote) {
                    this.makeRailMap(marker, x, y, z, player);
                }
                player.openGui(RTMCore.instance, RTMCore.guiIdRailMarker, world, x, y, z);
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
            int dis1 = RTMConfig.railGeneratingDistance;
            int dis2 = dis1 * 2;
            int dis3 = dis1 * dis1;
            int hei1 = RTMConfig.railGeneratingHeight;
            int hei2 = hei1 * 2;
            boolean isCreative = player == null || player.capabilities.isCreativeMode;

            if (type == 0 || type == 10) {
                RailPosition rpS = this.getRailPosition(world, x, y, z);

                List<TileEntity> tileEntityList = ((List<TileEntity>) world.loadedTileEntityList);
                if (tileEntityList != null && !tileEntityList.isEmpty()) {
                    RailPosition rpE = tileEntityList.stream()
                            .filter(TileEntityMarker.class::isInstance)
                            .map(TileEntityMarker.class::cast)
                            .filter(tile -> tile.getMarkerRP() != rpS)
                            .filter(tile -> tile.getBlockType().equals(this))
                            .filter(tile -> tile.getDistanceFrom(x, tile.yCoord, z) < dis3)
                            .filter(tile -> Math.abs(tile.yCoord - y) < hei1)
                            .sorted(Comparator.comparingInt(o -> Math.abs(o.yCoord - y)))
                            .min(Comparator.comparingDouble(o -> o.getDistanceFrom(x, y, z)))
                            .map(TileEntityMarker::getMarkerRP)
                            .orElse(null);
                    if (rpS != null && rpE != null) {
                        if (type == 10) {
                            NGTVec eS = new NGTVec(rpE.posX - rpS.posX, rpE.posY - rpS.posY, rpE.posZ - rpS.posZ);
                            NGTVec sE = new NGTVec(rpS.posX - rpE.posX, rpS.posY - rpE.posY, rpS.posZ - rpE.posZ);
                            rpS.anchorYaw = eS.getYaw();
                            rpS.anchorPitch = eS.getPitch();
                            rpE.anchorYaw = sE.getYaw();
                            rpE.anchorPitch = sE.getPitch();
                        }
                        return createRail0(world, rpS, rpE, prop, makeRail, isCreative);
                    }
                }
            } else if (type == 1) {
                List<RailPosition> list = new ArrayList<>();
                List<TileEntity> tileEntityList = ((List<TileEntity>) world.loadedTileEntityList);
                if (tileEntityList != null && !tileEntityList.isEmpty()) {
                    list = tileEntityList.stream()
                            .filter(TileEntityMarker.class::isInstance)
                            .map(TileEntityMarker.class::cast)
                            .filter(tile -> tile.getBlockType().equals(RTMBlock.marker) || tile.getBlockType().equals(RTMBlock.markerSwitch))
                            .filter(tile -> tile.getDistanceFrom(x, tile.yCoord, z) < dis3)
                            .filter(tile -> Math.abs(tile.yCoord - y) < hei1)
                            .sorted(Comparator.comparingInt(o -> Math.abs(o.yCoord - y)))
                            .map(TileEntityMarker::getMarkerRP)
                            .collect(Collectors.toList());
                }

                if (list.size() == 2 && list.stream().allMatch(rp -> rp.switchType == 1)) {
                    return createTurntable(world, list.get(0), list.get(1), prop, makeRail, isCreative);
                }
                if (list.size() >= 3) {
                    return createRail1(world, x, y, z, player, list, prop, makeRail, isCreative);
                }
            }
        }
        return false;
    }


    public static boolean createRail(World world, int x, int y, int z, List<RailPosition> rps, RailProperty prop, boolean makeRail, boolean isCreative) {

        rps = rps.stream().sorted(Comparator.comparingInt(o -> o.blockY)).collect(Collectors.toList());
        if (rps.size() == 2) {
            if (rps.stream().allMatch(rp -> rp.switchType == 1)) {
                return createTurntable(world, rps.get(0), rps.get(1), prop, makeRail, isCreative);
            } else {
                createRail0(world, rps.get(0), rps.get(1), prop, makeRail, isCreative);
            }
        } else if (rps.size() > 2) {
            createRail1(world, x, y, z, null, rps, prop, makeRail, isCreative);
        }
        return false;
    }


    /**
     * 通常のレール<br>
     * y0 <= y1でなければならない
     */
    private static boolean createRail0(World world, RailPosition start, RailPosition end, RailProperty prop, boolean makeRail, boolean isCreative) {
        RailMapBasic railMap = new RailMapBasic(start, end, RailMapBasic.fixRTMRailMapVersionCurrent);

        if (makeRail && railMap.canPlaceRail(world, isCreative, prop)) {
            //railMap.setRail(world, RTMRail.largeRailBase[shape[0]], x0, y0, z0);
            railMap.setRail(world, RTMRail.largeRailBase0, start.blockX, start.blockY, start.blockZ, prop);

            //world.setBlock(x0, y0, z0, RTMRail.largeRailCore[shape[0]], 0, 2);
            world.setBlock(start.blockX, start.blockY, start.blockZ, RTMRail.largeRailCore0, 0, 2);
            TileEntityLargeRailCore tile = (TileEntityLargeRailCore) world.getTileEntity(start.blockX, start.blockY, start.blockZ);
            tile.setRailPositions(new RailPosition[]{start, end});
            tile.setProperty(prop);
            tile.setStartPoint(start.blockX, start.blockY, start.blockZ);
            tile.fixRTMRailMapVersion = railMap.fixRTMRailMapVersion;

            tile.createRailMap();
            tile.markDirty();
            world.markBlockForUpdate(start.blockX, start.blockY, start.blockZ);

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
    private static boolean createRail1(World world, int x, int y, int z, EntityPlayer player, List<RailPosition> list, RailProperty prop, boolean makeRail, boolean isCreative) {
        RailMaker railMaker = new RailMaker(world, list, RailMapBasic.fixRTMRailMapVersionCurrent);
        SwitchType st = railMaker.getSwitch();
        if (st == null) {
            if (player != null) {
                NGTLog.sendChatMessage(player, "message.rail.switch_type");
            }
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
        tile.fixRTMRailMapVersion = railMaker.fixRTMRailMapVersion;
        tile.createRailMap();
        tile.markDirty();
        world.markBlockForUpdate(x, y, z);
        return true;
    }

    private static boolean createTurntable(World world, RailPosition start, RailPosition end, RailProperty prop, boolean makeRail, boolean isCreative) {
        int cx = 0;
        int cy = start.blockY;
        int cz = 0;
        int r = 0;

        if (start.blockX == end.blockX && (start.blockZ - end.blockZ) % 2 == 0) {
            cx = start.blockX;
            cz = (start.blockZ + end.blockZ) / 2;
            r = Math.abs(start.blockZ - end.blockZ) / 2;
        }

        if (start.blockZ == end.blockZ && (start.blockX - end.blockX) % 2 == 0) {
            cx = (start.blockX + end.blockX) / 2;
            cz = start.blockZ;
            r = Math.abs(start.blockX - end.blockX) / 2;
        }

        if (r == 0) {
            return false;
        }

        RailMapTurntable railMap = new RailMapTurntable(start, end, cx, cy, cz, r, RailMapBasic.fixRTMRailMapVersionCurrent);
        if (makeRail && railMap.canPlaceRail(world, isCreative, prop)) {
            railMap.setRail(world, RTMRail.largeRailBase0, cx, cy, cz, prop);

            world.setBlock(cx, cy, cz, RTMRail.TURNTABLE_CORE, 0, 3);
            TileEntityTurnTableCore tile = (TileEntityTurnTableCore) world.getTileEntity(cx, cy, cz);
            tile.setRailPositions(new RailPosition[]{start, end});
            tile.setProperty(prop);
            tile.setStartPoint(cx, cy, cz);
            tile.fixRTMRailMapVersion = railMap.fixRTMRailMapVersion;

            tile.createRailMap();
            tile.markDirty();
            world.markBlockForUpdate(cx, cy, cz);

            return true;
        }

        return false;
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
        if (this.markerType == 0 || this.markerType == 1 || this.markerType == 10) {
            list.add(new ItemStack(par1, 1, 0));
        }
    }

    @Override
    public String getItemIconName() {
        return "rtm:marker";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int par1, int par2) {
        int i = ((this.markerType == 0 || this.markerType == 1 || this.markerType == 10) ? 7 : 3);
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
            case 10:
                return 0xEC008C;
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