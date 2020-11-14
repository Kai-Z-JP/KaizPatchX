package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.*;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import jp.ngt.rtm.electric.*;
import jp.ngt.rtm.entity.EntityATC;
import jp.ngt.rtm.entity.EntityBumpingPost;
import jp.ngt.rtm.entity.EntityInstalledObject;
import jp.ngt.rtm.entity.EntityTrainDetector;
import jp.ngt.rtm.modelpack.cfg.ConnectorConfig;
import jp.ngt.rtm.modelpack.cfg.MachineConfig;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class ItemInstalledObject extends ItemWithModel {
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemInstalledObject() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            int meta = itemStack.getItemDamage();
            int x = par4;
            int y = par5;
            int z = par6;
            Block block = null;
            IstlObjType type = IstlObjType.getType(meta);

            if (par7 == 0)//up
            {
                --par5;
            } else if (par7 == 1)//down
            {
                ++par5;
            } else if (par7 == 2)//south
            {
                --par6;
            } else if (par7 == 3)//north
            {
                ++par6;
            } else if (par7 == 4)//east
            {
                --par4;
            } else if (par7 == 5)//west
            {
                ++par4;
            }

            if (!world.isAirBlock(par4, par5, par6)) {
                return true;
            }

            if (meta <= 2 || type == IstlObjType.FLUORESCENT_COVERED) {
                int i1 = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                if (player.canPlayerEdit(par4, par5, par6, par7, itemStack) && world.isAirBlock(par4, par5, par6)) {
                    world.setBlock(par4, par5, par6, RTMBlock.fluorescent, meta, 2);
                    byte dir = 0;
                    switch (par7) {
                        case 0:
                            if (i1 == 0 || i1 == 2) {
                                dir = 0;
                            } else {
                                dir = 4;
                            }
                            break;
                        case 1:
                            if (i1 == 0 || i1 == 2) {
                                dir = 2;
                            } else {
                                dir = 6;
                            }
                            break;
                        case 2:
                            dir = 1;
                            break;
                        case 3:
                            dir = 3;
                            break;
                        case 4:
                            dir = 5;
                            break;
                        case 5:
                            dir = 7;
                            break;
                    }
                    TileEntityFluorescent tile = (TileEntityFluorescent) world.getTileEntity(par4, par5, par6);
                    tile.setDir(dir);
                    block = RTMBlock.fluorescent;
                }
            } else if (type == IstlObjType.CROSSING) {
                if (par7 == 1) {
                    world.setBlock(par4, par5, par6, RTMBlock.crossingGate, 0, 3);
                    TileEntityCrossingGate tile = (TileEntityCrossingGate) world.getTileEntity(par4, par5, par6);
                    tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
                    tile.setModelName(this.getModelName(itemStack));
                    tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                    block = RTMBlock.crossingGate;
                }
            } else if (type == IstlObjType.TURNSTILE) {
                //当たり判定のため
                int dir = (MathHelper.floor_double((NGTMath.normalizeAngle(player.rotationYaw + 180.0D) / 90.0D) + 0.5D) & 3);
                world.setBlock(par4, par5, par6, RTMBlock.turnstile, dir, 3);
                TileEntityTurnstile tile = (TileEntityTurnstile) world.getTileEntity(par4, par5, par6);
                tile.setRotation(player, 90.0F, true);
                tile.setModelName(this.getModelName(itemStack));
                tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                block = RTMBlock.turnstile;
            } else if (type == IstlObjType.BUMPING_POST) {
                if (par7 == 1 && setEntityOnRail(world, new EntityBumpingPost(world), par4, par5 - 1, par6, player, itemStack)) {
                    block = Blocks.stone;
                }
            } else if (type == IstlObjType.POINT) {
                if (par7 == 1) {
                    world.setBlock(par4, par5, par6, RTMBlock.point, 0, 3);
                    TileEntityPoint tile = (TileEntityPoint) world.getTileEntity(par4, par5, par6);
                    tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, false);
                    tile.setModelName(this.getModelName(itemStack));
                    tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                    block = RTMBlock.point;
                }
            } else if (type == IstlObjType.SIGNBOARD) {
                world.setBlock(par4, par5, par6, RTMBlock.signboard, par7, 3);
                TileEntitySignBoard tile = (TileEntitySignBoard) world.getTileEntity(par4, par5, par6);
                int playerFacing = (MathHelper.floor_double((NGTMath.normalizeAngle(player.rotationYaw + 180.0D) / 90D) + 0.5D) & 3);
                tile.setDirection((byte) playerFacing);
                tile.setTexture("textures/signboard/ngt_a01.png");
                block = RTMBlock.signboard;
            } else if (type == IstlObjType.TICKET_VENDOR) {
                if (par7 == 1) {
                    world.setBlock(par4, par5, par6, RTMBlock.ticketVendor, 0, 3);
                    TileEntityTicketVendor tile = (TileEntityTicketVendor) world.getTileEntity(par4, par5, par6);
                    tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
                    tile.setModelName(this.getModelName(itemStack));
                    tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                    block = RTMBlock.ticketVendor;
                }
            } else if (type == IstlObjType.LIGHT) {
                world.setBlock(par4, par5, par6, RTMBlock.light, par7, 3);
                TileEntityLight tile = (TileEntityLight) world.getTileEntity(par4, par5, par6);
                tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
                tile.setModelName(this.getModelName(itemStack));
                tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                block = RTMBlock.light;
            } else if (type == IstlObjType.FLAG) {
                world.setBlock(par4, par5, par6, RTMBlock.flag, 0, 3);
                TileEntityFlag tile = (TileEntityFlag) world.getTileEntity(par4, par5, par6);
                tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
                tile.setTexture("textures/flag/flag_RTM3Anniversary.png");
                block = RTMBlock.flag;
            } else if (type == IstlObjType.ATC) {
                if (par7 == 1 && this.setEntityOnRail(world, new EntityATC(world), par4, par5 - 1, par6, player, itemStack)) {
                    block = Blocks.stone;
                }
            } else if (type == IstlObjType.TRAIN_DETECTOR) {
                if (par7 == 1 && this.setEntityOnRail(world, new EntityTrainDetector(world), par4, par5 - 1, par6, player, itemStack)) {
                    block = Blocks.stone;
                }
            } else if (type == IstlObjType.INSULATOR) {
                world.setBlock(par4, par5, par6, RTMBlock.insulator, par7, 2);
                TileEntityInsulator tile = (TileEntityInsulator) world.getTileEntity(par4, par5, par6);
                tile.setModelName(this.getModelName(itemStack));
                tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                block = RTMBlock.insulator;
            } else if (type == IstlObjType.CONNECTOR_IN || type == IstlObjType.CONNECTOR_OUT) {
                Block block2 = world.getBlock(x, y, z);
                if (block2 instanceof IBlockConnective && ((IBlockConnective) block2).canConnect(world, x, y, z)) {
                    if (type == IstlObjType.CONNECTOR_OUT) {
                        par7 += 6;
                    }
                    world.setBlock(par4, par5, par6, RTMBlock.connector, par7, 2);
                    TileEntityConnector tile = (TileEntityConnector) world.getTileEntity(par4, par5, par6);
                    tile.setModelName(this.getModelName(itemStack));
                    tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                    tile.setConnectionTo(x, y, z, ConnectionType.DIRECT, "");
                    block = RTMBlock.connector;
                }
            } else if (type == IstlObjType.SPEAKER) {
                world.setBlock(par4, par5, par6, RTMBlock.speaker, par7, 3);
                TileEntitySpeaker tile = (TileEntitySpeaker) world.getTileEntity(par4, par5, par6);
                tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
                tile.setModelName(this.getModelName(itemStack));
                tile.getResourceState().readFromNBT(this.getModelState(itemStack).writeToNBT());
                block = RTMBlock.speaker;
            }

            if (block != null) {
                world.playSoundEffect((double) par4 + 0.5D, (double) par5 + 0.5D, (double) par6 + 0.5D,
                        block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --itemStack.stackSize;
            }
        }
        return true;
    }

    public boolean setEntityOnRail(World world, EntityInstalledObject entity, int x, int y, int z, EntityPlayer player, ItemStack stack) {
        RailMap rm0 = TileEntityLargeRailBase.getRailMapFromCoordinates(world, null, x, y, z);
        if (rm0 == null) {
            return false;
        }

        int split = 128;
        int i0 = rm0.getNearlestPoint(split, (double) x + 0.5D, (double) z + 0.5D);
        double posX = rm0.getRailPos(split, i0)[1];
        double posY = rm0.getRailHeight(split, i0) + 0.0625D;
        double posZ = rm0.getRailPos(split, i0)[0];
        float yaw = rm0.getRailRotation(split, i0);
        float yaw2 = -player.rotationYaw + 180.0F;
        float dif = MathHelper.wrapAngleTo180_float(yaw - yaw2);
        if (Math.abs(dif) > 90.0F) {
            yaw += 180.0F;
        }

        entity.setPosition(posX, posY, posZ);
        entity.rotationYaw = yaw;
        entity.rotationPitch = 0.0F;
        world.spawnEntityInWorld(entity);
        entity.setModelName(this.getModelName(stack));
        entity.getResourceState().readFromNBT(this.getModelState(stack).writeToNBT());
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName() + "." + itemStack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List list) {
        Arrays.stream(IstlObjType.values())
                .filter(type -> type != IstlObjType.NONE)
                .map(type -> new ItemStack(par1, 1, type.id))
                .forEach(list::add);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        int j = MathHelper.clamp_int(par1, 0, 24);
        return this.icons[j];
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        IIcon missing = register.registerIcon("ngtlib.missing");
        this.icons = new IIcon[25];
        this.icons[IstlObjType.FLUORESCENT_GLASS.id] = register.registerIcon("rtm:fluorescent");
        this.icons[IstlObjType.FLUORESCENT_DIAMOND.id] = register.registerIcon("rtm:fluorescent");
        this.icons[IstlObjType.FLUORESCENT_BROKEN.id] = register.registerIcon("rtm:fluorescent");
        this.icons[IstlObjType.INSULATOR.id] = register.registerIcon("rtm:insulator");
        this.icons[IstlObjType.FLUORESCENT_COVERED.id] = register.registerIcon("rtm:fluorescent");
        this.icons[IstlObjType.CROSSING.id] = register.registerIcon("rtm:crossing");
        this.icons[6] = missing;
        this.icons[7] = missing;
        this.icons[IstlObjType.CONNECTOR_IN.id] = register.registerIcon("rtm:itemConnector_in");
        this.icons[IstlObjType.CONNECTOR_OUT.id] = register.registerIcon("rtm:itemConnector_out");
        this.icons[IstlObjType.ATC.id] = register.registerIcon("rtm:itemATC");
        this.icons[IstlObjType.TRAIN_DETECTOR.id] = register.registerIcon("rtm:itemTrainDetector");
        this.icons[IstlObjType.TURNSTILE.id] = register.registerIcon("rtm:itemTurnstile");
        this.icons[IstlObjType.BUMPING_POST.id] = register.registerIcon("rtm:itemBumpingPost");
        this.icons[14] = missing;
        this.icons[15] = missing;
        this.icons[IstlObjType.POINT.id] = register.registerIcon("rtm:point");
        this.icons[IstlObjType.SIGNBOARD.id] = register.registerIcon("rtm:itemSignBoard");
        this.icons[IstlObjType.TICKET_VENDOR.id] = register.registerIcon("rtm:item_ticket_vendor");
        this.icons[IstlObjType.LIGHT.id] = register.registerIcon("rtm:lightBlock");
        this.icons[IstlObjType.FLAG.id] = register.registerIcon("rtm:flag");
        this.icons[21] = missing;
        this.icons[22] = missing;
        this.icons[IstlObjType.SPEAKER.id] = register.registerIcon("rtm:speaker");
        this.icons[24] = missing;
    }

    @Override
    protected String getModelType(ItemStack itemStack) {
        return IstlObjType.getType(itemStack.getItemDamage()).modelType;
    }

    @Override
    protected String getDefaultModelName(ItemStack itemStack) {
        return IstlObjType.getType(itemStack.getItemDamage()).defaultModel;
    }

    @Override
    public String getSubType(ItemStack itemStack) {
        return IstlObjType.getType(itemStack.getItemDamage()).subType;
    }

    public enum IstlObjType {
        FLUORESCENT_GLASS(0, "", "", ""),
        FLUORESCENT_DIAMOND(1, "", "", ""),
        FLUORESCENT_BROKEN(2, "", "", ""),
        /**
         * 碍子
         */
        INSULATOR(3, ConnectorConfig.TYPE, "Relay", "Insulator01"),
        FLUORESCENT_COVERED(4, "", "", ""),
        /**
         * 遮断器
         */
        CROSSING(5, MachineConfig.TYPE, MachineType.Gate.toString(), "CrossingGate01L"),
        CONNECTOR_IN(8, ConnectorConfig.TYPE, "Input", "Input01"),
        CONNECTOR_OUT(9, ConnectorConfig.TYPE, "Output", "Output01"),
        ATC(10, MachineConfig.TYPE, MachineType.Antenna_Send.toString(), "ATC_01"),
        TRAIN_DETECTOR(11, MachineConfig.TYPE, MachineType.Antenna_Receive.toString(), "TrainDetector_01"),
        /**
         * 改札機
         */
        TURNSTILE(12, MachineConfig.TYPE, MachineType.Turnstile.toString(), "Turnstile01"),
        /**
         * 車止め
         */
        BUMPING_POST(13, MachineConfig.TYPE, MachineType.BumpingPost.toString(), "BumpingPost_Type2"),
        POINT(16, MachineConfig.TYPE, MachineType.Point.toString(), "Point01M"),
        SIGNBOARD(17, "", "", ""),
        TICKET_VENDOR(18, MachineConfig.TYPE, MachineType.Vendor.toString(), "Vendor01"),
        LIGHT(19, MachineConfig.TYPE, MachineType.Light.toString(), "SearchLight01"),
        FLAG(20, "", "", ""),
        //		STAIR(21, "", "", ""),
//		SCAFFOLD(22, "", "", ""),
        SPEAKER(23, MachineConfig.TYPE, MachineType.Speaker.toString(), "Speaker01"),
        //		MECHANISM(24, "", "", ""),
        NONE(-1, "", "", "");

        public final byte id;
        public final String modelType;
        public final String subType;
        public final String defaultModel;

        IstlObjType(int par1, String par2, String par3, String par4) {
            this.id = (byte) par1;
            this.modelType = par2;
            this.subType = par3;
            this.defaultModel = par4;
        }

        public static IstlObjType getType(int id) {
            return Arrays.stream(IstlObjType.values()).filter(type -> type.id == id).findFirst().orElse(NONE);
        }

        public static IstlObjType getType(MachineType machineType) {
            return Arrays.stream(IstlObjType.values()).filter(type -> type.subType.equals(machineType.toString())).findFirst().orElse(NONE);
        }
    }
}