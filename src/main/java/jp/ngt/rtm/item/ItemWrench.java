package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketMarkerRPClient;
import jp.ngt.rtm.rail.BlockMarker;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemWrench extends Item {
    /**
     * {S/C+PlayerID, Marker}
     */
    private final Map<String, TileEntityMarker> markerMap = new HashMap<>();

    public ItemWrench() {
        super();
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!this.changeMarkerAnchor(world, player)) {
            this.changeMode(world, itemStack, player);
        }
        return itemStack;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10) {
        if (side != 1 || itemStack.getItemDamage() > 5 && itemStack.getItemDamage() < 11) {
            this.changeMarkerAnchor(world, player);
            return true;
        }
        if (itemStack.getItemDamage() == 11) {
            this.revertRailToMarker(world, x, y, z);
        }

        ++y;

        switch (itemStack.getItemDamage()) {
            case 0:
                this.placeMarker(player, world, x, y, z, RTMBlock.marker, 0);
                break;
            case 1:
                this.placeMarker(player, world, x, y, z, RTMBlock.markerSwitch, 0);
                break;
            case 2:
                this.placeMarker(player, world, x, y, z, RTMBlock.markerStraight, 0);
                break;
        }
        return true;
    }

    private int directionMetaMap[] = {2, 5, 1, 4, 0, 7, 3, 6};

    public void revertRailToMarker(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityLargeRailBase) {
            TileEntityLargeRailBase targetRail = (TileEntityLargeRailBase) tile;
            TileEntityLargeRailCore targetRailCore = targetRail.getRailCore();
            if (targetRailCore != null) {
                world.setBlockToAir(x, y, z);
                RailPosition[] rps = targetRailCore.getRailPositions();
                Arrays.stream(rps).forEach(rp -> {
                    if (rp != null) {
                        int meta = directionMetaMap[rp.direction];
                        world.setBlock(rp.blockX, rp.blockY, rp.blockZ, rp.switchType == 0 ? RTMBlock.marker : RTMBlock.markerSwitch, meta, 2);
                        TileEntityMarker marker = (TileEntityMarker) world.getTileEntity(rp.blockX, rp.blockY, rp.blockZ);
                        marker.setMarkerRP(rp);
                    }
                });
            }
        }
    }

    //BlockMarkerから, C/S両方で呼ばれる
    public void onRightClickMarker(ItemStack itemStack, World world, EntityPlayer player, TileEntityMarker marker) {
        switch (itemStack.getItemDamage()) {
            case 6:
                marker.displayDistance ^= true;
                break;
            case 7:
                marker.changeDisplayMode();
                break;
            case 8:
                this.changeMarkerHeight(world, player, marker);
                break;
            case 9:
                this.changeMarkerAnchor(world, player, marker);
                break;
            case 10:
                marker.fitNeighbor ^= true;
                if (world.isRemote) {
                    NGTLog.sendChatMessage(player, "Set fit neighbor rail: " + marker.fitNeighbor);
                }
                break;
        }
    }

    private void changeMode(World world, ItemStack itemStack, EntityPlayer player) {
        int i = (itemStack.getItemDamage() + 1) % 12;
        if (i >= 3 && i <= 5) {
            i = 6;
        }
        itemStack.setItemDamage(i);
        if (!world.isRemote) {
            String mode = StatCollector.translateToLocal("description.wrench.mode_" + i);
            EnumChatFormatting color;
            switch (i) {
                case 0:
                    color = EnumChatFormatting.RED;
                    break;
                case 1:
                    color = EnumChatFormatting.AQUA;
                    break;
                case 2:
                    color = EnumChatFormatting.LIGHT_PURPLE;
                    break;
                default:
                    color = EnumChatFormatting.WHITE;
                    break;
            }
            NGTLog.sendChatMessage(player, "message.wrench.mode", color + mode);
        } else {
            RTMCore.proxy.playSound(player, new ResourceLocation("gui.button.press"), 1.0F, 1.0F);
        }
    }

    private void placeMarker(EntityPlayer player, World world, int x, int y, int z, BlockMarker block, int damage) {
        if (world.isRemote) {
            return;
        }

        if (block.markerType == 0 || block.markerType == 1) {
            int dir = (MathHelper.floor_double((NGTMath.normalizeAngle(player.rotationYaw + 180.0D) / 45.0D) + 0.5D) & 7);
            damage = dir / 2 + (dir % 2 == 0 ? 0 : 4);
        } else {
            int dir = (MathHelper.floor_double((NGTMath.normalizeAngle(player.rotationYaw + 180.0D) / 90.0D) + 0.5D) & 3);
            damage += dir;
        }
        world.setBlock(x, y, z, block, damage, 3);
        world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
    }

    private void changeMarkerHeight(World world, EntityPlayer player, TileEntityMarker marker) {
        marker.setDisplayMode((byte) 2);
        byte b = marker.increaseHeight();

        if (!world.isRemote) {
            if (marker.startY < 0) {
                ((BlockMarker) marker.getBlockType()).onMarkerActivated(world, marker.xCoord, marker.yCoord, marker.zCoord, player, false);
            } else {
                ((BlockMarker) marker.getBlockType()).onMarkerActivated(world, marker.startX, marker.startY, marker.startZ, player, false);
            }
            NGTLog.sendChatMessage(player, "Set height : " + b);
        }
    }

    private boolean changeMarkerAnchor(World world, EntityPlayer player) {
        TileEntityMarker marker = this.markerMap.get(this.getKey(player));
        if (marker != null) {
            this.changeMarkerAnchor(world, player, marker);
            return true;
        }
        return false;
    }

    private void changeMarkerAnchor(World world, EntityPlayer player, TileEntityMarker marker) {
        if (marker.followMouseMoving) {
            marker.followingPlayer = null;
            marker.followMouseMoving = false;
            marker.editMode = 0;
            this.markerMap.remove(this.getKey(player));
            TileEntityMarker core = marker.getCoreMarker();
            if (world.isRemote && core != null) {
                RTMCore.NETWORK_WRAPPER.sendToServer(new PacketMarkerRPClient(core));
            }
        } else {
            marker.setDisplayMode((byte) 2);
            marker.followingPlayer = player;
            marker.followMouseMoving = true;
            this.markerMap.put(this.getKey(player), marker);
        }
    }

    private String getKey(EntityPlayer player) {
        return (player.worldObj.isRemote ? "C" : "S") + player.getEntityId();
    }

    private MovingObjectPosition getTarget(EntityPlayer player) {
        MovingObjectPosition target = BlockUtil.getMOPFromPlayer(player, 128.0D, true);
        if (target != null && target.typeOfHit == MovingObjectType.BLOCK) {
            if (target.sideHit == 1) {
                ++target.blockY;
                return target;//Block.onBlockActivated() - f, f, f
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        for (int i = 0; i < 12; ++i) {
            if (i >= 3 && i <= 5) {
                continue;
            }
            String sb = I18n.format("usage.wrench") + i + " : " + I18n.format("description.wrench.mode_" + i);
            list.add(EnumChatFormatting.GRAY + sb);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        int i = itemStack.getItemDamage();
        return super.getItemStackDisplayName(itemStack) + " (" + I18n.format("description.wrench.mode_" + i) + ")";
    }
}