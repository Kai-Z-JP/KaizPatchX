package jp.ngt.rtm;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXFileLoader;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.command.RTMCommand;
import jp.ngt.rtm.electric.WireManager;
import jp.ngt.rtm.event.RTMEventHandler;
import jp.ngt.rtm.gui.RTMGuiHandler;
import jp.ngt.rtm.item.ItemBucketLiquid;
import jp.ngt.rtm.world.RTMChunkManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystemConfig;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = RTMCore.MODID, name = "RealTrainMod", version = RTMCore.VERSION)
public final class RTMCore {
    public static final String MODID = "RTM";
    public static final String VERSION = "1.7.10.41_KaizPatchX1.5.0-rc.1";

    @Instance(MODID)
    public static RTMCore instance;

    @Metadata(MODID)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "jp.ngt.rtm.ClientProxy", serverSide = "jp.ngt.rtm.CommonProxy")
    public static CommonProxy proxy;

    public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    public static short guiIdSelectEntityModel = getNextGuiID();
    public static short guiIdSelectTileEntityModel = getNextGuiID();
    public static short guiIdSelectItemModel = getNextGuiID();
    public static short guiIdFreightCar = getNextGuiID();
    public static short guiIdItemContainer = getNextGuiID();
    public static short guiIdSelectTexture = getNextGuiID();
    public static short guiIdTrainControlPanel = getNextGuiID();
    public static short guiIdTrainWorkBench = getNextGuiID();
    public static short guiIdSignalConverter = getNextGuiID();
    public static short guiIdTicketVendor = getNextGuiID();
    public static short guiIdStation = getNextGuiID();
    public static short guiIdPaintTool = getNextGuiID();
    public static short guiIdMovingMachine = getNextGuiID();
    public static short guiIdTurnplate = getNextGuiID();
    public static short guiIdNPC = getNextGuiID();
    public static short guiIdMotorman = getNextGuiID();
    public static short guiIdRailMarker = getNextGuiID();
    public static short guiIdSpeaker = getNextGuiID();
    public static short guiIdCamera = getNextGuiID();
    public static short guiIdChangeOffset = getNextGuiID();

    public static final byte KEY_Forward = 0;
    public static final byte KEY_Back = 1;
    public static final byte KEY_Horn = 2;
    public static final byte KEY_Chime = 3;
    public static final byte KEY_ControlPanel = 4;
    public static final byte KEY_Fire = 5;
    public static final byte KEY_ATS = 6;
    public static final byte KEY_LEFT = 7;
    public static final byte KEY_RIGHT = 8;
    public static final byte KEY_JUMP = 9;
    public static final byte KEY_SNEAK = 10;
    public static final byte KEY_EB = 11;

    public static final String EDIT_VEHICLE = "editVehicle";
    //public static final String USE_RAZER = "useRazer";
    //public static final String USE_GUN = "useGun";
    //public static final String USE_CANNON = "useCannon";
    public static final String EDIT_RAIL = "editRail";
    public static final String DRIVE_TRAIN = "driveTrain";
    public static final String CHANGE_MODEL = "changeModel";
    public static final String EDIT_ORNAMENT = "editOrnament";

    public static float trainSoundVol;
    public static float gunSoundVol;
    public static short railGeneratingDistance;
    public static short railGeneratingHeight;
    public static short markerDisplayDistance;
    public static byte crossingGateSoundType;
    public static boolean gunBreakBlock;
    public static boolean deleteBat;
    public static boolean useServerModelPack;//C/S両側で有効
    public static boolean versionCheck;
    public static int mirrorTextureSize;
    public static boolean smoothing;
    public static byte mirrorRenderingFrequency;
    public static Property marker;
    public static boolean use1122Marker;
    public static int loadSpeed;
    public static boolean expandPlayableSoundCount;
    public static Configuration cfg;

    public static final int PacketSize = 512;
    public static final int ATOMIC_BOM_META = 2;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        FIXFileLoader.INSTANCE.load();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();
            trainSoundVol = cfg.get("Sound", "sound train", 100, "Train sound volume. (0 ~ 100)").getInt() / 100.0F;
            crossingGateSoundType = (byte) cfg.get("Sound", "sound crossing gate", 0, "Sound type of crossing gate. (0, 1)").getInt();
            gunSoundVol = (float) cfg.get("Sound", "sound gun", 100, "Gun sound volume. (0 ~ 100)").getInt() / 100.0F;

            railGeneratingDistance = (short) cfg.get("Rail", "GeneratingDistance", 64,
                    "Distance for generating a rail. (default:64, recomended max value:256, It depends on server side)").getInt();
            railGeneratingHeight = (short) cfg.get("Rail", "GeneratingHeight", 8,
                    "Height for generating a rail. (default:8, recomended max value:256)").getInt();
            markerDisplayDistance = (short) cfg.get("Rail", "MarkerDisplayDistance", 100, "(default length:100)").getInt();

            gunBreakBlock = cfg.get("Item", "Gun Break Block", true).getBoolean();
            //itemPro1.comment = "Delete bat";
            deleteBat = cfg.get("Entity", "delete bat", false, "Delete bat").getBoolean();
            useServerModelPack = cfg.get("Model", "use ServerModelPack", false,
                    "Download ModelPacks from Server (or Permit download ModelPacks).").getBoolean();
            smoothing = cfg.get("Model", "do smoothing", true).getBoolean();
            versionCheck = cfg.get("Mod", "version check", true).getBoolean();
            mirrorTextureSize = cfg.get("Block", "mirror texture size", 512,
                    "FrameBuffer size for mirror. (Recomended size : 256~2048)").getInt();
            mirrorRenderingFrequency = (byte) cfg.get("Block", "mirror render frequency", 1,
                    "Frequency of rendering mirror. (1 : Full tick)").getInt();

            marker = cfg.get("Marker", "Use like 1.12", false);
            use1122Marker = marker.getBoolean();
            loadSpeed = cfg.get("Load", "ModelPack load speed", 2, "1:Slow 2:Default 3:Fast").getInt();
            expandPlayableSoundCount = cfg.get("Sound", "Expand playable sound count", true,
                    "expands the count of playable sound count at the same time. this may cause compatibility issue with Immersive Vehicles.").getBoolean();
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Error Message");
        } finally {
            cfg.save();
        }

        RTMBlock.init();
        RTMItem.init();
        RTMEntity.init(this);
        RTMRecipe.init();
        RTMAchievement.init();
        RTMPacket.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new RTMGuiHandler());

        proxy.preInit();

        ForgeChunkManager.setForcedChunkLoadingCallback(this, RTMChunkManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RTMChunkManager.INSTANCE);

        if (event.getSide() == Side.CLIENT && expandPlayableSoundCount) {
            SoundSystemConfig.setNumberNormalChannels(1024);
            SoundSystemConfig.setNumberStreamingChannels(32);
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //FMLCommonHandler.instance().bus().register(new RTMTickHandler());
        RTMEventHandler handler = new RTMEventHandler();
        FMLCommonHandler.instance().bus().register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
        MinecraftForge.EVENT_BUS.register(new ItemBucketLiquid());

        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PermissionManager.INSTANCE.registerPermission("fixrtm.all_permit");
        PermissionManager.INSTANCE.registerPermission(RTMCore.EDIT_VEHICLE);
        PermissionManager.INSTANCE.registerPermission(RTMCore.EDIT_RAIL);
        PermissionManager.INSTANCE.registerPermission(RTMCore.DRIVE_TRAIN);
        PermissionManager.INSTANCE.registerPermission(RTMCore.CHANGE_MODEL);
        PermissionManager.INSTANCE.registerPermission(RTMCore.EDIT_ORNAMENT);
    }

    @EventHandler
    public void handleServerStarting(FMLServerStartingEvent event) {
        RTMCommand.init(event);
        WireManager.INSTANCE.clear();
    }

    public static void registerRtmPrefixed(Item item, String name) {
        GameRegistry.registerItem(item, removePrefix(name));
        registerMapping(item, name);
    }

    public static void registerRtmPrefixed(Block block, String name) {
        GameRegistry.registerBlock(block, removePrefix(name));
        registerMapping(block, name);
    }

    public static void registerRtmPrefixed(Block block, Class<? extends ItemBlock> itemclass, String name) {
        GameRegistry.registerBlock(block, itemclass, removePrefix(name));
        registerMapping(block, name);
    }

    private static String removePrefix(String name) {
        assert name.startsWith("rtm:");
        return name.substring("rtm:".length());
    }

    public static void registerMapping(Item item, String name) {
        itemMap.put(MODID + ':' + name, item);
    }

    public static void registerMapping(Block block, String name) {
        blockMap.put(MODID + ':' + name, block);
        Item itemForBlock = Item.getItemFromBlock(block);
        if (itemForBlock != null) {
            registerMapping(itemForBlock, name);
        }
    }

    private static Map<String, Item> itemMap = new HashMap<>();
    private static Map<String, Block> blockMap = new HashMap<>();

    @EventHandler
    public void handleMissingMapping(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            switch (mapping.type) {
                case BLOCK:
                    Block mappedBlock = blockMap.get(mapping.name);
                    if (mappedBlock != null)
                        mapping.remap(mappedBlock);
                    break;
                case ITEM:
                    Item mappedItem = itemMap.get(mapping.name);
                    if (mappedItem != null)
                        mapping.remap(mappedItem);
                    break;
            }
        }
    }

    private static short guiId;

    private static short getNextGuiID() {
        return guiId++;
    }
}
