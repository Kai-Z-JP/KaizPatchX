package jp.ngt.ngtlib;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.event.NGTEventHandlerClient;
import jp.ngt.ngtlib.gui.GuiWarning;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.ngtlib.util.PackInfo;
import jp.ngt.ngtlib.util.VersionChecker;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private static final GuiWarning GUI_WARNING = new GuiWarning(NGTUtilClient.getMinecraft());

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public World getWorld() {
        return NGTUtilClient.getMinecraft().theWorld;
    }

    @Override
    public EntityPlayer getPlayer() {
        return NGTUtilClient.getMinecraft().thePlayer;
    }

    @Override
    public File getMinecraftDirectory(String folder) {
        return new File(NGTUtilClient.getMinecraft().mcDataDir, folder);
    }

    @Override
    public String getUserName() {
        return NGTUtilClient.getMinecraft().getSession().getPlayerID();
    }

    @Override
    public int getNewRenderType() {
        return RenderingRegistry.getNextAvailableRenderId();
    }

    @Override
    public void preInit() {
        if (NGTCore.versionCheck) {
            VersionChecker.addToCheckList(new PackInfo(NGTCore.metadata.name, NGTCore.metadata.url, NGTCore.metadata.updateUrl, NGTCore.metadata.version));
        }

        //認証処理無効化
		/*NGTCertificate.checkPlayerData(this.getUserName());

		if(!NGTCertificate.canUse())
		{
			MinecraftForge.EVENT_BUS.register(GUI_WARNING);
		}*/

        MinecraftForge.EVENT_BUS.register(NGTEventHandlerClient.INSTANCE);
        //FMLCommonHandler.instance().bus().register(NGTEventHandlerClient.INSTANCE);
    }

    @Override
    public void postInit() {
        VersionChecker.checkVersion();
    }

    @Override
    public void removeGuiWarning() {
        MinecraftForge.EVENT_BUS.unregister(GUI_WARNING);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int meta) {
        if (NGTUtil.isServer()) {
            super.breakBlock(world, x, y, z, meta);
            return;
        }

        if (NGTUtilClient.getMinecraft().playerController != null && NGTUtilClient.getMinecraft().thePlayer != null) {
            NGTUtilClient.getMinecraft().playerController.onPlayerDestroyBlock(x, y, z, meta);
        }
    }

    @Override
    public void zoom(EntityPlayer player, int count) {
        float fovModifierHand = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, NGTUtilClient.getMinecraft().entityRenderer, "fovModifierHand", "field_78507_R");
        fovModifierHand = 0.1F;
        ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, NGTUtilClient.getMinecraft().entityRenderer, fovModifierHand, "fovModifierHand", "field_78507_R");
    }

    @Override
    public int getChunkLoadDistance() {
        return NGTUtilClient.getMinecraft().gameSettings.renderDistanceChunks << 4;
    }
}