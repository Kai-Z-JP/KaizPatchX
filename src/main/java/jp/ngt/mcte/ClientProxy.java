package jp.ngt.mcte;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.block.RenderMiniature;
import jp.ngt.mcte.block.TileEntityMiniature;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.mcte.editor.RenderEditor;
import jp.ngt.mcte.editor.filter.FilterManager;
import jp.ngt.mcte.item.RenderItemMiniature;
import jp.ngt.ngtlib.util.PackInfo;
import jp.ngt.ngtlib.util.VersionChecker;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        VersionChecker.addToCheckList(new PackInfo(MCTE.metadata.name, MCTE.metadata.url, MCTE.metadata.updateUrl, MCTE.metadata.version));

        RenderingRegistry.registerEntityRenderingHandler(EntityEditor.class, new RenderEditor());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMiniature.class, RenderMiniature.INSTANCE);

        MinecraftForgeClient.registerItemRenderer(MCTE.itemMiniature, RenderItemMiniature.INSTANCE);

        MCTEKeyHandlerClient.init();

        MinecraftForge.EVENT_BUS.register(RenderItemMiniature.INSTANCE);
    }

    @Override
    public void init() {
        FMLCommonHandler.instance().bus().register(new MCTEKeyHandlerClient());
        FilterManager.INSTANCE.loadFilters();//preInitではdevPathが設定できてないため
    }
}