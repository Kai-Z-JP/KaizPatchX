package jp.ngt.ngtlib;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import jp.ngt.ngtlib.command.CommandNGT;
import jp.ngt.ngtlib.command.CommandPermit;
import jp.ngt.ngtlib.command.CommandProtection;
import jp.ngt.ngtlib.event.NGTEventHandler;
import jp.ngt.ngtlib.item.ItemProtectionKey;
import jp.ngt.ngtlib.network.*;
import jp.ngt.ngtlib.util.PermissionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

import java.io.IOException;

@Mod(modid = NGTCore.MODID, name = "NGTLib", version = NGTCore.VERSION)
public class NGTCore {
	public static final String MODID = "NGTLib";
	public static final String VERSION = "1.7.10.32";

	@Instance(MODID)
	public static NGTCore instance;

	@Metadata(MODID)
	public static ModMetadata metadata;

	@SidedProxy(clientSide = "jp.ngt.ngtlib.ClientProxy", serverSide = "jp.ngt.ngtlib.CommonProxy")
	public static CommonProxy proxy;
	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	public static String shaderModName;
	public static boolean versionCheck;
	public static boolean debugLog;

	public static ItemProtectionKey protection_key;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		try {
			cfg.load();
			Property modPro1 = cfg.get("Mod", "version check", true);
			modPro1.comment = "";
			Property modPro2 = cfg.get("Mod", "shadersmod name", "ShadersModCore");
			modPro2.comment = "File name of ShadersMod";
			Property modPro3 = cfg.get("Mod", "debug log", false);
			modPro3.comment = "";
			debugLog = modPro3.getBoolean();

			versionCheck = modPro1.getBoolean();
			shaderModName = modPro2.getString();
		} catch (Exception e) {
			FMLLog.log(Level.ERROR, e, "Error Message");
		} finally {
			cfg.save();
		}

		protection_key = (ItemProtectionKey) (new ItemProtectionKey()).setUnlocalizedName("protection_key").setTextureName("ngtlib:protection_key");

		GameRegistry.registerItem(protection_key, "protection_key");

		proxy.preInit();
		NETWORK_WRAPPER.registerMessage(PacketNoticeHandlerClient.class, PacketNotice.class, 0, Side.CLIENT);
		NETWORK_WRAPPER.registerMessage(PacketNoticeHandlerServer.class, PacketNotice.class, 1, Side.SERVER);
		NETWORK_WRAPPER.registerMessage(PacketNBTHandlerClient.class, PacketNBT.class, 2, Side.CLIENT);
		NETWORK_WRAPPER.registerMessage(PacketNBTHandlerServer.class, PacketNBT.class, 3, Side.SERVER);
		NETWORK_WRAPPER.registerMessage(PacketProtection.class, PacketProtection.class, 4, Side.CLIENT);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		NGTEventHandler handler = new NGTEventHandler();
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);

		try {
			PermissionManager.INSTANCE.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandNGT());
		event.registerServerCommand(new CommandProtection());
		event.registerServerCommand(new CommandPermit());
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		//NGTStructureBuilder.init();
	}
}