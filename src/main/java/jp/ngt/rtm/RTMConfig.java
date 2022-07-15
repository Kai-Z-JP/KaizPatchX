package jp.ngt.rtm;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class RTMConfig {
    public static Configuration cfg;

    public static final String CATEGORY_SOUND = "Sound";
    public static final String CATEGORY_RAIL = "Rail";
    public static final String CATEGORY_ITEM = "Item";
    public static final String CATEGORY_ENTITY = "Entity";
    public static final String CATEGORY_MODEL = "Model";
    public static final String CATEGORY_MOD = "MOD";
    public static final String CATEGORY_BLOCK = "Block";
    public static final String CATEGORY_MARKER = "Marker";
    public static final String CATEGORY_LOAD = "Load";


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
    public static boolean use1122Marker;
    public static int loadSpeed;
    public static boolean expandPlayableSoundCount;

    public static void init(Configuration config) {
        cfg = config;
        try {
            config.load();
            syncConfig();
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Error Message");
        }
    }

    public static void syncConfig() {

        RTMConfig.trainSoundVol = cfg.getInt(
                "sound train", CATEGORY_SOUND, 100, 0, 100, "Train sound volume.") / 100.0F;
        RTMConfig.crossingGateSoundType = (byte) cfg.getInt(
                "sound crossing gate", CATEGORY_SOUND, 0, 0, 1, "Sound type of crossing gate.");
        RTMConfig.gunSoundVol = (float) cfg.getInt(
                "sound gun", CATEGORY_SOUND, 100, 0, 100, "Gun sound volume.") / 100.0F;
        RTMConfig.expandPlayableSoundCount = cfg.getBoolean(
                "Expand playable sound count", CATEGORY_SOUND, true,
                "expands the count of playable sound count at the same time. this may cause compatibility issue with Immersive Vehicles.");

        RTMConfig.railGeneratingDistance = (short) cfg.getInt(
                "GeneratingDistance", CATEGORY_RAIL, 64, 0, 256, "Distance for generating a rail. (default:64, recomended max value:256, It depends on server side)");
        RTMConfig.railGeneratingHeight = (short) cfg.getInt(
                "GeneratingHeight", CATEGORY_RAIL, 8, 0, 256, "Height for generating a rail. (recomended max value:256)");
        RTMConfig.markerDisplayDistance = (short) cfg.getInt(
                "MarkerDisplayDistance", CATEGORY_RAIL, 100, 0, Short.MAX_VALUE, "");

        RTMConfig.gunBreakBlock = cfg.getBoolean(
                "Gun Break Block", CATEGORY_ITEM, true, "");
        RTMConfig.deleteBat = cfg.getBoolean(
                "delete bat", CATEGORY_ENTITY, false, "Delete bat");

        RTMConfig.useServerModelPack = cfg.getBoolean(
                "use ServerModelPack", CATEGORY_MODEL, false, "Download ModelPacks from Server (or Permit download ModelPacks).");
        RTMConfig.smoothing = cfg.getBoolean("do smoothing", CATEGORY_MODEL, true, "");

        RTMConfig.versionCheck = cfg.getBoolean(
                "version check", CATEGORY_MOD, true, "");

        RTMConfig.mirrorTextureSize = cfg.getInt(
                "mirror texture size", CATEGORY_BLOCK, 512, 256, 2048, "FrameBuffer size for mirror. (Recomended size : 256~2048)");
        RTMConfig.mirrorRenderingFrequency = (byte) cfg.getInt(
                "mirror render frequency", CATEGORY_BLOCK, 1, 1, Byte.MAX_VALUE, "Frequency of rendering mirror. (1 : Full tick)");

        RTMConfig.use1122Marker = cfg.getBoolean("Use like 1.12", CATEGORY_MARKER, false, "");

        RTMConfig.loadSpeed = cfg.getInt(
                "ModelPack load speed", CATEGORY_LOAD, 2, 1, 3, "1:Slow 2:Default 3:Fast");

        cfg.save();
    }
}
