package jp.ngt.mcte;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import jp.ngt.mcte.editor.filter.FilterManager;
import jp.ngt.mcte.network.PacketMCTEKey;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class MCTEKeyHandlerClient {
	private static final String CATG_MCTE = "mcte.key";
	public static final KeyBinding keyEditMenu = new KeyBinding("mcte.editor.menu", Keyboard.KEY_K, CATG_MCTE);
	public static final KeyBinding keyEditMode = new KeyBinding("mcte.editor.mode", Keyboard.KEY_M, CATG_MCTE);
	public static final KeyBinding keyDelete = new KeyBinding("mcte.editor.delete", Keyboard.KEY_DELETE, CATG_MCTE);
	public static final KeyBinding keyUndo = new KeyBinding("mcte.editor.undo", Keyboard.KEY_Z, CATG_MCTE);
	public static final KeyBinding keyCut = new KeyBinding("mcte.editor.cut", Keyboard.KEY_X, CATG_MCTE);
	public static final KeyBinding keyCopy = new KeyBinding("mcte.editor.copy", Keyboard.KEY_C, CATG_MCTE);
	public static final KeyBinding keyPaste = new KeyBinding("mcte.editor.paste", Keyboard.KEY_V, CATG_MCTE);
	public static final KeyBinding keyFill = new KeyBinding("mcte.editor.fill", Keyboard.KEY_B, CATG_MCTE);
	public static final KeyBinding keyClear = new KeyBinding("mcte.editor.clear", Keyboard.KEY_N, CATG_MCTE);

	public static void init() {
		ClientRegistry.registerKeyBinding(keyEditMenu);
		ClientRegistry.registerKeyBinding(keyEditMode);
		ClientRegistry.registerKeyBinding(keyDelete);
		ClientRegistry.registerKeyBinding(keyUndo);
		ClientRegistry.registerKeyBinding(keyCut);
		ClientRegistry.registerKeyBinding(keyCopy);
		ClientRegistry.registerKeyBinding(keyPaste);
		ClientRegistry.registerKeyBinding(keyFill);
		ClientRegistry.registerKeyBinding(keyClear);
	}

	@SubscribeEvent
	public void keyDown(KeyInputEvent event) {
		EntityPlayer player = NGTUtil.getClientPlayer();

		if (keyEditMenu.isPressed()) {
			if (NGTUtil.isEquippedItem(player, MCTE.painter)) {
				player.openGui(MCTE.instance, MCTE.guiIdPainter, player.worldObj, 0, 0, 0);
			} else {
				this.sendKeyToServer(MCTE.KEY_EditMenu);
			}
		} else if (keyEditMode.isPressed()) {
			this.sendKeyToServer(MCTE.KEY_EditMode);
		} else if (keyUndo.isPressed()) {
			this.sendKeyToServer(MCTE.KEY_Undo);
		} else if (keyClear.isPressed()) {
			this.sendKeyToServer(MCTE.KEY_Clear);
		} else if (keyDelete.isPressed()) {
			FilterManager.INSTANCE.execFilter(player, "Delete");
			NGTLog.sendChatMessage(player, "Delete Blocks");
		} else if (keyCut.isPressed()) {
			FilterManager.INSTANCE.execFilter(player, "Cut");
			NGTLog.sendChatMessage(player, "Cut Blocks");
		} else if (keyCopy.isPressed()) {
			FilterManager.INSTANCE.execFilter(player, "Copy");
			NGTLog.sendChatMessage(player, "Copy Blocks");
		} else if (keyPaste.isPressed()) {
			FilterManager.INSTANCE.execFilter(player, "Paste");
			NGTLog.sendChatMessage(player, "Paste Blocks");
		} else if (keyFill.isPressed()) {
			FilterManager.INSTANCE.execFilter(player, "Fill");
			NGTLog.sendChatMessage(player, "Fill Blocks");
		}
	}

	private void sendKeyToServer(byte keyCode) {
		EntityPlayer player = NGTUtil.getClientPlayer();
		MCTE.NETWORK_WRAPPER.sendToServer(new PacketMCTEKey(player, keyCode));
	}
}