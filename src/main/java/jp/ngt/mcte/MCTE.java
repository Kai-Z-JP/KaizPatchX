package jp.ngt.mcte;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import jp.ngt.mcte.block.*;
import jp.ngt.mcte.block.RSPort.PortType;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.mcte.gui.MCTEGuiHandler;
import jp.ngt.mcte.item.ItemEditor;
import jp.ngt.mcte.item.ItemGenerator;
import jp.ngt.mcte.item.ItemMiniature;
import jp.ngt.mcte.item.ItemPainter;
import jp.ngt.mcte.network.*;
import jp.ngt.mcte.world.WorldTypePictorial;
import jp.ngt.ngtlib.util.PermissionManager;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

@Mod(modid = MCTE.MODID, name = "MCTerrainEditor", version = MCTE.VERSION)
public class MCTE {
    public static final String MODID = "MCTE";
    public static final String VERSION = "1.7.10.16";

    @Instance("MCTE")
    public static MCTE instance;

    @Metadata(MODID)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "jp.ngt.mcte.ClientProxy", serverSide = "jp.ngt.mcte.CommonProxy")
    public static CommonProxy proxy;

    public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    public static short guiIdEditor = 1700;
    public static short guiIdGenerator = 1701;
    public static short guiIdPainter = 1702;
    public static short guiIdItemMiniature = 1703;

    public static final byte KEY_EditMode = 0;
    //public static final byte KEY_Delete = 1;
    //public static final byte KEY_Cut = 2;
    //public static final byte KEY_Copy = 3;
    //public static final byte KEY_Paste = 4;
    //public static final byte KEY_Fill = 5;
    public static final byte KEY_Clear = 6;
    public static final byte KEY_EditMenu = 7;
    public static final byte KEY_Undo = 8;

    public static final String USE_EDITOR = "useEditor";

    public static Block minesweeper;
    public static Block miniature;
    public static Block portIn;
    public static Block portOut;

    public static Item editor;
    public static Item generator;
    public static Item painter;
    public static Item itemMiniature;

    public static float rotationInterval;
    public static int numberOfUndo;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();

            Property prop1 = cfg.get("Block", "MiniatureRotationInterval", 15.0D);
            Property prop2 = cfg.get("Editor", "NumberOfUndo", 5);

            rotationInterval = (float) prop1.getDouble();
            numberOfUndo = prop2.getInt();
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Error Message");
        } finally {
            cfg.save();
        }

        minesweeper = (new BlockMinesweeper()).setBlockName("mcte:minesweeper").setBlockTextureName("mcte:minesweeper");
        miniature = (new BlockMiniature()).setBlockName("mcte:miniature");
        portIn = (new BlockPort(PortType.IN)).setBlockName("mcte:port_in").setBlockTextureName("mcte:port_in").setCreativeTab(CreativeTabs.tabTools);
        portOut = (new BlockPort(PortType.OUT)).setBlockName("mcte:port_out").setBlockTextureName("mcte:port_out").setCreativeTab(CreativeTabs.tabTools);

        editor = (new ItemEditor()).setUnlocalizedName("mcte:editor").setTextureName("mcte:editor").setCreativeTab(CreativeTabs.tabTools);
        generator = (new ItemGenerator()).setUnlocalizedName("mcte:generator").setTextureName("mcte:generator").setCreativeTab(CreativeTabs.tabTools);
        painter = (new ItemPainter()).setUnlocalizedName("mcte:painter").setTextureName("mcte:painter").setCreativeTab(CreativeTabs.tabTools);
        itemMiniature = (new ItemMiniature()).setUnlocalizedName("mcte:itemMiniature").setTextureName("mcte:itemMiniature").setCreativeTab(CreativeTabs.tabTools);

        GameRegistry.registerBlock(minesweeper, "minesweeper");
        GameRegistry.registerBlock(miniature, "miniature");
        GameRegistry.registerBlock(portIn, "port_in");
        GameRegistry.registerBlock(portOut, "port_out");

        GameRegistry.registerItem(editor, "editor");
        GameRegistry.registerItem(generator, "generator");
        GameRegistry.registerItem(painter, "painter");
        GameRegistry.registerItem(itemMiniature, "item_miniature");

        GameRegistry.registerTileEntity(TileEntityMinesweeper.class, "TEMinesweeper");
        GameRegistry.registerTileEntity(TileEntityMiniature.class, "TEMiniature");

        EntityRegistry.registerModEntity(EntityEditor.class, "mcte.e.editor", 0, this, 160, 3, false);

        proxy.preInit();

        NETWORK_WRAPPER.registerMessage(PacketMCTEKey.class, PacketMCTEKey.class, 0, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(PacketEditor.class, PacketEditor.class, 1, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(PacketResetSlot.class, PacketResetSlot.class, 2, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(PacketGenerator.class, PacketGenerator.class, 3, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(PacketRenderBlocks.class, PacketRenderBlocks.class, 4, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(PacketNBT.class, PacketNBT.class, 5, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(PacketExportData.class, PacketExportData.class, 6, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(PacketFilter.class, PacketFilter.class, 7, Side.SERVER);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new MCTEGuiHandler());

        WorldTypePictorial.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PermissionManager.INSTANCE.registerPermission(MCTE.USE_EDITOR);
    }

    @EventHandler
    public void handleServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMCTE());
    }
}