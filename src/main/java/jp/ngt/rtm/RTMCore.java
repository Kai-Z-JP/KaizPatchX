package jp.ngt.rtm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import jp.kaiz.kaizpatch.KaizPatchX;
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
import paulscode.sound.SoundSystemConfig;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = RTMCore.MODID, name = RTMCore.NAME, version = RTMCore.VERSION, guiFactory = "jp.ngt.rtm.gui.RTMConfigGuiFactory")
public final class RTMCore {
    public static final String MODID = "RTM";
    public static final String NAME = "RealTrainMod";
    public static final String VERSION = "1.7.10.41 KaizPatchX/" + KaizPatchX.VERSION;

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

    public static final int PacketSize = 512;
    public static final int ATOMIC_BOM_META = 2;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        FIXFileLoader.INSTANCE.load();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RTMConfig.init(new Configuration(event.getSuggestedConfigurationFile()));

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

        if (event.getSide() == Side.CLIENT && RTMConfig.expandPlayableSoundCount) {
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
