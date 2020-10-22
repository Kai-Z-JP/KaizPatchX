package jp.ngt.rtm.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.ClientProxy;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.RenderBullet;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.gui.GuiIngameCustom;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.sound.RTMSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public final class RTMEventHandlerClient {
	private final RTMSoundHandler soundHandler;
	private final GuiIngameCustom guiIngame;

	public RTMEventHandlerClient(Minecraft par1) {
		this.soundHandler = new RTMSoundHandler();
		this.guiIngame = new GuiIngameCustom(par1);
	}

	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Pre event)//GuiIngame, GuiIngameForge
	{
		this.guiIngame.onRenderGui(event);
	}

	@SubscribeEvent
	public void onUpdateFOV(FOVUpdateEvent event)//EntityRenderer.updateFovModifierHand()
	{
		event.newfov = RTMCore.proxy.getFov(event.entity, event.fov);
	}

	@SubscribeEvent
	public void onRenderWorldBlocks(RenderWorldEvent.Post event)//WorldRenderer.updateRenderer()
	{
		if (event.pass == 0)//1はたまにしか呼ばれない
		{
			List list = NGTUtilClient.getMinecraft().renderGlobal.tileEntities;
			for (int i = 0; i < list.size(); ++i) {
				TileEntity tile = (TileEntity) list.get(i);
				if (tile instanceof TileEntityLargeRailCore) {
					TileEntityLargeRailCore rail = ((TileEntityLargeRailCore) tile);
					if (!rail.isLoaded()) {
						continue;
					}
					int[] size = rail.getRailSize();
					boolean flag1 = size[0] < event.renderer.posX + 16 && size[3] >= event.renderer.posX;
					boolean flag2 = size[1] < event.renderer.posY + 16 && size[4] >= event.renderer.posY;
					boolean flag3 = size[2] < event.renderer.posZ + 16 && size[5] >= event.renderer.posZ;
					if (flag1 && flag2 && flag3) {
						if (GLHelper.isValid(rail.glList)) {
							rail.shouldRerender = true;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLoadSound(SoundLoadEvent event) {
		this.soundHandler.onLoadSound(event);
	}

	private boolean isPlayerSittingSeat(EntityPlayer player, byte type) {
		if (player.isRiding() && player.ridingEntity instanceof EntityFloor) {
			if (((EntityFloor) player.ridingEntity).getSeatType() == type) {
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Pre event) {
		if (this.isPlayerSittingSeat(event.entityPlayer, (byte) 3))//寝台
		{
			GL11.glPushMatrix();
			event.renderer.modelBipedMain.isRiding = false;
			GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
		}
	}

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Post event) {
		if (this.isPlayerSittingSeat(event.entityPlayer, (byte) 3))//寝台
		{
			event.renderer.modelBipedMain.isRiding = true;
			GL11.glPopMatrix();
		}

		//RenderEventHandler.INSTANCE.renderEntity(event.entity, event.renderer, event.x, event.y, event.z);

		RenderBullet.INSTANCE.onPlayerRender(event.entityPlayer, false);
	}

	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Pre event) {
		this.renderEntityPre(event.entity, event.renderer, event.x, event.y, event.z);
	}

	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Post event) {
		this.renderEntityPost(event.entity, event.renderer, event.x, event.y, event.z);

		if (event.entity instanceof EntityNPC) {
			RenderBullet.INSTANCE.onNPCRender((EntityNPC) event.entity, event.x, event.y, event.z);
		}
	}

	@SubscribeEvent
	public void onRenderPlayerHand(RenderHandEvent event) {
		EntityPlayer player = NGTUtilClient.getMinecraft().thePlayer;
		byte viewMode = ClientProxy.getViewMode(player);
		if (viewMode >= 0 && viewMode < 3) {
			event.setCanceled(true);
		}

		RenderBullet.INSTANCE.onPlayerRender(player, true);
	}

	//NVD////////////////////////////////////////////////////////////////////////

	public void renderEntityPre(EntityLivingBase entity, RendererLivingEntity renderer, double x, double y, double z) {
		if (!this.isPlayerWearedNVD(entity)) {
			return;
		}

		GLHelper.disableLighting();
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 0.5F, 0.4F, 1.0F);
		GLHelper.setLightmapMaxBrightness();
	}

	public void renderEntityPost(EntityLivingBase entity, RendererLivingEntity renderer, double x, double y, double z) {
		if (!this.isPlayerWearedNVD(entity)) {
			return;
		}

		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		GLHelper.enableLighting();
	}

	private boolean isPlayerWearedNVD(EntityLivingBase entity) {
		EntityPlayer viewer = NGTUtilClient.getMinecraft().thePlayer;
		return entity != viewer && ClientProxy.getViewMode(viewer) == 3;
	}

	/////////////////////////////////////////////////////////////////////////////
}