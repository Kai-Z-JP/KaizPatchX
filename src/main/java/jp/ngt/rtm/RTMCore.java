package jp.ngt.rtm;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import jp.ngt.ngtlib.util.PermissionManager;
import jp.ngt.rtm.command.RTMCommand;
import jp.ngt.rtm.electric.WireManager;
import jp.ngt.rtm.event.RTMEventHandler;
import jp.ngt.rtm.gui.RTMGuiHandler;
import jp.ngt.rtm.item.ItemBucketLiquid;
import jp.ngt.rtm.world.RTMChunkManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystemConfig;

@Mod(modid = RTMCore.MODID, name = "RealTrainMod", version = RTMCore.VERSION)
public final class RTMCore {
    public static final String MODID = "RTM";
    public static final String VERSION = "1.7.10.41_KaizPatchX1.3RC1";

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
    public static Configuration cfg;

    public static final int PacketSize = 512;
    public static final int ATOMIC_BOM_META = 2;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();
            Property soundPro1 = cfg.get("Sound", "sound train", 100);
            soundPro1.comment = "Train sound volume. (0 ~ 100)";
            Property soundPro2 = cfg.get("Sound", "sound crossing gate", 0);
            soundPro2.comment = "Sound type of crossing gate. (0, 1)";
            Property soundPro3 = cfg.get("Sound", "sound gun", 100);
            soundPro3.comment = "Gun sound volume. (0 ~ 100)";

            Property railPro1 = cfg.get("Rail", "GeneratingDistance", 64);
            railPro1.comment = "Distance for generating a rail. (default:64, recomended max value:256, It depends on server side)";
            Property railPro2 = cfg.get("Rail", "GeneratingHeight", 8);
            railPro2.comment = "Height for generating a rail. (default:8, recomended max value:256)";
            Property railPro3 = cfg.get("Rail", "MarkerDisplayDistance", 100);
            railPro3.comment = "(default length:100)";

            Property itemPro1 = cfg.get("Item", "Gun Break Block", true);
            //itemPro1.comment = "Delete bat";
            Property entityPro1 = cfg.get("Entity", "delete bat", false);
            entityPro1.comment = "Delete bat";
            Property modelPro1 = cfg.get("Model", "use ServerModelPack", false);
            modelPro1.comment = "Download ModelPacks from Server (or Permit download ModelPacks).";
            Property modelPro2 = cfg.get("Model", "do smoothing", true);
            //modelPro2.comment = "";
            Property modPro1 = cfg.get("Mod", "version check", true);
            modPro1.comment = "";
            Property blockPro1 = cfg.get("Block", "mirror texture size", 512);
            blockPro1.comment = "FrameBuffer size for mirror. (Recomended size : 256~2048)";
            Property blockPro2 = cfg.get("Block", "mirror render frequency", 1);
            blockPro2.comment = "Frequency of rendering mirror. (1 : Full tick)";


            marker = cfg.get("Marker", "Use like 1.12", false);
            Property fastLoadProperty = cfg.get("Load", "ModelPack load speed", 2);
            fastLoadProperty.comment = "1:Slow 2:Default 3:Fast";

            loadSpeed = fastLoadProperty.getInt();
            trainSoundVol = (float) soundPro1.getInt() / 100.0F;
            crossingGateSoundType = (byte) soundPro2.getInt();
            gunSoundVol = (float) soundPro3.getInt() / 100.0F;
            railGeneratingDistance = (short) railPro1.getInt();
            railGeneratingHeight = (short) railPro2.getInt();
            markerDisplayDistance = (short) railPro3.getInt();
            gunBreakBlock = itemPro1.getBoolean();
            deleteBat = entityPro1.getBoolean();
            useServerModelPack = modelPro1.getBoolean();
            smoothing = modelPro2.getBoolean();
            versionCheck = modPro1.getBoolean();
            mirrorTextureSize = blockPro1.getInt();
            mirrorRenderingFrequency = (byte) blockPro2.getInt();
            use1122Marker = marker.getBoolean();
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

        if (event.getSide() == Side.CLIENT) {
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
    }

    @EventHandler
    public void handleServerStarting(FMLServerStartingEvent event) {
        RTMCommand.init(event);
        WireManager.INSTANCE.clear();
    }

    private static short guiId;

    private static short getNextGuiID() {
        return guiId++;
    }
}
