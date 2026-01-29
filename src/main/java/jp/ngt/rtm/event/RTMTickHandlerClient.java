package jp.ngt.rtm.event;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.ClientProxy;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.RenderMirror;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.gui.camera.Camera;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.Display;

/**
 * FMLのイベント
 */
@SideOnly(Side.CLIENT)
public final class RTMTickHandlerClient {
    public static long renderTickCount = 0;

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event)//Minecraft.runGameLoop()
    {
        if (event.phase == Phase.END) {
            if (NGTUtilClient.getMinecraft().inGameHasFocus && Display.isActive()) {
                EntityPlayer player = NGTUtilClient.getMinecraft().thePlayer;
                if (player.isRiding() && player.ridingEntity instanceof EntityArtillery) {
                    ((EntityArtillery) player.ridingEntity).updateYawAndPitch(player);
                }
            }

            RenderMirror.INSTANCE.onRenderTickEnd();

            renderTickCount++;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)//runGameLoop()内で複数回呼ばれる
    {
        World world = NGTUtilClient.getMinecraft().theWorld;
        if (!NGTUtilClient.getMinecraft().isGamePaused() && world != null) {
            if (event.phase == Phase.START) {
                byte viewMode = ClientProxy.getViewMode();
                if (viewMode == 4) {
                    Camera.INSTANCE.updateKeyState();
                }

                if (!RenderMirror.INSTANCE.finishRender) {
                    RenderMirror.INSTANCE.update();
                }
                RTMCore.proxy.getFormationManager().updateFormations(world);

                RTMKeyHandlerClient.INSTANCE.onTickStart();
            } else if (event.phase == Phase.END) {
                RTMKeyHandlerClient.INSTANCE.onTickEnd();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(RTMCore.MODID)) {
            RTMConfig.syncConfig();
        }
    }
}