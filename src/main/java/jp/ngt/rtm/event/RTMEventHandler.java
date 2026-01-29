package jp.ngt.rtm.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.item.ItemWrench;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.network.ConnectionManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

public final class RTMEventHandler {
    //PlayerEvent.StartTracking
    //CanUpdate

    @SubscribeEvent
    public void connectedToServer(ClientConnectedToServerEvent event) {
        ConnectionManager.INSTANCE.onConnectedToServer(event.isLocal);
    }

    @SubscribeEvent
    public void connectedFromClient(ServerConnectionFromClientEvent event)//このタイミングではS->Cのパケット届かない
    {
        ConnectionManager.INSTANCE.onConnectedFromClient(event.isLocal);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event)//ServerConfigurationManager.initializeConnectionToPlayer()
    {
        if (NGTUtil.isSMP() || NGTUtil.openedLANWorld()) {
            ModelPackManager.INSTANCE.sendModelSetsToClient((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        Entity ridingEntity = event.player.ridingEntity;
        if (ridingEntity instanceof EntityFloor) {
            event.player.mountEntity(null);
        } else if (ridingEntity instanceof EntityTrainBase) {
            event.player.mountEntity(null);
            ((EntityTrainBase) ridingEntity).setEBNotch();
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityBat) {
            if (RTMConfig.deleteBat) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(WorldTickEvent event) {
        if (event.phase == Phase.END) {
            RTMCore.proxy.getFormationManager().updateFormations(event.world);//Serverしか呼ばれない
        }
    }

    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        //StationManager.INSTANCE.loadData(event.world);
        FormationManager.getInstance().loadData(event.world);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (RTMCore.proxy.canCrash()) {
            CrashReport report = RTMCore.proxy.getCrashReport();
            RTMCore.proxy.postReportCrash();
            throw new ReportedException(report);
        }
    }

    @SubscribeEvent
    public void onPlayerAnimationEvent(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
            ItemStack heldItem = player.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemWrench) {
                ItemWrench wrench = (ItemWrench) heldItem.getItem();
                wrench.toggleModeLock(heldItem, player);
            }
        }
    }
}