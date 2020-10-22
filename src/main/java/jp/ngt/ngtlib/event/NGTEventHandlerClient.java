package jp.ngt.ngtlib.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.GLHelper;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent;

@SideOnly(Side.CLIENT)
public final class NGTEventHandlerClient {
	public static final NGTEventHandlerClient INSTANCE = new NGTEventHandlerClient();

	private NGTEventHandlerClient() {
	}

	@SubscribeEvent
	public void onChangeTexture(TextureStitchEvent.Post event) {
		GLHelper.initGLList();
	}

	/*@SubscribeEvent
	public void onFinishRenderWorld(RenderWorldLastEvent event)
	{
		GLHelper.clearGLList();
	}*/

	/*@SubscribeEvent
	public void onOpenGui(GuiOpenEvent event)//Minecraft 830
	{
		GLHelper.clearGLList();
	}*/

	@SubscribeEvent
	public void onUnloadWorld(WorldEvent.Unload event) {
		if (event.world.isRemote) {
			//GLHelper.clearGLList();
		}
	}
}