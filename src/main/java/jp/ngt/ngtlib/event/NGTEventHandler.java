package jp.ngt.ngtlib.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.protection.ProtectionManager;
import jp.ngt.ngtlib.util.VersionChecker;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;

public final class NGTEventHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnected(ClientConnectedToServerEvent event) {
        VersionChecker.sendUpdateMessage(event);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ProtectionManager.INSTANCE.sendDataToClient();
    }

    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        ProtectionManager.INSTANCE.loadData(event.world);
    }

	/*@SubscribeEvent
	public void onSaveWorld(WorldEvent.Save event)
	{
		ProtectionManager.INSTANCE.saveData(event.world);
	}*/

    @SubscribeEvent
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (ProtectionManager.INSTANCE.rightClickBlock(event.entityPlayer, event.x, event.y, event.z)) {
                if (!event.world.isRemote)//Clientでキャンセルするとパケ送られない
                {
                    event.setCanceled(true);
                }
            }
        } else if (event.action == Action.LEFT_CLICK_BLOCK) {
            if (ProtectionManager.INSTANCE.leftClickBlock(event.entityPlayer, event.x, event.y, event.z)) {
                //クリエイティブでは呼ばれない?
                event.setCanceled(true);
                this.cancelBreakBlock(event.entityPlayer.worldObj, event.x, event.y, event.z);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerBreakBlock(BreakEvent event) {
        //鯖側のみ
        if (ProtectionManager.INSTANCE.leftClickBlock(event.getPlayer(), event.x, event.y, event.z)) {
            event.setCanceled(true);
            this.cancelBreakBlock(event.getPlayer().worldObj, event.x, event.y, event.z);
        }
    }

    private void cancelBreakBlock(World world, int x, int y, int z) {
        //Client側で破壊されたブロックを再描画
        world.markBlockForUpdate(x, y, z);
    }

    @SubscribeEvent
    public void onPlayerInteractEntity(EntityInteractEvent event) {
        if (ProtectionManager.INSTANCE.rightClickEntity(event.entityPlayer, event.target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        if (ProtectionManager.INSTANCE.leftClickEntity(event.entityPlayer, event.target)) {
            event.setCanceled(true);
        }
    }
}