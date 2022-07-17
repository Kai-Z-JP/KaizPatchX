package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.RailConfig;
import jp.ngt.rtm.modelpack.cfg.RailConfig.BallastSet;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.rail.BlockMarker;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.util.RailPosition;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemRail extends ItemWithModel {
    public ItemRail() {
        super();
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockMarker) {
            return false;
        }
        if (!world.isRemote && PermissionManager.INSTANCE.hasPermission(player, RTMCore.EDIT_RAIL)) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityLargeRailBase) {
                TileEntityLargeRailCore core = ((TileEntityLargeRailBase) tile).getRailCore();
                if (core != null) {
                    RailProperty property = ItemRail.getProperty(itemStack);
                    if (property != null) {
                        if (player.isSneaking()) {
                            core.replaceRail(property);
                        } else {
                            core.addSubRail(property);
                        }
                    }

                }
            } else {
                this.placeRail(world, x, y + 1, z, itemStack, player);
                return true;
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        List<ModelSetBase> rails = ModelPackManager.INSTANCE.getModelList("ModelRail");
        for (ModelSetBase modelSet : rails) {
            RailConfig cfg = (RailConfig) modelSet.getConfig();
            if (cfg.defaultBallast == null) {
                continue;
            }

            for (BallastSet set : cfg.defaultBallast) {
                Block block = Block.getBlockFromName(set.blockName);
                int meta = set.blockMetadata;
                float h = set.height <= 0.0F ? 0.0625F : set.height;
                if (block == null) {
                    block = Blocks.air;
                }
                RailProperty prop = new RailProperty(cfg.getName(), block, meta, h);
                list.add(getRailItem(prop));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        String s = super.getItemStackDisplayName(itemStack);
        RailProperty prop = getProperty(itemStack);
        if (prop == null) {
            return s;
        }

        String localizedName = "";
        if (StatCollector.canTranslate(prop.unlocalizedName)) {
            localizedName = ", " + StatCollector.translateToLocal(prop.unlocalizedName);
        }
        return s + "(" + prop.getModelSet().getConfig().getName() + localizedName + ")";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        RailProperty prop = getProperty(itemStack);
        if (prop == null) {
            return;
        }
        //list.add(EnumChatFormatting.GRAY + "Model:" + prop.railModel);
        //String s = StatCollector.translateToLocal(prop.unlocalizedName);
        //list.add(EnumChatFormatting.GRAY + "Block:" + s);
        list.add(EnumChatFormatting.GRAY + "Height:" + prop.blockHeight);
    }

    public static RailProperty getDefaultProperty() {
        return new RailProperty("1067mm_Wood", Blocks.gravel, 0, 0.0625F);
    }

    public static ItemStack getRailItem(RailProperty prop) {
        ItemStack itemStack = new ItemStack(RTMItem.itemLargeRail, 1, 0);
        writePropToItem(prop, itemStack);
        return itemStack;
    }

    public static void writePropToItem(RailProperty prop, ItemStack itemStack) {
        NBTTagCompound nbtP = new NBTTagCompound();
        prop.writeToNBT(nbtP);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Property", nbtP);
        itemStack.setTagCompound(nbt);
    }

    public static RailProperty getProperty(ItemStack stack) {
        return stack.hasTagCompound() ? RailProperty.readFromNBT(stack.getTagCompound().getCompoundTag("Property")) : null;
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        RailProperty prop = getProperty(itemStack);
        return (prop == null) ? this.getDefaultModelName(itemStack) : prop.railModel;
    }

    @Override
    public void setModelName(ItemStack itemStack, String name) {
        RailProperty prop = getProperty(itemStack);
        if (prop == null) {
            prop = getDefaultProperty();
        }
        prop = new RailProperty(name, prop.block, prop.blockMetadata, prop.blockHeight);
        writePropToItem(prop, itemStack);
    }

    @Override
    protected String getModelType(ItemStack itemStack) {
        return RailConfig.TYPE;
    }

    @Override
    protected String getDefaultModelName(ItemStack itemStack) {
        return "1067mm_Wood";
    }

    private static List<RailPosition> getRPFromItem(ItemStack stack) {
        List<RailPosition> list = new ArrayList<>();
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            byte size = nbt.getByte("Size");
            for (int i = 0; i < size; ++i) {
                list.add(RailPosition.readFromNBT(nbt.getCompoundTag("RP" + i)));
            }
        }
        return list;
    }

    private static void setRPToItem(ItemStack stack, RailPosition[] rps) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = stack.getTagCompound();

        nbt.setByte("Size", (byte) rps.length);
        for (int i = 0; i < rps.length; ++i) {
            nbt.setTag("RP" + i, rps[i].writeToNBT());
        }
    }

    public static ItemStack copyItemFromRail(TileEntityLargeRailCore core) {
        ItemStack stack = ItemRail.getRailItem(core.getProperty());
        RailPosition[] rps = core.getRailPositions();
        setRPToItem(stack, rps);
        String shape = core.getRailShapeName();
        stack.getTagCompound().setString("ShapeName", shape);
        return stack;
    }

    private boolean placeRail(World world, int x, int y, int z, ItemStack stack, EntityPlayer player) {
        List<RailPosition> rps = getRPFromItem(stack);
        if (!rps.isEmpty()) {
            int dir = -BlockMarker.getFacing(player, false) * 2 + 4;//90刻みへ変換
            RailPosition topRP = rps.get(0);//分岐RP前提、BlockMarkerで並べ替え
            int difDir = dir - topRP.direction;
            int origX = topRP.blockX;
            int origY = topRP.blockY;
            int origZ = topRP.blockZ;
            for (RailPosition rp : rps) {
                double dif2X = (rp.blockX + 0.5D) - (origX + 0.5D);
                double dif2Y = (rp.blockY + 0.5D) - (origY + 0.5D);
                double dif2Z = (rp.blockZ + 0.5D) - (origZ + 0.5D);
                Vec3 vec = PooledVec3.create(dif2X, dif2Y, dif2Z);
                vec = vec.rotateAroundY(difDir * 45.0F);
                rp.blockX = MathHelper.floor_double(x + 0.5D + vec.getX());//整数座標で計算するとずれる
                rp.blockY = MathHelper.floor_double(y + 0.5D + vec.getY());
                rp.blockZ = MathHelper.floor_double(z + 0.5D + vec.getZ());
                rp.direction = (byte) ((rp.direction + difDir + 8) & 7);
                rp.anchorYaw = MathHelper.wrapAngleTo180_float(rp.anchorYaw + difDir * 45.0F);
                rp.init();
            }
            RailProperty state = ItemRail.getProperty(stack);
            boolean isCreative = player.capabilities.isCreativeMode;
            return BlockMarker.createRail(world, x, y, z, rps, state, true, isCreative);
        }
        return false;
    }
}